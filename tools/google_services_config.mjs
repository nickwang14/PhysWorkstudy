import { existsSync, readFileSync, writeFileSync, renameSync, rmSync, mkdirSync } from 'node:fs';
import { resolve, dirname, basename } from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs, parseEnv, isDeepStrictEqual } from 'node:util';
import { randomUUID } from 'node:crypto';
import { spawnSync } from 'node:child_process';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const token = /^\$\{(FIREBASE_[A-Z0-9_]+)\}$/;
const read = path => readFileSync(path, 'utf8');
function readJson(path) {
  try { return JSON.parse(read(path)); }
  catch { throw new Error('Cannot read/parse configuration JSON; details suppressed to protect values.'); }
}

export function variables(template) {
  const keys = new Set();
  function visit(value) {
    if (value && typeof value === 'object') Object.values(value).forEach(visit);
    else {
      const match = typeof value === 'string' && value.match(token);
      if (!match) throw new Error('Every template leaf must be a FIREBASE_* placeholder.');
      keys.add(match[1]);
    }
  }
  visit(template);
  return [...keys];
}

function usable(key, value) {
  if (typeof value !== 'string' || !value.trim() || value !== value.trim() ||
      /^(YOUR_|CHANGEME|REPLACE_ME)|\$\{|[\r\n\0]/i.test(value)) {
    throw new Error(`Missing, placeholder or invalid value for ${key}; no file written.`);
  }
  return value;
}

// Locate real assignments without treating lines inside a quoted multiline
// value as new variables. Keep spans so unrelated text is copied byte-for-byte.
function assignments(text) {
  const lines = text.match(/[^\n]*\n|[^\n]+$/g) ?? [];
  const entries = [];
  let offset = 0;
  for (let i = 0; i < lines.length; i++) {
    const start = offset;
    const line = lines[i];
    offset += line.length;
    const match = line.replace(/[\r\n]+$/, '').match(/^\s*(?:export\s+)?([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.*)$/);
    if (!match) continue;
    const first = match[2][0];
    if (['"', "'", '`'].includes(first)) {
      // Node's parseEnv closes at the next matching quote (no shell expansion).
      let remainder = match[2].slice(1);
      while (!remainder.includes(first) && i + 1 < lines.length) {
        remainder += lines[++i];
        offset += lines[i].length;
      }
    }
    entries.push({ key: match[1], start, end: offset,
      newline: lines[i].endsWith('\r\n') ? '\r\n' : lines[i].endsWith('\n') ? '\n' : '' });
  }
  return entries;
}

export function envValues(text, keys) {
  const seen = new Set();
  for (const entry of assignments(text)) {
    if (!keys.includes(entry.key)) continue;
    if (seen.has(entry.key)) throw new Error(`Duplicate .env entry: ${entry.key}`);
    seen.add(entry.key);
  }
  let parsed;
  try { parsed = parseEnv(text.replace(/^\uFEFF/, '')); }
  catch { throw new Error('Cannot parse .env; details suppressed to protect values.'); }
  return Object.fromEntries(keys.map(key => [key, usable(key, parsed[key])]));
}

export function render(template, values) {
  variables(template);
  function visit(value) {
    if (Array.isArray(value)) return value.map(visit);
    if (value && typeof value === 'object') return Object.fromEntries(Object.entries(value).map(([k, v]) => [k, visit(v)]));
    const key = value.match(token)[1];
    const actual = usable(key, values[key]);
    if (key.endsWith('_CLIENT_TYPE')) {
      if (!/^[123]$/.test(actual)) throw new Error(`Invalid OAuth client type: ${key}`);
      return Number(actual);
    }
    return actual;
  }
  return visit(template);
}

export function extract(template, config) {
  variables(template);
  const result = {};
  function visit(expected, actual, path = 'root') {
    if (Array.isArray(expected)) {
      if (!Array.isArray(actual) || expected.length !== actual.length) throw new Error(`Config/template array mismatch at ${path}; update the template, do not drop entries.`);
      expected.forEach((entry, i) => visit(entry, actual[i], `${path}[${i}]`));
    } else if (expected && typeof expected === 'object') {
      if (!actual || typeof actual !== 'object' || Array.isArray(actual) ||
          !isDeepStrictEqual(Object.keys(expected).sort(), Object.keys(actual).sort())) {
        throw new Error(`Config/template field mismatch at ${path}; update the template to preserve every field.`);
      }
      for (const [key, value] of Object.entries(expected)) visit(value, actual[key], `${path}.${key}`);
    } else {
      const key = expected.match(token)[1];
      const numeric = key.endsWith('_CLIENT_TYPE');
      if (typeof actual !== (numeric ? 'number' : 'string')) throw new Error(`Config type mismatch for ${key}`);
      const value = usable(key, String(actual));
      if (Object.hasOwn(result, key) && result[key] !== value) throw new Error(`Conflicting template reuse of ${key}`);
      result[key] = value;
    }
  }
  visit(template, config);
  if (!isDeepStrictEqual(render(template, result), config)) throw new Error('Configuration did not round-trip; no file written.');
  return result;
}

export function validate(config, expectedProject, expectedPackage) {
  const project = config.project_info;
  const client = config.client?.[0];
  if (!project || !/^\d+$/.test(project.project_number) || project.project_id !== expectedProject) throw new Error('Firebase project must match .firebaserc and have a numeric project number.');
  if (client?.client_info?.android_client_info?.package_name !== expectedPackage ||
      !client.client_info.mobilesdk_app_id.startsWith(`1:${project.project_number}:android:`)) throw new Error('Firebase Android registration must match applicationId and project number.');
  const [android, web] = client.oauth_client ?? [];
  if (android?.client_type !== 1 || android.android_info?.package_name !== expectedPackage ||
      !/^[a-f0-9]{40}$/i.test(android.android_info?.certificate_hash ?? '') ||
      web?.client_type !== 3 || client.services?.appinvite_service?.other_platform_oauth_client?.[0]?.client_type !== 3) {
    throw new Error('Expected Android SHA-1 OAuth client and web/App Invite client types are missing or invalid.');
  }
}

export function mergeEnv(text, values, overwrite = false) {
  const keys = Object.keys(values);
  for (const [key, value] of Object.entries(values)) {
    usable(key, value);
    // Firebase identifiers/keys are single-line tokens; keep Java Properties
    // compatibility for the unrelated ExerciseDB variables read by Gradle.
    if (!/^[A-Za-z0-9_.:/@+-]+$/.test(value)) throw new Error(`Unsupported .env token characters for ${key}`);
  }
  let parsed;
  try { parsed = parseEnv(text.replace(/^\uFEFF/, '')); }
  catch { throw new Error('Cannot parse existing .env; values suppressed.'); }
  const seen = new Set();
  const newline = text.includes('\r\n') ? '\r\n' : '\n';
  let updated = '';
  let cursor = 0;
  for (const entry of assignments(text)) {
    const key = entry.key;
    if (!keys.includes(key)) continue;
    if (seen.has(key)) throw new Error(`Duplicate .env entry: ${key}`);
    seen.add(key);
    const old = parsed[key];
    const placeholder = !old || /^(YOUR_|CHANGEME|REPLACE_ME)|\$\{/i.test(old);
    if (!overwrite && !placeholder && old !== values[key]) throw new Error(`Existing ${key} differs; use --overwrite-env only after reviewing the source project.`);
    updated += text.slice(cursor, entry.start) + `${key}=${values[key]}${entry.newline}`;
    cursor = entry.end;
  }
  updated += text.slice(cursor);
  const missing = keys.filter(key => !seen.has(key));
  if (missing.length) {
    if (updated && !updated.endsWith('\n')) updated += newline;
    updated += `${newline}# Firebase Android client config; generated/imported locally, never commit values.${newline}`;
    updated += missing.map(key => `${key}=${values[key]}`).join(newline) + newline;
  }
  return updated;
}

function atomicWrite(path, text) {
  mkdirSync(dirname(path), { recursive: true });
  const temporary = `${path}.${randomUUID()}.tmp`;
  try {
    writeFileSync(temporary, text, { flag: 'wx', mode: 0o600 });
    renameSync(temporary, path);
  } finally { rmSync(temporary, { force: true }); }
}

function requireIgnored(path, root) {
  const result = spawnSync('git', ['check-ignore', '--', path], { cwd: root, encoding: 'utf8', windowsHide: true });
  if (result.status !== 0) throw new Error('Write destination must be untracked and Git-ignored; no values written. Git must be available.');
}

export function main(args = process.argv.slice(2), workspaceRoot = root) {
  const root = workspaceRoot;
  const { values, positionals } = parseArgs({ args, allowPositionals: true, options: {
    'env-file': { type: 'string' }, template: { type: 'string' }, source: { type: 'string' },
    force: { type: 'boolean' }, 'overwrite-env': { type: 'boolean' }, help: { type: 'boolean' },
  } });
  if (values.help) {
    console.log('google_services_config.mjs import|generate|check [--env-file FILE] [--template FILE]\nImport: [--source FILE] [--overwrite-env]. Generate: [--force].\nDefaults: root .env, app/google-services.template.json, app/google-services.json. Values are never printed.');
    return;
  }
  const command = positionals[0];
  if (positionals.length !== 1 || !['import', 'generate', 'check'].includes(command)) throw new Error('Choose import, generate or check; see --help.');
  if ((values.source || values['overwrite-env']) && command !== 'import') throw new Error('--source/--overwrite-env are import-only.');
  if (values.force && command !== 'generate') throw new Error('--force is generate-only.');
  const envFile = values['env-file'] ? resolve(values['env-file']) : resolve(root, '.env');
  const envName = basename(envFile);
  if ((envName !== '.env' && !envName.startsWith('.env.')) || envName === '.env.example') throw new Error('Use an ignored .env or .env.<environment> file, never a tracked example.');
  const output = resolve(root, 'app/google-services.json');
  const templateFile = values.template ? resolve(values.template) : resolve(root, 'app/google-services.template.json');
  const template = readJson(templateFile);
  const project = readJson(resolve(root, '.firebaserc')).projects.default;
  const appId = read(resolve(root, 'app/build.gradle.kts')).match(/applicationId\s*=\s*"([^"]+)"/)?.[1];
  if (!project || !appId) throw new Error('Cannot determine approved Firebase project/applicationId.');
  if (command === 'import') {
    const source = values.source ? resolve(values.source) : output;
    if ([templateFile, resolve(root, '.env.example')].includes(source) ||
        envFile === source || envFile === templateFile || envFile === resolve(root, '.env.example')) throw new Error('Import source/environment must not overwrite templates or source JSON.');
    const config = readJson(source);
    const imported = extract(template, config);
    validate(config, project, appId);
    const existing = existsSync(envFile) ? read(envFile) : '';
    const merged = mergeEnv(existing, imported, values['overwrite-env']);
    requireIgnored(envFile, root);
    if (merged !== existing) atomicWrite(envFile, merged);
    console.log(`Imported ${Object.keys(imported).length} Firebase variables into local environment; unrelated entries preserved. Values not displayed.`);
    return;
  }
  if (!existsSync(envFile)) throw new Error('Local .env file is missing; import an authentic config first. Process environment is not a substitute.');
  requireIgnored(envFile, root);
  requireIgnored(output, root);
  const config = render(template, envValues(read(envFile), variables(template)));
  validate(config, project, appId);
  const existing = existsSync(output) ? readJson(output) : undefined;
  if (command === 'check') {
    if (!existing || !isDeepStrictEqual(existing, config)) throw new Error('Generated config is missing or differs from environment/template; run generate (review differences before --force).');
    console.log('Firebase config matches environment/template and approved project/applicationId. Values not displayed.');
  } else if (existing && isDeepStrictEqual(existing, config)) {
    console.log('Firebase config is already up to date. Values not displayed.');
  } else {
    if (existing && !values.force) throw new Error('Existing google-services.json differs; use --force only after reviewing environment values.');
    atomicWrite(output, JSON.stringify(config, null, 2) + '\n');
    console.log('Generated ignored app/google-services.json. Values not displayed.');
  }
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  try { main(); }
  catch (error) { console.error(`Firebase config: ${error.message}`); process.exitCode = 1; }
}
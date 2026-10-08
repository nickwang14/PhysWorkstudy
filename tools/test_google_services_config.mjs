import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, writeFileSync, mkdirSync, mkdtempSync, rmSync, existsSync, readdirSync } from 'node:fs';
import { join } from 'node:path';
import { tmpdir } from 'node:os';
import { fileURLToPath } from 'node:url';
import { spawnSync } from 'node:child_process';
import { variables, envValues, render, extract, validate, mergeEnv, main } from './google_services_config.mjs';

const template = JSON.parse(readFileSync(fileURLToPath(new URL('../app/google-services.template.json', import.meta.url)), 'utf8'));
const fake = {
  FIREBASE_PROJECT_NUMBER: '123456789', FIREBASE_PROJECT_ID: 'test-physiapp',
  FIREBASE_STORAGE_BUCKET: 'test-physiapp.firebasestorage.app',
  FIREBASE_MOBILESDK_APP_ID: '1:123456789:android:abcdef123456',
  FIREBASE_ANDROID_PACKAGE_NAME: 'com.test.physiapp',
  FIREBASE_ANDROID_OAUTH_CLIENT_ID: '123456789-android.apps.googleusercontent.com',
  FIREBASE_ANDROID_OAUTH_CLIENT_TYPE: '1', FIREBASE_ANDROID_OAUTH_PACKAGE_NAME: 'com.test.physiapp',
  FIREBASE_ANDROID_CERTIFICATE_HASH: 'abcdef1234'.repeat(4),
  FIREBASE_WEB_OAUTH_CLIENT_ID: '123456789-web.apps.googleusercontent.com', FIREBASE_WEB_OAUTH_CLIENT_TYPE: '3',
  FIREBASE_API_KEY: 'synthetic-test-key-not-a-credential',
  FIREBASE_APPINVITE_OAUTH_CLIENT_ID: '123456789-invite.apps.googleusercontent.com',
  FIREBASE_APPINVITE_OAUTH_CLIENT_TYPE: '3', FIREBASE_CONFIGURATION_VERSION: '1',
};
const config = render(template, fake);
const envText = Object.entries(fake).map(([k, v]) => `${k}=${v}`).join('\n') + '\n';

test('template contains every scalar placeholder and exact env-example coverage', () => {
  assert.equal(variables(template).length, 15);
  const example = readFileSync(fileURLToPath(new URL('../.env.example', import.meta.url)), 'utf8');
  const names = [...example.matchAll(/^(FIREBASE_[A-Z0-9_]+)=/gm)].map(match => match[1]);
  assert.deepEqual(names.sort(), variables(template).sort());
  assert.throws(() => variables({ key: 'real-literal-value' }), /placeholder/);
});

test('every leaf round-trips, including numeric OAuth types and separate App Invite ID', () => {
  assert.deepEqual(extract(template, config), fake);
  assert.deepEqual(render(template, extract(template, config)), config);
  assert.equal(typeof config.client[0].oauth_client[0].client_type, 'number');
  assert.equal(typeof config.configuration_version, 'string');
  validate(config, 'test-physiapp', 'com.test.physiapp');
});

test('import refuses unknown fields/extra clients instead of dropping data', () => {
  const extra = structuredClone(config);
  extra.project_info.firebase_url = 'https://example.invalid';
  assert.throws(() => extract(template, extra), /field mismatch/);
  const clients = structuredClone(config);
  clients.client[0].oauth_client.push({ client_id: 'extra', client_type: 3 });
  assert.throws(() => extract(template, clients), /array mismatch/);
  const wrongType = structuredClone(config);
  wrongType.client[0].oauth_client[0].client_type = '1';
  assert.throws(() => extract(template, wrongType), /type mismatch/);
});

test('missing/blank/placeholders/duplicate entries fail with names only', () => {
  for (const value of [undefined, '', 'YOUR_API_KEY', '${FIREBASE_API_KEY}', 'CHANGEME', 'a\nb']) {
    assert.throws(() => render(template, { ...fake, FIREBASE_API_KEY: value }), /FIREBASE_API_KEY/);
  }
  assert.throws(() => envValues(envText + 'FIREBASE_API_KEY=duplicate\n', variables(template)), /Duplicate/);
  assert.throws(() => envValues('', variables(template)), /Missing/);
  assert.throws(() => render(template, { ...fake, FIREBASE_WEB_OAUTH_CLIENT_TYPE: 'NaN' }), /client type/);
});

test('quoted, BOM, CRLF env input is parsed without variable interpolation', () => {
  const text = '\uFEFF' + envText.replace('FIREBASE_PROJECT_ID=test-physiapp', 'export FIREBASE_PROJECT_ID="test-physiapp" # comment').replaceAll('\n', '\r\n');
  assert.deepEqual(envValues(text, variables(template)), fake);
});

test('import preserves unrelated values/comments/CRLF and is idempotent', () => {
  const unrelated = '# personal settings\r\nEXERCISE_DB_API_KEY=keep-this-local\r\nCUSTOM="unchanged"\r\n';
  const merged = mergeEnv(unrelated, fake);
  assert.ok(merged.startsWith(unrelated));
  assert.deepEqual(envValues(merged, variables(template)), fake);
  assert.equal(mergeEnv(merged, fake), merged);
  assert.ok(!merged.replaceAll('\r\n', '').includes('\n'));
});

test('assignment-like text within multiline quoted values remains unchanged', () => {
  for (const quote of ['"', "'", '`']) {
    const unrelated = `CUSTOM=${quote}first line\nFIREBASE_API_KEY=YOUR_FAKE\nFIREBASE_PROJECT_ID=YOUR_FAKE\nlast line${quote}\n`;
    const merged = mergeEnv(unrelated + envText, fake);
    assert.ok(merged.startsWith(unrelated));
    assert.deepEqual(envValues(merged, variables(template)), fake);
  }
});

test('only placeholders may be replaced without overwrite approval', () => {
  assert.equal(mergeEnv('FIREBASE_API_KEY=YOUR_API_KEY\n', { FIREBASE_API_KEY: fake.FIREBASE_API_KEY }), `FIREBASE_API_KEY=${fake.FIREBASE_API_KEY}\n`);
  assert.throws(() => mergeEnv('FIREBASE_API_KEY=other-value\n', { FIREBASE_API_KEY: fake.FIREBASE_API_KEY }), /overwrite-env/);
  assert.equal(mergeEnv('FIREBASE_API_KEY=other-value\n', { FIREBASE_API_KEY: fake.FIREBASE_API_KEY }, true), `FIREBASE_API_KEY=${fake.FIREBASE_API_KEY}\n`);
  assert.throws(() => mergeEnv('FIREBASE_API_KEY=one\nFIREBASE_API_KEY=two\n', { FIREBASE_API_KEY: fake.FIREBASE_API_KEY }, true), /Duplicate/);
});

test('validate rejects project, package, SHA and OAuth mismatches', () => {
  assert.throws(() => validate(config, 'wrong', 'com.test.physiapp'), /project/);
  assert.throws(() => validate(config, 'test-physiapp', 'com.wrong'), /applicationId/);
  const invalid = structuredClone(config);
  invalid.client[0].oauth_client[0].android_info.certificate_hash = 'invalid';
  assert.throws(() => validate(invalid, 'test-physiapp', 'com.test.physiapp'), /SHA-1/);
});

function fixture(callback) {
  const root = mkdtempSync(join(tmpdir(), 'physiapp-firebase-config-'));
  try {
    mkdirSync(join(root, 'app'));
    writeFileSync(join(root, '.firebaserc'), JSON.stringify({ projects: { default: 'test-physiapp' } }));
    writeFileSync(join(root, 'app/build.gradle.kts'), 'applicationId = "com.test.physiapp"');
    writeFileSync(join(root, 'app/google-services.template.json'), JSON.stringify(template));
    writeFileSync(join(root, '.gitignore'), '.env\n.env.*\n!.env.example\n**/google-services*.json\n**/google-services*.tmp\n!**/google-services.template.json\n');
    const result = spawnSync('git', ['init', '--quiet'], { cwd: root, encoding: 'utf8' });
    assert.equal(result.status, 0, 'Git is required for ignored-destination safety checks');
    callback(root);
  } finally { rmSync(root, { recursive: true, force: true }); }
}

test('CLI import/generate/check handles initial import and subsequent regeneration', () => fixture(root => {
  const output = join(root, 'app/google-services.json');
  writeFileSync(output, JSON.stringify(config));
  main(['import'], root);
  const imported = readFileSync(join(root, '.env'), 'utf8');
  assert.deepEqual(envValues(imported, variables(template)), fake);
  rmSync(output);
  main(['generate'], root);
  assert.deepEqual(JSON.parse(readFileSync(output, 'utf8')), config);
  main(['check'], root);
  const before = readFileSync(output, 'utf8');
  main(['generate'], root);
  assert.equal(readFileSync(output, 'utf8'), before);
  assert.ok(!readdirSync(join(root, 'app')).some(name => name.endsWith('.tmp')));
}));

test('CLI checks stale configs and requires force before overwriting', () => fixture(root => {
  const output = join(root, 'app/google-services.json');
  writeFileSync(join(root, '.env'), envText);
  const stale = structuredClone(config);
  stale.client[0].api_key[0].current_key = 'synthetic-stale-key';
  writeFileSync(output, JSON.stringify(stale));
  assert.throws(() => main(['check'], root), /differs/);
  assert.throws(() => main(['generate'], root), /force/);
  assert.deepEqual(JSON.parse(readFileSync(output, 'utf8')), stale);
  main(['generate', '--force'], root);
  assert.deepEqual(JSON.parse(readFileSync(output, 'utf8')), config);
}));

test('CLI will not write examples, arbitrary files, tracked outputs or unignored env files', () => fixture(root => {
  const output = join(root, 'app/google-services.json');
  writeFileSync(output, JSON.stringify(config));
  assert.throws(() => main(['import', '--env-file', join(root, '.env.example')], root), /tracked example/);
  assert.throws(() => main(['import', '--env-file', join(root, 'config.env')], root), /ignored/);
  writeFileSync(join(root, '.gitignore'), '**/google-services*.json\n!**/google-services.template.json\n');
  assert.throws(() => main(['import'], root), /Git-ignored/);
  assert.equal(existsSync(join(root, '.env')), false);
  writeFileSync(join(root, '.gitignore'), '.env\n**/google-services*.json\n!**/google-services.template.json\n');
  writeFileSync(join(root, '.env'), envText);
  assert.equal(spawnSync('git', ['add', '-f', 'app/google-services.json'], { cwd: root }).status, 0);
  assert.throws(() => main(['generate'], root), /untracked/);
}));

test('CLI never substitutes process credentials for a missing local env file', () => fixture(root => {
  assert.throws(() => main(['generate'], root), /Process environment is not a substitute/);
  assert.equal(existsSync(join(root, 'app/google-services.json')), false);
}));

test('malformed source JSON errors do not reveal source values', () => fixture(root => {
  writeFileSync(join(root, 'app/google-services.json'), '{"key":"CANARY_DO_NOT_LEAK", broken');
  assert.throws(() => main(['import'], root), error => !error.message.includes('CANARY_DO_NOT_LEAK') && /suppressed/.test(error.message));
  assert.equal(existsSync(join(root, '.env')), false);
}));
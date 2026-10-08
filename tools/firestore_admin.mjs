import { readFileSync, mkdirSync, writeFileSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';
import { randomUUID } from 'node:crypto';
import Ajv from 'ajv';
import addFormats from 'ajv-formats';
import { initializeApp, applicationDefault, deleteApp } from 'firebase-admin/app';
import { getFirestore, Timestamp, FieldValue } from 'firebase-admin/firestore';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const config = JSON.parse(readFileSync(resolve(root, 'firebase.json'), 'utf8'));
const projects = JSON.parse(readFileSync(resolve(root, '.firebaserc'), 'utf8')).projects;
const blueprint = JSON.parse(readFileSync(resolve(root, 'firebase-blueprint.json'), 'utf8'));
export const DATABASE_ID = config.firestore[0].database;
const schemas = { users: 'UserProfile', curriculum_lessons: 'CurriculumLessonIndex' };
const ajv = new Ajv({ allErrors: true });
ajv.addKeyword('x-firestore-type');
addFormats(ajv);
const validators = Object.fromEntries(Object.entries(schemas).map(([collection, schema]) =>
  [collection, ajv.compile(blueprint.entities[schema])]));

export function target(options, env = process.env) {
  const mode = options.mode ?? 'emulator';
  if (!['emulator', 'live'].includes(mode)) throw new Error('Mode must be emulator or live.');
  const project = options.project ?? (mode === 'emulator' ? projects.local : undefined);
  const database = options.database ?? (mode === 'emulator' ? DATABASE_ID : undefined);
  if (!project || !database) throw new Error('Live mode requires --project and --database.');
  if (database !== DATABASE_ID) throw new Error('Database is not the named database in firebase.json.');
  if (mode === 'live') {
    if (project !== projects.default) throw new Error('Project is not the approved project in .firebaserc.');
    if (env.FIRESTORE_EMULATOR_HOST) throw new Error('Unset FIRESTORE_EMULATOR_HOST before live mode.');
  } else {
    if (!project.startsWith('demo-')) throw new Error('Emulator mode requires a demo- project.');
    const host = env.FIRESTORE_EMULATOR_HOST ?? '127.0.0.1:8085';
    if (!/^(127\.0\.0\.1|localhost):[0-9]+$/.test(host)) throw new Error('Emulator must use a loopback host:port.');
    return { mode, project, database, host };
  }
  return { mode, project, database };
}

export function documentPath(path) {
  const parts = (path ?? '').split('/');
  if (parts.length !== 2 || !Object.hasOwn(schemas, parts[0]) || !parts[1] || ['.', '..'].includes(parts[1])) {
    throw new Error('Use an exact users/{uid} or curriculum_lessons/{lessonId} document path.');
  }
  return parts;
}

export function portable(data) {
  if (data instanceof Timestamp) return data.toDate().toISOString();
  if (Array.isArray(data)) return data.map(portable);
  if (data && typeof data === 'object') return Object.fromEntries(Object.entries(data).map(([k, v]) => [k, portable(v)]));
  return data;
}

export function prepare(path, existing, patch, remove = [], now = new Date()) {
  const [collection, id] = documentPath(path);
  const schema = blueprint.entities[schemas[collection]];
  if (!patch || typeof patch !== 'object' || Array.isArray(patch)) throw new Error('Data must be a JSON object.');
  const update = {};
  const merged = { ...portable(existing ?? {}) };
  for (const [field, property] of Object.entries(schema.properties)) {
    if (property['x-firestore-type'] !== 'timestamp' || Object.hasOwn(patch, field) || !existing) continue;
    if (!(existing[field] instanceof Timestamp) || existing[field].toMillis() > now.getTime()) {
      throw new Error(`Existing ${field} must be a native, non-future Timestamp; explicitly repair it.`);
    }
  }
  for (const [field, value] of Object.entries(patch)) {
    const property = schema.properties[field];
    if (!Object.hasOwn(schema.properties, field)) throw new Error(`Unknown field: ${field}`);
    let normalized = value;
    if (property['x-firestore-type'] === 'timestamp') {
      normalized = value === 'now' ? now.toISOString() : value;
      if (typeof normalized !== 'string' || !Number.isFinite(Date.parse(normalized))) throw new Error(`Invalid timestamp: ${field}`);
      if (Date.parse(normalized) > now.getTime()) throw new Error(`Future timestamp: ${field}`);
      update[field] = value === 'now' ? FieldValue.serverTimestamp() : Timestamp.fromDate(new Date(normalized));
    } else {
      update[field] = value;
    }
    merged[field] = normalized;
  }
  for (const field of remove) {
    if (!Object.hasOwn(schema.properties, field) || schema.required.includes(field)) throw new Error(`Cannot remove field: ${field}`);
    if (Object.hasOwn(patch, field)) throw new Error(`Field is both set and removed: ${field}`);
    delete merged[field];
    update[field] = FieldValue.delete();
  }
  const identity = collection === 'users' ? 'userId' : 'lessonId';
  if (merged[identity] !== id) throw new Error(`${identity} must match the document ID.`);
  if (!validators[collection](merged)) throw new Error(ajv.errorsText(validators[collection].errors));
  if (!Object.keys(update).length) throw new Error('No fields to change.');
  return { update, merged };
}

export function authorizeWrite(options, destination, path) {
  if (!options.apply) return false;
  const expected = `${destination.project}/${destination.database}/${path}`;
  if (options.confirm !== expected) throw new Error(`Apply requires --confirm "${expected}"`);
  return true;
}

export async function execute(db, options, destination, output = console.log) {
  const command = options.command;
  const path = options.path;
  output(JSON.stringify({ target: destination, command, path }));
  if (command === 'query') {
    if (!Object.hasOwn(schemas, path)) throw new Error('Query collection must be users or curriculum_lessons.');
    const limit = Number(options.limit ?? 20);
    if (!Number.isInteger(limit) || limit < 1 || limit > 100) throw new Error('Limit must be 1–100.');
    const fields = (options.fields ?? '').split(',').filter(Boolean);
    const schema = blueprint.entities[schemas[path]];
    for (const field of fields) if (!Object.hasOwn(schema.properties, field)) throw new Error(`Unknown field: ${field}`);
    let query = db.collection(path);
    if (options['where-eq']) {
      const filter = JSON.parse(options['where-eq']);
      if (!Array.isArray(filter) || filter.length !== 2 || !Object.hasOwn(schema.properties, filter[0])) throw new Error('where-eq must be JSON [knownField, value].');
      query = query.where(filter[0], '==', filter[1]);
    }
    query = query.orderBy('__name__').limit(limit).select(...fields);
    const result = await query.get();
    output(JSON.stringify(result.docs.map(doc => ({ path: doc.ref.path, data: portable(doc.data()) })), null, 2));
    return;
  }
  documentPath(path);
  if (command === 'create' && options.remove) throw new Error('Create cannot use --remove; omit optional fields in the JSON.');
  const ref = db.doc(path);
  const snapshot = await ref.get();
  if (command === 'get') {
    output(JSON.stringify({ exists: snapshot.exists, data: portable(snapshot.data()) }, null, 2));
    return;
  }
  if (!['create', 'update', 'delete'].includes(command)) throw new Error('Unknown command.');
  if (command === 'create' && snapshot.exists) throw new Error('Document already exists; use update.');
  if (command !== 'create' && !snapshot.exists) throw new Error('Document does not exist.');
  const apply = authorizeWrite(options, destination, path);
  let prepared;
  if (command !== 'delete') {
    if (!options['data-file']) throw new Error('Create/update requires --data-file.');
    const patch = JSON.parse(readFileSync(resolve(options['data-file']), 'utf8'));
    prepared = prepare(path, snapshot.data(), patch, (options.remove ?? '').split(',').filter(Boolean));
  }
  output(JSON.stringify({ dryRun: !apply, before: portable(snapshot.data()), after: prepared?.merged ?? null }, null, 2));
  if (command === 'delete' && path.startsWith('users/')) output('Single profile only: subcollections and Firebase Auth account are NOT deleted.');
  if (!apply) return;
  if (destination.mode === 'live') {
    const backupDir = resolve(root, '.local/firebase-backups');
    mkdirSync(backupDir, { recursive: true });
    const backup = resolve(backupDir, `${Date.now()}-${randomUUID()}.json`);
    // Timestamp.toJSON preserves nanoseconds; unlike the human-readable preview.
    writeFileSync(backup, JSON.stringify({ target: destination, path, command, existed: snapshot.exists,
      updateTime: snapshot.updateTime, data: snapshot.data() }, null, 2), { flag: 'wx', mode: 0o600 });
    output(`Local before-image: ${backup}`);
  }
  if (command === 'create') await ref.create(prepared.update);
  else if (command === 'update') await ref.update(prepared.update, { lastUpdateTime: snapshot.updateTime });
  else await ref.delete({ lastUpdateTime: snapshot.updateTime });
  output('Applied one document operation.');
}

const help = `Local Firestore manager (Admin SDK; IAM, not client rules).
Commands: query COLLECTION | get PATH | create PATH | update PATH | delete PATH
Default: demo-physiapp, named database, emulator 127.0.0.1:8085.
Options: --mode emulator|live --project ID --database ID
  --limit 20 --fields field1,field2 --where-eq '["field",value]'
  --data-file FILE --remove optionalField --apply --confirm PROJECT/DATABASE/PATH
Writes preview by default. Live writes save before-images under .local/.
Query returns IDs only unless --fields is given. Get/previews may contain personal data.
See docs/firebase-local-development.md for authentication and examples.`;

export async function main(args = process.argv.slice(2)) {
  const { values, positionals } = parseArgs({ args, allowPositionals: true, options: {
    mode: { type: 'string' }, project: { type: 'string' }, database: { type: 'string' },
    limit: { type: 'string' }, fields: { type: 'string' }, 'where-eq': { type: 'string' },
    'data-file': { type: 'string' }, remove: { type: 'string' }, apply: { type: 'boolean' },
    confirm: { type: 'string' }, help: { type: 'boolean', short: 'h' },
  } });
  if (values.help || !positionals.length) { console.log(help); return; }
  if (positionals.length !== 2 || !['query', 'get', 'create', 'update', 'delete'].includes(positionals[0])) throw new Error('Expected command and one exact path/collection; see --help.');
  const destination = target(values);
  if (destination.mode === 'emulator') process.env.FIRESTORE_EMULATOR_HOST = destination.host;
  const app = initializeApp({ projectId: destination.project,
    ...(destination.mode === 'live' ? { credential: applicationDefault() } : {}) });
  const db = getFirestore(app, destination.database);
  try {
    db.settings({ clientConfig: { interfaces: { 'google.firestore.v1.Firestore': {
      retry_params: { default: { initial_retry_delay_millis: 100, retry_delay_multiplier: 1.3,
        max_retry_delay_millis: 1000, initial_rpc_timeout_millis: 15000, rpc_timeout_multiplier: 1,
        max_rpc_timeout_millis: 15000, total_timeout_millis: 15000 } },
      methods: Object.fromEntries(['BatchGetDocuments', 'RunQuery', 'Commit'].map(method =>
        [method, { timeout_millis: 15000, retry_codes_name: 'non_idempotent' }])),
    } } } });
    await execute(db, { ...values, command: positionals[0], path: positionals[1] }, destination);
  } finally {
    try { await db.terminate(); } finally { await deleteApp(app); }
  }
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  main().catch(error => { console.error(`Firestore manager: ${error.message}`); process.exitCode = 1; });
}
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { Timestamp } from 'firebase-admin/firestore';
import { target, DATABASE_ID, documentPath, prepare, authorizeWrite, execute } from './firestore_admin.mjs';

export const profile = {
  userId: 'local-review-user', email: 'local-review@example.invalid', displayName: 'Local Review',
  trainingGoal: 'Strength & Hypertrophy', fitnessLevel: 'Beginner', weeklyTargetWorkouts: 3,
  favoriteSplit: 'Full Body', createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z',
};
const storedProfile = { ...profile, createdAt: Timestamp.fromDate(new Date(profile.createdAt)),
  updatedAt: Timestamp.fromDate(new Date(profile.updatedAt)) };
const emulator = target({}, {});

test('default is named database on demo emulator, never live', () => {
  assert.equal(emulator.mode, 'emulator');
  assert.equal(emulator.project, 'demo-physiapp');
  assert.equal(emulator.database, DATABASE_ID);
  assert.equal(emulator.host, '127.0.0.1:8085');
  assert.throws(() => target({ project: 'real-project' }, {}), /demo-/);
  assert.throws(() => target({}, { FIRESTORE_EMULATOR_HOST: 'remote:8085' }), /loopback/);
});

test('live mode requires explicit approved project/database and no emulator environment', () => {
  const live = { mode: 'live', project: 'gen-lang-client-0444088676', database: DATABASE_ID };
  assert.equal(target(live, {}).mode, 'live');
  assert.throws(() => target({ mode: 'live' }, {}), /requires/);
  assert.throws(() => target({ ...live, database: '(default)' }, {}), /named database/);
  assert.throws(() => target({ ...live, project: 'wrong' }, {}), /approved project/);
  assert.throws(() => target(live, { FIRESTORE_EMULATOR_HOST: '127.0.0.1:8085' }), /Unset/);
});

test('only exact supported document paths accepted', () => {
  assert.deepEqual(documentPath('users/test'), ['users', 'test']);
  for (const path of ['', '/users/test', 'users/', 'users/test/lesson_progress/one', 'other/test', 'users/..',
    'constructor/test', 'toString/test', '__proto__/test']) {
    assert.throws(() => documentPath(path));
  }
});

test('validate profiles, ID equality and known changed fields', () => {
  const result = prepare('users/local-review-user', undefined, profile);
  assert.ok(result.update.createdAt instanceof Timestamp);
  assert.throws(() => prepare('users/other', undefined, profile), /document ID/);
  assert.throws(() => prepare('users/local-review-user', storedProfile, { weeklyTargetWorkouts: 0 }));
  assert.throws(() => prepare('users/local-review-user', storedProfile, { fitnessLevel: 'Expert' }));
  assert.throws(() => prepare('users/local-review-user', storedProfile, { displayName: '' }));
  assert.throws(() => prepare('users/local-review-user', storedProfile, { typo: 3 }), /Unknown field/);
  assert.throws(() => prepare('users/local-review-user', storedProfile, { toString: 'bad' }), /Unknown field/);
});

test('remove optional fields only, retain legacy fields and required fields', () => {
  const result = prepare('users/local-review-user', { ...storedProfile, weightKg: 70, legacy: true }, {}, ['weightKg']);
  assert.equal(result.merged.weightKg, undefined);
  assert.equal(result.merged.legacy, true);
  assert.throws(() => prepare('users/local-review-user', storedProfile, {}, ['email']), /Cannot remove/);
  assert.throws(() => prepare('users/local-review-user', storedProfile, { weightKg: 70 }, ['weightKg']), /both/);
  assert.throws(() => prepare('users/local-review-user', storedProfile, { weightKg: null }));
  assert.throws(() => prepare('users/local-review-user', storedProfile, {}), /No fields/);
});

test('timestamp input becomes native timestamp or server sentinel', () => {
  const now = new Date('2026-01-02T00:00:00Z');
  const result = prepare('users/local-review-user', storedProfile, { updatedAt: 'now' }, [], now);
  assert.equal(result.merged.updatedAt, now.toISOString());
  assert.throws(() => prepare('users/local-review-user', storedProfile, { updatedAt: 'bad' }), /timestamp/);
  assert.throws(() => prepare('users/local-review-user', storedProfile, { updatedAt: '2099-01-01T00:00:00Z' }), /Future/);
});

test('unchanged timestamps must already have valid native types', () => {
  assert.throws(() => prepare('users/local-review-user', profile, { displayName: 'Edit' }), /native/);
  assert.throws(() => prepare('users/local-review-user', { ...storedProfile,
    createdAt: Timestamp.fromDate(new Date('2099-01-01')) }, { displayName: 'Edit' }), /non-future/);
  const repair = prepare('users/local-review-user', profile, { createdAt: profile.createdAt, updatedAt: 'now' });
  assert.ok(repair.update.createdAt instanceof Timestamp);
});

test('create rejects field-removal option before any database access', async () => {
  await assert.rejects(execute({}, { command: 'create', path: 'users/test', remove: 'weightKg' }, emulator, () => {}), /Create cannot/);
});

test('apply requires full target confirmation even on emulator', () => {
  const path = 'users/local-review-user';
  assert.equal(authorizeWrite({}, emulator, path), false);
  assert.throws(() => authorizeWrite({ apply: true }, emulator, path), /confirm/);
  assert.equal(authorizeWrite({ apply: true, confirm: `${emulator.project}/${DATABASE_ID}/${path}` }, emulator, path), true);
});

test('delete preview does not mutate; applied delete uses update precondition', async () => {
  let calls = 0;
  const ref = { get: async () => ({ exists: true, data: () => profile, updateTime: 'version' }),
    delete: async precondition => { assert.deepEqual(precondition, { lastUpdateTime: 'version' }); calls++; } };
  const db = { doc: () => ref };
  const options = { command: 'delete', path: 'users/local-review-user' };
  await execute(db, options, emulator, () => {});
  assert.equal(calls, 0);
  await execute(db, { ...options, apply: true, confirm: `${emulator.project}/${DATABASE_ID}/${options.path}` }, emulator, () => {});
  assert.equal(calls, 1);
});

test('query validates limits and fields before network call', async () => {
  const db = { collection: () => { throw new Error('Unexpected database call'); } };
  await assert.rejects(execute(db, { command: 'query', path: 'users', limit: '101' }, emulator, () => {}), /Limit/);
  await assert.rejects(execute(db, { command: 'query', path: 'users', fields: 'secret' }, emulator, () => {}), /Unknown/);
  await assert.rejects(execute(db, { command: 'query', path: 'constructor' }, emulator, () => {}), /collection/);
});
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { writeFileSync, mkdtempSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { initializeApp, deleteApp } from 'firebase-admin/app';
import { getFirestore } from 'firebase-admin/firestore';
import { target, execute, main } from './firestore_admin.mjs';

test('named emulator database: create, query, update, field removal, dry-run and delete', async () => {
  assert.ok(process.env.FIRESTORE_EMULATOR_HOST, 'Run via npm run test:backend:emulator, not against live Firebase');
  const destination = target({}, process.env);
  const app = initializeApp({ projectId: destination.project }, 'admin-integration');
  const db = getFirestore(app, destination.database);
  const uid = `cli-test-${Date.now()}`;
  const path = `users/${uid}`;
  const confirm = `${destination.project}/${destination.database}/${path}`;
  const temp = mkdtempSync(join(tmpdir(), 'physiapp-admin-'));
  const file = join(temp, 'data.json');
  const profile = { userId: uid, email: 'test@example.invalid', displayName: 'Synthetic Test',
    trainingGoal: 'Strength', fitnessLevel: 'Beginner', weeklyTargetWorkouts: 3,
    favoriteSplit: 'Full Body', createdAt: 'now', updatedAt: 'now', weightKg: 70 };
  const run = options => execute(db, { path, ...options }, destination, () => {});
  try {
    writeFileSync(file, JSON.stringify(profile));
    await run({ command: 'create', 'data-file': file });
    assert.equal((await db.doc(path).get()).exists, false);
    await run({ command: 'create', 'data-file': file, apply: true, confirm });
    assert.equal((await db.doc(path).get()).data().displayName, 'Synthetic Test');
    writeFileSync(file, JSON.stringify({ displayName: 'Updated Test', updatedAt: 'now' }));
    await run({ command: 'update', 'data-file': file, remove: 'weightKg', apply: true, confirm });
    assert.equal((await db.doc(path).get()).data().weightKg, undefined);
    const output = [];
    await execute(db, { command: 'query', path: 'users', fields: 'displayName', 'where-eq': JSON.stringify(['userId', uid]) }, destination, text => output.push(text));
    assert.match(output.join('\n'), /Updated Test/);
    // Parent deletion must not silently cascade.
    await db.doc(`${path}/lesson_progress/test-lesson`).set({ lessonId: 'test-lesson' });
    await run({ command: 'delete' });
    assert.equal((await db.doc(path).get()).exists, true);
    await run({ command: 'delete', apply: true, confirm });
    assert.equal((await db.doc(path).get()).exists, false);
    assert.equal((await db.doc(`${path}/lesson_progress/test-lesson`).get()).exists, true);
    // Exercise SDK settings/credential initialization, not just the operation layer.
    await main(['query', 'users', '--limit', '1']);
  } finally {
    await db.doc(`${path}/lesson_progress/test-lesson`).delete();
    await db.doc(path).delete();
    rmSync(temp, { recursive: true, force: true });
    await db.terminate();
    await deleteApp(app);
  }
});
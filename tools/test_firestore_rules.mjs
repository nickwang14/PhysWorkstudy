import { test } from 'node:test';
import assert from 'node:assert/strict';
import { initializeApp, deleteApp } from 'firebase/app';
import { getFirestore, connectFirestoreEmulator, doc, setDoc, getDoc, deleteDoc,
  serverTimestamp, terminate, collection, query, where, getDocs,
  runTransaction } from 'firebase/firestore';
import { assertFails, assertSucceeds } from '@firebase/rules-unit-testing';
import { initializeApp as initializeAdminApp, deleteApp as deleteAdminApp } from 'firebase-admin/app';
import { getFirestore as getAdminFirestore } from 'firebase-admin/firestore';
import { target } from './firestore_admin.mjs';

// Intentionally relies on firebase.json rules loaded by the CLI, not the rules-testing
// library's default-database-only setup. A named database with open rules fails this suite.
test('configured named database enforces current client rules', async () => {
  assert.ok(process.env.FIRESTORE_EMULATOR_HOST, 'Run via npm run test:rules');
  const destination = target({ project: process.env.GCP_PROJECT }, process.env);
  const [host, port] = destination.host.split(':');
  const apps = [];
  const clients = [];
  const client = (uid, claims = {}) => {
    const app = initializeApp({ projectId: destination.project, apiKey: 'emulator-only', appId: 'emulator-only' }, `rules-${apps.length}`);
    apps.push(app);
    const db = getFirestore(app, destination.database);
    connectFirestoreEmulator(db, host, Number(port), uid ? { mockUserToken: { sub: uid, user_id: uid, ...claims } } : {});
    clients.push(db);
    return db;
  };
  const alice = client('rules-alice');
  const bob = client('rules-bob');
  const anonymous = client();
  const reviewer = client('rules-admin', { admin: true });
  const adminApp = initializeAdminApp({ projectId: destination.project }, 'rules-cleanup');
  const admin = getAdminFirestore(adminApp, destination.database);
  const path = 'users/rules-alice';
  const lessonPath = 'curriculum_lessons/rules-test-lesson';
  const reviewPath = `${lessonPath}/review/status`;
  const commentPath = `${lessonPath}/comments/private-feedback`;
  const profile = { userId: 'rules-alice', email: 'alice@example.invalid', displayName: 'Rules Test',
    trainingGoal: 'Strength', fitnessLevel: 'Beginner', weeklyTargetWorkouts: 3, favoriteSplit: 'Full Body',
    createdAt: serverTimestamp(), updatedAt: serverTimestamp() };
  try {
    await admin.doc(path).delete();
    await admin.doc(lessonPath).delete();
    await assertFails(getDoc(doc(anonymous, path)));
    await assertFails(setDoc(doc(anonymous, path), profile));
    await assertSucceeds(setDoc(doc(alice, path), profile));
    await assertSucceeds(getDoc(doc(alice, path)));
    await assertFails(getDoc(doc(bob, path)));
    await assertFails(setDoc(doc(bob, path), profile));
    await assertFails(setDoc(doc(alice, path), { ...profile, weeklyTargetWorkouts: 10 }));
    await assertFails(deleteDoc(doc(alice, path)));
    const lesson = { lessonId: 'rules-test-lesson', title: 'Synthetic lesson', chapterId: 'test-chapter',
      chapterNumber: 1, chapterTitle: 'Test Chapter', subchapterId: 'test-subchapter', subchapterTitle: 'Test',
      lessonIndex: 1, durationMinutes: 8, category: 'MOVEMENT_PATTERNS', summary: 'Synthetic metadata only',
      assetPath: 'curriculum/synthetic.md' };
    await assertFails(setDoc(doc(alice, lessonPath), lesson));
    await assertSucceeds(setDoc(doc(reviewer, lessonPath), lesson));
    await assertSucceeds(getDoc(doc(bob, lessonPath)));
    await assertFails(getDoc(doc(anonymous, lessonPath)));
    await assertFails(deleteDoc(doc(alice, lessonPath)));
    const review = { status: 'needs_improvement', updatedBy: 'rules-admin', updatedAt: serverTimestamp() };
    await assertSucceeds(setDoc(doc(reviewer, reviewPath), review));
    await assertSucceeds(getDoc(doc(alice, reviewPath)));
    await assertFails(setDoc(doc(alice, reviewPath), { ...review, updatedBy: 'rules-alice' }));
    await assertFails(deleteDoc(doc(reviewer, reviewPath)));
    const comment = { authorId: 'rules-alice', text: '<script>literal private feedback</script>', createdAt: serverTimestamp() };
    await assertSucceeds(setDoc(doc(alice, commentPath), comment));
    assert.equal((await assertSucceeds(getDoc(doc(alice, commentPath)))).data().text, comment.text);
    await assertSucceeds(getDoc(doc(reviewer, commentPath)));
    await assertFails(getDoc(doc(bob, commentPath)));
    await assertFails(getDoc(doc(anonymous, commentPath)));
    await assertFails(setDoc(doc(alice, commentPath), { ...comment, text: 'Immutable' }));
    await assertFails(setDoc(doc(bob, `${lessonPath}/comments/forged`), comment));
    await assertFails(setDoc(doc(alice, `${lessonPath}/comments/extra`), { ...comment, admin: true }));
    await assertFails(getDocs(collection(alice, `${lessonPath}/comments`)));
    await assertSucceeds(getDocs(query(collection(alice, `${lessonPath}/comments`), where('authorId', '==', 'rules-alice'))));
    await assertSucceeds(getDocs(collection(reviewer, `${lessonPath}/comments`)));
    await assertFails(deleteDoc(doc(bob, commentPath)));
    await assertSucceeds(deleteDoc(doc(reviewer, commentPath)));
    // Validate Android's read-first payload/permission pattern online. Offline device
    // behavior needs actual transport failure, not JS disableNetwork (stream-only).
    const updateReview = () => runTransaction(reviewer, async transaction => {
      const parent = await transaction.get(doc(reviewer, lessonPath));
      assert.ok(parent.exists());
      transaction.set(doc(reviewer, reviewPath), { ...review, status: 'approved' });
    }, { maxAttempts: 1 });
    await assertSucceeds(updateReview());
    assert.equal((await getDoc(doc(reviewer, reviewPath))).data().status, 'approved');
  } finally {
    await admin.doc(commentPath).delete();
    await admin.doc(reviewPath).delete();
    await admin.doc(path).delete();
    await admin.doc(lessonPath).delete();
    await Promise.all(clients.map(db => terminate(db)));
    await Promise.all(apps.map(app => deleteApp(app)));
    await admin.terminate();
    await deleteAdminApp(adminApp);
  }
});
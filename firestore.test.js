const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");
const assert = require("node:assert/strict");
const { serverTimestamp, deleteField } = require("firebase/firestore");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

const validAliceProfile = {
  userId: ALICE_UID,
  email: "alice@example.com",
  displayName: "Alice Physi",
  trainingGoal: "Strength & Hypertrophy",
  fitnessLevel: "Intermediate",
  weeklyTargetWorkouts: 4,
  favoriteSplit: "Upper / Lower (4-Day Hypertrophy)",
  weightKg: 68.5,
  heightCm: 172.0,
  createdAt: new Date(),
  updatedAt: new Date(),
};

const validAliceWorkoutLog = {
  logId: "log_1",
  userId: ALICE_UID,
  workoutName: "Upper Body Hypertrophy",
  splitDay: "Upper Body",
  durationMinutes: 55,
  completedExercisesCount: 6,
  totalExercisesCount: 6,
  notes: "Great bench press session",
  completedAt: new Date(),
  createdAt: new Date(),
};

test("Unauthenticated user: cannot read user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Unauthenticated user: cannot write user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validAliceProfile));
});

test("Authenticated user: Alice can create and read her own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validAliceProfile));
  const snap = await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
  if (!snap.exists) throw new Error("Profile should exist");
});

test("Authenticated user: Bob cannot read or modify Alice's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set(validAliceProfile);
  });
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).set({ ...validAliceProfile, displayName: "Hacked" }));
});

test("Authenticated user: Alice cannot create profile with invalid data", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  // Invalid fitnessLevel
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).set({
    ...validAliceProfile,
    fitnessLevel: "Expert",
  }));
  // Invalid weeklyTargetWorkouts (> 7)
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).set({
    ...validAliceProfile,
    weeklyTargetWorkouts: 10,
  }));
});

test("WorkoutLog: Alice can log a workout session", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("workout_logs").doc("log_1").set(validAliceWorkoutLog)
  );
  const snap = await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("workout_logs").doc("log_1").get()
  );
  if (!snap.exists) throw new Error("Workout log should exist");
});

test("WorkoutLog: Bob cannot access Alice's workout logs", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("workout_logs").doc("log_1").set(validAliceWorkoutLog);
  });
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("workout_logs").doc("log_1").get()
  );
});

const LESSON_ID = "terminology-01";
const ADMIN_UID = "curriculum_admin";
const lessonData = {
    lessonId: "terminology-01",
    title: "What Is Movement?",
    chapterId: "chapter-1-foundations-of-movement-and-terminology",
    chapterTitle: "Foundations of Movement & Terminology",
    chapterNumber: 1,
    subchapterId: "subchapter-1-movement-terminology",
    subchapterTitle: "Movement Terminology & Anatomical Position",
    lessonIndex: 1,
    durationMinutes: 8,
    category: "MOVEMENT_PATTERNS",
    summary: "Define movement and explain why movement terminology matters in exercise and training",
    assetPath: "curriculum/chapter-1-foundations-of-movement-and-terminology/subchapter-1-movement-terminology/lesson-01-what-is-movement.md",
};

async function seedLesson() {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("curriculum_lessons").doc(LESSON_ID).set(lessonData);
  });
}

function adminDb() {
  return testEnv.authenticatedContext(ADMIN_UID, { admin: true }).firestore();
}

function lessonRef(db, id = LESSON_ID) {
  return db.collection("curriculum_lessons").doc(id);
}

function reviewRef(db, id = LESSON_ID) {
  return lessonRef(db, id).collection("review").doc("status");
}

function commentRef(db, id = "comment_1", lessonId = LESSON_ID) {
  return lessonRef(db, lessonId).collection("comments").doc(id);
}

function reviewPayload(overrides = {}) {
  return { status: "approved", updatedBy: ADMIN_UID, updatedAt: serverTimestamp(), ...overrides };
}

function commentPayload(overrides = {}) {
  return { authorId: ALICE_UID, text: "Please clarify this example.", createdAt: serverTimestamp(), ...overrides };
}

test("CurriculumIndex: only admins publish; signed-in users can read", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(lessonRef(aliceDb).set(lessonData));
  await assertSucceeds(lessonRef(adminDb()).set(lessonData));
  await assertFails(lessonRef(aliceDb).update({ title: "Untrusted edit" }));
  const snap = await assertSucceeds(lessonRef(aliceDb).get());
  if (!snap.exists) throw new Error("Lesson should exist in index");

  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("curriculum_lessons").doc("terminology-01").get());
});

test("CurriculumIndex: admin cannot embed review/comments or delete the lesson", async () => {
  const db = adminDb();
  await assertFails(lessonRef(db).set({ ...lessonData, status: "approved" }));
  await assertFails(lessonRef(db).set({ ...lessonData, comments: [] }));
  await assertSucceeds(lessonRef(db).set(lessonData));
  await assertFails(lessonRef(db).delete());
});

test("Review: admins can set all statuses; users can read but never write", async () => {
  await seedLesson();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  for (const status of ["approved", "rejected", "needs_improvement"]) {
    await assertSucceeds(reviewRef(adminDb()).set(reviewPayload({ status })));
    const snapshot = await assertSucceeds(reviewRef(aliceDb).get());
    assert.equal(snapshot.data().status, status);
    await assertFails(reviewRef(aliceDb).update({ status }));
    await assertFails(reviewRef(aliceDb).set(reviewPayload({ status, updatedBy: ALICE_UID })));
  }
  await assertFails(reviewRef(adminDb()).delete());
  await assertFails(reviewRef(testEnv.unauthenticatedContext().firestore()).get());
  await assertFails(reviewRef(testEnv.unauthenticatedContext().firestore()).set(reviewPayload()));
});

test("Review: reject invalid enum, types, extra/missing fields and forged audit metadata", async () => {
  await seedLesson();
  const reference = reviewRef(adminDb());
  for (const overrides of [
    { status: "needs improvement" }, { status: "published" }, { status: null }, { status: [] },
    { updatedBy: ALICE_UID }, { updatedAt: new Date(0) }, { updatedAt: "now" },
    { extra: true }, { admin: true },
  ]) await assertFails(reference.set(reviewPayload(overrides)));
  for (const field of ["status", "updatedBy", "updatedAt"]) {
    const payload = reviewPayload();
    delete payload[field];
    await assertFails(reference.set(payload));
  }
  await assertSucceeds(reference.set(reviewPayload()));
  await assertFails(reference.update({ updatedBy: deleteField() }));
  await assertFails(lessonRef(adminDb()).collection("review").doc("arbitrary").set(reviewPayload()));
  await assertFails(reviewRef(adminDb(), "missing-lesson").set(reviewPayload()));
});

test("Admin: absent/false/string/number/profile claims never grant admin access", async () => {
  await seedLesson();
  for (const claims of [{}, { admin: false }, { admin: "true" }, { admin: 1 }, { role: "admin" }]) {
    const db = testEnv.authenticatedContext(ALICE_UID, claims).firestore();
    await assertFails(reviewRef(db).set(reviewPayload({ updatedBy: ALICE_UID })));
    await assertFails(lessonRef(db).update({ title: "Privilege escalation" }));
  }
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set({ ...validAliceProfile, admin: true }));
  await assertFails(reviewRef(aliceDb).set(reviewPayload({ updatedBy: ALICE_UID })));
});

test("Comments: author and admin read/delete; other users and guests denied", async () => {
  await seedLesson();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const guestDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(commentRef(aliceDb).set(commentPayload()));
  await assertSucceeds(commentRef(aliceDb).get());
  await assertSucceeds(commentRef(adminDb()).get());
  for (const db of [bobDb, guestDb]) {
    await assertFails(commentRef(db).get());
    await assertFails(commentRef(db).delete());
    await assertFails(commentRef(db).set(commentPayload()));
  }
  await assertFails(commentRef(guestDb, "guest").set(commentPayload()));
  await assertSucceeds(commentRef(aliceDb).delete());
  await assertSucceeds(commentRef(aliceDb).set(commentPayload()));
  await assertSucceeds(commentRef(adminDb()).delete());
});

test("Comments: private queries require author constraint; admins can query all", async () => {
  await seedLesson();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(commentRef(aliceDb).set(commentPayload()));
  await assertSucceeds(commentRef(bobDb, "bob").set(commentPayload({ authorId: BOB_UID })));
  const collection = lessonRef(aliceDb).collection("comments");
  await assertFails(collection.get());
  await assertFails(collection.where("authorId", "==", BOB_UID).get());
  const own = await assertSucceeds(collection.where("authorId", "==", ALICE_UID).limit(50).get());
  assert.equal(own.size, 1);
  const all = await assertSucceeds(lessonRef(adminDb()).collection("comments").limit(50).get());
  assert.equal(all.size, 2);
  await assertFails(lessonRef(testEnv.unauthenticatedContext().firestore()).collection("comments").get());
});

test("Comments: reject impersonation, unknown fields, malformed text and forged timestamps", async () => {
  await seedLesson();
  const db = testEnv.authenticatedContext(ALICE_UID).firestore();
  for (const overrides of [
    { authorId: BOB_UID }, { text: "" }, { text: " \n\t\r " }, { text: "x".repeat(2001) },
    { text: null }, { text: {} }, { text: 123 }, { status: "approved" }, { admin: true },
    { createdAt: new Date(0) }, { createdAt: new Date(Date.now() + 60000) }, { createdAt: "now" },
  ]) await assertFails(commentRef(db).set(commentPayload(overrides)));
  for (const field of ["authorId", "text", "createdAt"]) {
    const payload = commentPayload();
    delete payload[field];
    await assertFails(commentRef(db).set(payload));
  }
  await assertFails(commentRef(db, "orphan", "missing-lesson").set(commentPayload()));
  await assertFails(commentRef(db).collection("arbitrary").doc("payload").set({ text: "nested write" }));
});

test("Comments: immutable even for author/admin; cannot modify identity or audit fields", async () => {
  await seedLesson();
  const db = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(commentRef(db).set(commentPayload()));
  for (const actor of [db, adminDb()]) {
    await assertFails(commentRef(actor).update({ text: "Edited" }));
    await assertFails(commentRef(actor).update({ authorId: BOB_UID }));
    await assertFails(commentRef(actor).update({ createdAt: serverTimestamp() }));
    await assertFails(commentRef(actor).set(commentPayload()));
  }
});

test("Comments: bounded Unicode/multiline and injection-looking input remain literal data", async () => {
  await seedLesson();
  const db = testEnv.authenticatedContext(ALICE_UID).firestore();
  const payloads = [
    "x", "x".repeat(2000), "Anatomie: 肌肉 💪\nPlease explain this example.",
    "<script>alert('x')</script>", "'; DROP TABLE users; --", "${process.env.SECRET}",
    "[link](javascript:alert(1))", "../../users/bob_456", '{"admin":true,"status":"approved"}',
  ];
  for (const [index, text] of payloads.entries()) {
    const reference = commentRef(db, `literal_${index}`);
    await assertSucceeds(reference.set(commentPayload({ text })));
    const snapshot = await assertSucceeds(reference.get());
    assert.equal(snapshot.data().text, text);
  }
  await assertFails(lessonRef(db).update({ title: "Text did not grant access" }));
  await assertFails(reviewRef(db).set(reviewPayload({ updatedBy: ALICE_UID })));
});

test("LessonProgress: Alice can track completed lessons and Bob cannot modify", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const progressData = {
    lessonId: "terminology-01",
    userId: ALICE_UID,
    completedAt: new Date(),
  };
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("lesson_progress").doc("terminology-01").set(progressData)
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("lesson_progress").doc("terminology-01").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("lesson_progress").doc("terminology-01").set(progressData)
  );
});

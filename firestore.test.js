const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

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

test("CurriculumIndex: Alice can index and read curriculum lessons", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
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
  await assertSucceeds(aliceDb.collection("curriculum_lessons").doc("terminology-01").set(lessonData));
  const snap = await assertSucceeds(aliceDb.collection("curriculum_lessons").doc("terminology-01").get());
  if (!snap.exists) throw new Error("Lesson should exist in index");

  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("curriculum_lessons").doc("terminology-01").get());
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

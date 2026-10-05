package com.example.physiapp.data.repository

import com.example.physiapp.data.model.ExerciseDef
import com.example.physiapp.data.model.MovementPattern
import com.example.physiapp.data.model.WorkoutTemplate

object ExerciseRepository {

    private val _exercises: MutableList<ExerciseDef> = mutableListOf(
        // SQUAT PATTERN
        ExerciseDef(
            id = "sq-1",
            name = "Goblet Squat",
            pattern = MovementPattern.SQUAT,
            primaryMuscles = "Quadriceps, Gluteus Maximus, Core Stabilizers",
            equipment = "Kettlebell or Dumbbell",
            coachingCues = listOf(
                "Hold weight tight against sternum with elbows tucked inside knees",
                "Root feet with tripod pressure (big toe, pinky toe, heel)",
                "Spread the floor with your feet to engage external hip rotators",
                "Descend between your hips keeping your chest proud"
            ),
            commonMistakes = listOf(
                "Letting elbows flare out or weight drift away from chest",
                "Heels lifting off the ground due to limited dorsiflexion",
                "Lumbar rounding ('butt wink') at bottom of range"
            ),
            regression = "Assisted TRX Squat or Box Squat to parallel bench",
            progression = "Front Squat with barbell or Zercher Squat",
            gifUrl = "https://v2.exercisedb.io/image/9Z1c0UHz0YmG8C"
        ),
        ExerciseDef(
            id = "sq-2",
            name = "Bulgarian Split Squat",
            pattern = MovementPattern.SQUAT,
            primaryMuscles = "Quadriceps, Gluteus Medius, Adductor Magnus",
            equipment = "Bench & Dumbbells",
            coachingCues = listOf(
                "Rest rear laces on a bench roughly knee height",
                "Slight forward torso pitch to align with rear femur",
                "Lower until rear knee hovers an inch above the floor",
                "Drive through front midfoot to stand"
            ),
            commonMistakes = listOf(
                "Stance too narrow causing pelvic rotation",
                "Hyperextending lower back instead of hinging slightly at hip"
            ),
            regression = "Split squat with both feet on floor",
            progression = "Deficit Bulgarian split squat or barbell loading",
            gifUrl = "https://v2.exercisedb.io/image/5xL1Zq8V2kNm0R"
        ),

        // HINGE PATTERN
        ExerciseDef(
            id = "hg-1",
            name = "Romanian Deadlift (RDL)",
            pattern = MovementPattern.HINGE,
            primaryMuscles = "Hamstrings, Gluteus Maximus, Erector Spinae, Lats",
            equipment = "Barbell or Pair of Dumbbells",
            coachingCues = listOf(
                "Soft unlock at knees, then freeze knee angle in place",
                "Push your hips straight back toward the wall behind you",
                "Keep dumbbells sliding along the thighs and shins",
                "Feel a deep stretch in hamstrings, then squeeze glutes to stand tall"
            ),
            commonMistakes = listOf(
                "Turning the hinge into a squat by bending knees forward",
                "Letting weights drift away from body, increasing lower back shear",
                "Hyperextending spine at the top of the lift"
            ),
            regression = "Wall-touch hip hinge or Cable pull-through",
            progression = "Single-leg Romanian Deadlift",
            gifUrl = "https://v2.exercisedb.io/image/YVfB9RkLz5uL6Z"
        ),
        ExerciseDef(
            id = "hg-2",
            name = "Kettlebell Swing",
            pattern = MovementPattern.HINGE,
            primaryMuscles = "Glutes, Hamstrings, Core, Latissimus Dorsi",
            equipment = "Kettlebell",
            coachingCues = listOf(
                "Hike bell deep into the zipper area with braced lats",
                "Violently snap hips forward to stand plank-tall",
                "The bell floats from hip momentum, not an arm shoulder raise",
                "Wait until forearms touch ribs before hinging back"
            ),
            commonMistakes = listOf(
                "Squatting the bell instead of hinging",
                "Lifting bell with shoulders rather than hip snap"
            ),
            regression = "Banded kettlebell deadlift",
            progression = "Single-arm kettlebell swing",
            gifUrl = "https://v2.exercisedb.io/image/P4qN7YwL1oZ8rV"
        ),

        // PUSH PATTERN
        ExerciseDef(
            id = "ps-1",
            name = "Dumbbell Flat Bench Press",
            pattern = MovementPattern.PUSH,
            primaryMuscles = "Pectoralis Major, Anterior Deltoid, Triceps Brachii",
            equipment = "Bench & Dumbbells",
            coachingCues = listOf(
                "Retract and depress scapulae into the bench for stable shoulder base",
                "Elbows at roughly 45-degree angle to torso (arrow shape, not T-shape)",
                "Drive feet into the floor to maintain tension",
                "Press up and slightly inward in a smooth arc"
            ),
            commonMistakes = listOf(
                "Elbows flared out at 90 degrees stressing anterior shoulder capsule",
                "Lifting hips off the bench",
                "Bouncing weights or losing shoulder retraction"
            ),
            regression = "Floor Press with dumbbells (limits shoulder extension range)",
            progression = "Barbell bench press or incline press",
            gifUrl = "https://v2.exercisedb.io/image/LzNqE20F7UqT4D"
        ),
        ExerciseDef(
            id = "ps-2",
            name = "Standing Overhead Dumbbell Press",
            pattern = MovementPattern.PUSH,
            primaryMuscles = "Deltoids, Upper Trapezius, Triceps, Core",
            equipment = "Dumbbells",
            coachingCues = listOf(
                "Lock glutes and pull ribcage down toward pelvis (no swayback)",
                "Press dumbbells directly overhead until biceps frame the ears",
                "Allow scapulae to upwardly rotate freely",
                "Lower with control to clavicle level"
            ),
            commonMistakes = listOf(
                "Arching lower back when shoulder flexion is restricted",
                "Pressing load forward instead of straight vertically"
            ),
            regression = "Half-kneeling landmine press",
            progression = "Standing strict barbell military press",
            gifUrl = "https://v2.exercisedb.io/image/M5qR8vN1yL4zX2"
        ),

        // PULL PATTERN
        ExerciseDef(
            id = "pl-1",
            name = "Single-Arm Dumbbell Row",
            pattern = MovementPattern.PULL,
            primaryMuscles = "Latissimus Dorsi, Rhomboids, Biceps Brachii, Posterior Deltoid",
            equipment = "Dumbbell & Bench",
            coachingCues = listOf(
                "Flat back parallel or 30 degrees to the ground with firm hand support",
                "Initiate pull by retracting shoulder blade toward the spine",
                "Pull elbow toward the hip pocket, not up to the ceiling",
                "Control the eccentric descent, feeling a gentle stretch in the lat"
            ),
            commonMistakes = listOf(
                "Yanking weight with torso rotation rather than back muscles",
                "Shrugging shoulder up toward the ear"
            ),
            regression = "Incline chest-supported row",
            progression = "Bent-over barbell row or meadow row",
            gifUrl = "https://v2.exercisedb.io/image/Qc1L8BvY5wR3xZ"
        ),
        ExerciseDef(
            id = "pl-2",
            name = "Cable Face Pull",
            pattern = MovementPattern.PULL,
            primaryMuscles = "Rear Deltoids, Infraspinatus, Teres Minor, Mid/Lower Trapezius",
            equipment = "Cable Machine with Rope Attachment",
            coachingCues = listOf(
                "Set pulley at eye level with thumbs facing backward",
                "Pull the center of the rope toward your bridge of nose",
                "Externally rotate hands so knuckles finish behind ears ('double bicep pose')",
                "Pause for 1 second of peak scapular retraction"
            ),
            commonMistakes = listOf(
                "Using too much weight and leaning torso backward to compensate",
                "Failing to achieve external rotation at finish"
            ),
            regression = "Band pull-apart or prone Y-raise",
            progression = "Heavier cable load or rings face pull",
            gifUrl = "https://v2.exercisedb.io/image/F7yT1wL8rV4mZ2"
        ),

        // CARRY PATTERN
        ExerciseDef(
            id = "cr-1",
            name = "Farmer's Walk",
            pattern = MovementPattern.CARRY,
            primaryMuscles = "Forearm flexors (grip), Trapezius, Quadratus Lumborum, Gluteus Medius",
            equipment = "Two Heavy Dumbbells or Kettlebells",
            coachingCues = listOf(
                "Deadlift weights up safely with neutral spine",
                "Stand tall with chest wide and shoulder blades packed down",
                "Take smooth, heel-to-toe marching strides without swaying side to side",
                "Maintain active diaphragmatic breathing under load"
            ),
            commonMistakes = listOf(
                "Slouching shoulders forward or letting weights swing",
                "Rushing steps with erratic balance"
            ),
            regression = "Lighter dumbbells or static isometric carry hold",
            progression = "Trap bar carry or offset weight carry",
            gifUrl = "https://v2.exercisedb.io/image/P4qN7YwL1oZ8rV"
        ),
        ExerciseDef(
            id = "cr-2",
            name = "Suitcase Carry",
            pattern = MovementPattern.CARRY,
            primaryMuscles = "Contralateral Quadratus Lumborum, Obliques, Gluteus Medius",
            equipment = "Single Heavy Dumbbell or Kettlebell",
            coachingCues = listOf(
                "Hold weight in one hand only; do not let weight rest against thigh",
                "Keep shoulders and pelvis perfectly level (anti-lateral flexion)",
                "Walk slowly with controlled cadence",
                "Switch sides after designated distance or time"
            ),
            commonMistakes = listOf(
                "Tilting torso toward or away from the loaded side",
                "Shrugging one shoulder higher than the other"
            ),
            regression = "Lighter load with focus on vertical posture",
            progression = "Heavy kettlebell or sandbag suitcase carry",
            gifUrl = "https://v2.exercisedb.io/image/S9kM2vR7pL1wX5"
        ),

        // ROTATION / ANTI-ROTATION PATTERN
        ExerciseDef(
            id = "rt-1",
            name = "Pallof Press",
            pattern = MovementPattern.ROTATION,
            primaryMuscles = "Transverse Abdominis, Internal/External Obliques, Glutes",
            equipment = "Cable Column or Resistance Band",
            coachingCues = listOf(
                "Stand perpendicular to cable anchor with feet shoulder-width",
                "Hold handle at center of chest with both hands",
                "Press arms straight out in front without letting torso rotate toward anchor",
                "Hold extended position for 2 seconds before returning to chest"
            ),
            commonMistakes = listOf(
                "Rotating torso or hips toward the machine",
                "Holding breath instead of bracing with diaphragmatic breaths"
            ),
            regression = "Half-kneeling Pallof press or lighter band",
            progression = "Pallof press with overhead raise or lateral walkout",
            gifUrl = "https://v2.exercisedb.io/image/K3wT9PzR4mL7sB"
        ),
        ExerciseDef(
            id = "rt-2",
            name = "Bird Dog (Cross-Body Anti-Rotation)",
            pattern = MovementPattern.ROTATION,
            primaryMuscles = "Multifidus, Erector Spinae, Gluteus Maximus, Anterior Deltoid",
            equipment = "Exercise Mat",
            coachingCues = listOf(
                "Start on hands and knees with wrists under shoulders, knees under hips",
                "Extend opposite arm and leg simultaneously until parallel to floor",
                "Imagine balancing a cup of water on your sacrum (no pelvic tilt or twist)",
                "Hold for 2 seconds at top, then return smoothly"
            ),
            commonMistakes = listOf(
                "Hyperextending lumbar spine or lifting leg too high",
                "Allowing hips to drop or rotate toward the floor"
            ),
            regression = "Leg extension only, hands remaining on floor",
            progression = "Bird dog with resistance band or isometric square holds",
            gifUrl = "https://v2.exercisedb.io/image/W4rN8yL2mZ9vQ1"
        )
    )

    val templates: List<WorkoutTemplate> = listOf(
        WorkoutTemplate(
            id = "tmpl-fullbody-a",
            title = "Full Body Foundations A",
            subtitle = "Knee dominance, horizontal push & pull, loaded locomotion",
            estimatedDurationMinutes = 45,
            patternsCovered = listOf(MovementPattern.SQUAT, MovementPattern.PUSH, MovementPattern.PULL, MovementPattern.CARRY),
            defaultExerciseIds = listOf("sq-1", "ps-1", "pl-1", "cr-1"),
            physioFocus = "Focus on hip clearance, stable tripod feet, and scapular depression under load."
        ),
        WorkoutTemplate(
            id = "tmpl-fullbody-b",
            title = "Full Body Foundations B",
            subtitle = "Posterior chain hinge, vertical push & pull, anti-rotation core",
            estimatedDurationMinutes = 45,
            patternsCovered = listOf(MovementPattern.HINGE, MovementPattern.PUSH, MovementPattern.PULL, MovementPattern.ROTATION),
            defaultExerciseIds = listOf("hg-1", "ps-2", "pl-2", "rt-1"),
            physioFocus = "Master the hip hinge without lumbar shear; stabilize obliques against rotational torque."
        ),
        WorkoutTemplate(
            id = "tmpl-upper",
            title = "Upper Body Kinetic Chain",
            subtitle = "Balanced push, pull, rotator cuff integrity, and anti-lateral carry",
            estimatedDurationMinutes = 40,
            patternsCovered = listOf(MovementPattern.PUSH, MovementPattern.PULL, MovementPattern.CARRY),
            defaultExerciseIds = listOf("ps-1", "pl-1", "ps-2", "pl-2", "cr-2"),
            physioFocus = "Scapulohumeral rhythm and glenohumeral stability."
        ),
        WorkoutTemplate(
            id = "tmpl-lower",
            title = "Lower Body Resilience",
            subtitle = "Squat mechanics, posterior chain hinge, and unilateral pelvic control",
            estimatedDurationMinutes = 40,
            patternsCovered = listOf(MovementPattern.SQUAT, MovementPattern.HINGE, MovementPattern.ROTATION),
            defaultExerciseIds = listOf("sq-1", "hg-1", "sq-2", "rt-2"),
            physioFocus = "Knee alignment over second toe and glute medius stability."
        ),
        WorkoutTemplate(
            id = "tmpl-deload",
            title = "Deload & Active Recovery Session",
            subtitle = "50% volume reduction, RPE 5-6, joint circulation & tendon remodeling",
            estimatedDurationMinutes = 30,
            patternsCovered = listOf(MovementPattern.SQUAT, MovementPattern.HINGE, MovementPattern.PULL, MovementPattern.ROTATION),
            defaultExerciseIds = listOf("sq-1", "hg-1", "pl-2", "rt-2"),
            isDeloadTemplate = true,
            physioFocus = "Promote synovial fluid circulation, preserve motor patterns, and accelerate tissue recovery."
        )
    )

    val exercises: List<ExerciseDef> get() = _exercises

    fun registerExercise(def: ExerciseDef) {
        if (_exercises.none { it.id == def.id }) {
            _exercises.add(def)
        }
    }

    fun getExerciseById(id: String): ExerciseDef? {
        return exercises.find { it.id == id }
    }

    fun getExercisesForPattern(pattern: MovementPattern): List<ExerciseDef> {
        return exercises.filter { it.pattern == pattern }
    }
}

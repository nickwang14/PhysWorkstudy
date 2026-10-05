package com.example.physiapp.data.repository

import com.example.physiapp.data.model.CurriculumChapter
import com.example.physiapp.data.model.CurriculumLesson
import com.example.physiapp.data.model.CurriculumSubchapter
import com.example.physiapp.data.model.KnowledgeCheckOption
import com.example.physiapp.data.model.KnowledgeCheckQuestion
import com.example.physiapp.data.model.TopicCategory

object CurriculumRepository {

    val chapters: List<CurriculumChapter> by lazy {
        listOf(
            createChapter1(),
            createChapter2(),
            createChapter3(),
            createChapter4(),
            createChapter5()
        )
    }

    val allLessons: List<CurriculumLesson> by lazy {
        chapters.flatMap { ch -> ch.subchapters.flatMap { sub -> sub.lessons } }
    }

    fun getLessonById(id: String): CurriculumLesson? {
        return allLessons.find { it.id == id }
    }

    fun getNextLesson(currentId: String): CurriculumLesson? {
        val index = allLessons.indexOfFirst { it.id == currentId }
        return if (index in 0 until allLessons.size - 1) allLessons[index + 1] else null
    }

    private fun createChapter1(): CurriculumChapter {
        return CurriculumChapter(
            id = "ch-1",
            number = 1,
            title = "Foundations of Movement & Terminology",
            description = "Master the universal language of human movement, anatomical reference planes, joint motions, and foundational movement patterns.",
            category = TopicCategory.MOVEMENT_PATTERNS,
            subchapters = listOf(
                CurriculumSubchapter(
                    id = "sub-1-1",
                    chapterId = "ch-1",
                    title = "Movement Terminology & Anatomical Position",
                    description = "Core language for joint actions and standard anatomical orientation.",
                    lessons = listOf(
                        CurriculumLesson(
                            id = "lesson-1-1-1",
                            chapterId = "ch-1",
                            subchapterId = "sub-1-1",
                            lessonIndex = 1,
                            title = "What Is Movement?",
                            subtitle = "Displacement, segment velocity, and physical context",
                            durationMinutes = 5,
                            category = TopicCategory.MOVEMENT_PATTERNS,
                            summary = "Movement is the change in position or posture of a body segment over time. Terminology connects anatomy to purposeful exercise selection.",
                            fullMarkdownText = """
                                # What Is Human Movement?
                                
                                Movement is defined as the change in position or posture of a body segment over time relative to a frame of reference. In human movement science and physiotherapy, motion is rarely evaluated in isolation; it is analyzed through the coordination of joints, muscles, connective tissues, and neural control systems.
                                
                                ### Why Accurate Terminology Matters
                                Rather than classifying exercises by gym equipment or subjective muscle burns, clinical terminology allows practitioners and athletes to evaluate:
                                1. Which anatomical axis the joint rotates around
                                2. Which muscular system acts as prime mover (agonist) versus stabilizer
                                3. The torque and lever arm generated against external resistance
                                
                                When movement is communicated accurately, training decisions become repeatable, explainable, and adaptable to individual joint anatomy.
                            """.trimIndent(),
                            keyTerms = listOf("Kinematics", "Segment displacement", "Joint axis", "Agonist", "Postural control"),
                            practicalApplication = "Stand with feet shoulder-width apart. Slowly raise your right arm straight in front to shoulder height, then out to the side. Notice how your shoulder blade (scapula) and clavicle must adjust their positions to accommodate the movement.",
                            questions = listOf(
                                KnowledgeCheckQuestion(
                                    id = "q-1-1-1-1",
                                    questionText = "Which statement best defines human movement in biomechanics?",
                                    options = listOf(
                                        KnowledgeCheckOption("opt-1", "A random series of strenuous muscle contractions", false, "Movement requires coordinate spatial displacement, not merely muscular fatigue."),
                                        KnowledgeCheckOption("opt-2", "A change in position or posture of a body segment over time", true, "Correct! Movement is the continuous displacement of body segments relative to a coordinate frame."),
                                        KnowledgeCheckOption("opt-3", "Any activity performed while heart rate is elevated", false, "Cardiovascular metrics describe systemic response, not mechanical movement."),
                                        KnowledgeCheckOption("opt-4", "The maximum weight a person can lift once", false, "That defines 1-rep maximum load, not movement itself.")
                                    )
                                ),
                                KnowledgeCheckQuestion(
                                    id = "q-1-1-1-2",
                                    questionText = "Why is movement terminology prioritized over generic gym names?",
                                    options = listOf(
                                        KnowledgeCheckOption("opt-1", "It sounds more academic and impressive", false, "The goal is practical diagnostic clarity, not pretension."),
                                        KnowledgeCheckOption("opt-2", "It allows clinicians and lifters to diagnose joint actions, levers, and stabilizers accurately", true, "Accurate terms clarify torque, plane of motion, and load distribution on specific tissues."),
                                        KnowledgeCheckOption("opt-3", "It replaces the need to understand muscular anatomy", false, "Movement terminology directly builds on anatomy.")
                                    )
                                )
                            )
                        ),
                        CurriculumLesson(
                            id = "lesson-1-1-2",
                            chapterId = "ch-1",
                            subchapterId = "sub-1-1",
                            lessonIndex = 2,
                            title = "Planes of Motion & Mechanical Axes",
                            subtitle = "Sagittal, frontal, and transverse navigation",
                            durationMinutes = 6,
                            category = TopicCategory.BIOMECHANICS,
                            summary = "The human body moves through three cardinal planes: sagittal, frontal, and transverse, each rotating perpendicular to a primary axis.",
                            fullMarkdownText = """
                                # The Three Cardinal Planes of Motion
                                
                                To systematically analyze any human exercise, we project body movement onto three perpendicular cardinal planes:
                                
                                1. **Sagittal Plane**: Bisects the body into left and right halves. Motions include **flexion** (decreasing joint angle) and **extension** (increasing joint angle). Examples: Squats, lunges, biceps curls, running forward.
                                
                                2. **Frontal (Coronal) Plane**: Bisects the body into anterior (front) and posterior (back) halves. Motions include **abduction** (away from midline), **adduction** (toward midline), and lateral flexion. Examples: Lateral lunges, jumping jacks, side planks.
                                
                                3. **Transverse (Horizontal) Plane**: Bisects the body into superior (upper) and inferior (lower) portions. Motions include **internal rotation**, **external rotation**, and horizontal abduction/adduction. Examples: Throwing, swinging a bat, cable torso rotations.
                                
                                Most athletic movements are multi-planar, but breaking them down into primary plane components prevents injury and builds balanced joint durability.
                            """.trimIndent(),
                            keyTerms = listOf("Sagittal plane", "Frontal plane", "Transverse plane", "Flexion / Extension", "Abduction / Adduction"),
                            practicalApplication = "Perform 5 bodyweight squats. Look at yourself from the side: you will observe the hips, knees, and ankles flexing and extending predominantly within the sagittal plane.",
                            questions = listOf(
                                KnowledgeCheckQuestion(
                                    id = "q-1-1-2-1",
                                    questionText = "In which cardinal plane do standard barbell back squats primarily occur?",
                                    options = listOf(
                                        KnowledgeCheckOption("opt-1", "Sagittal plane", true, "Squats consist primarily of hip, knee, and ankle flexion and extension, which define sagittal plane motion."),
                                        KnowledgeCheckOption("opt-2", "Frontal plane", false, "Frontal plane motion involves side-to-side movements like lateral raises and side lunges."),
                                        KnowledgeCheckOption("opt-3", "Transverse plane", false, "Transverse plane movements involve rotational twisting along the vertical axis.")
                                    )
                                )
                            )
                        )
                    )
                ),
                CurriculumSubchapter(
                    id = "sub-1-2",
                    chapterId = "ch-1",
                    title = "The 6 Fundamental Movement Patterns",
                    description = "Squat, Hinge, Push, Pull, Carry, and Rotation/Anti-Rotation.",
                    lessons = listOf(
                        CurriculumLesson(
                            id = "lesson-1-2-1",
                            chapterId = "ch-1",
                            subchapterId = "sub-1-2",
                            lessonIndex = 3,
                            title = "Pattern Overview: The Six Pillars",
                            subtitle = "Organizing strength around neuromuscular motor programs",
                            durationMinutes = 6,
                            category = TopicCategory.MOVEMENT_PATTERNS,
                            summary = "Rather than memorizing thousands of separate lifts, physiotherapy organizes human resistance training into six foundational patterns.",
                            fullMarkdownText = """
                                # The Six Fundamental Movement Patterns
                                
                                Human motor control relies on functional patterns evolved for survival and locomotion. By organizing training programs around these six patterns, we ensure structural balance, prevent repetitive strain, and safeguard joint longevity:
                                
                                1. **Squat**: Knee-dominant, vertical displacement with deep bilateral or unilateral knee and hip flexion with an upright torso.
                                2. **Hinge**: Hip-dominant, posterior chain recruitment (glutes, hamstrings, erector spinae) with maximal hip bend and minimal knee flexion.
                                3. **Push**: Moving external load away from torso (bench press, overhead press, push-up).
                                4. **Pull**: Drawing external load toward torso (rows, pull-ups, face pulls).
                                5. **Carry**: Loaded locomotion challenging spinal stiffness, pelvic levelness, and grip strength.
                                6. **Rotation / Anti-Rotation**: Generating or resisting rotational torque through the core and oblique sling systems.
                            """.trimIndent(),
                            keyTerms = listOf("Movement pattern", "Motor program", "Posterior chain", "Spinal stiffness", "Anti-rotation"),
                            practicalApplication = "Audit your current routine: write down your last 5 exercises and categorize each into one of the 6 fundamental patterns. Is there a glaring deficiency in pulls or carries?",
                            questions = listOf(
                                KnowledgeCheckQuestion(
                                    id = "q-1-2-1-1",
                                    questionText = "What distinguishes a Hinge pattern from a Squat pattern?",
                                    options = listOf(
                                        KnowledgeCheckOption("opt-1", "Hinge is hip-dominant with minimal knee bend; squat is knee-dominant with greater knee flexion", true, "Exactly! The hinge shifts load to the posterior chain (glutes/hamstrings) via hip displacement."),
                                        KnowledgeCheckOption("opt-2", "Hinge uses light dumbbells while squats must use barbells", false, "Patterns describe biomechanical joint action regardless of implement."),
                                        KnowledgeCheckOption("opt-3", "Hinge only trains upper back muscles", false, "Hinges target the gluteus maximus, hamstrings, and posterior kinetic chain.")
                                    )
                                )
                            )
                        ),
                        CurriculumLesson(
                            id = "lesson-1-2-2",
                            chapterId = "ch-1",
                            subchapterId = "sub-1-2",
                            lessonIndex = 4,
                            title = "The Squat Pattern Deep Dive",
                            subtitle = "Femur kinematics, ankle dorsiflexion, and lumbar neutral",
                            durationMinutes = 7,
                            category = TopicCategory.MOVEMENT_PATTERNS,
                            summary = "Unpack the mechanics of the squat: why ankle mobility governs torso angle, how pelvic orientation affects hip socket clearance, and optimal cueing.",
                            fullMarkdownText = """
                                # The Mechanics of the Squat Pattern
                                
                                The squat is a bilateral or unilateral closed-kinetic-chain movement requiring simultaneous flexion of hips, knees, and ankles.
                                
                                ### The Triad of Joint Mechanics:
                                - **Ankle Talocrural Joint**: Requires sufficient **dorsiflexion** (knees traveling forward over toes). Insufficient dorsiflexion forces excessive forward torso lean, increasing spinal shear stress.
                                - **Femoroacetabular Joint (Hip)**: Hip anatomy varies dramatically between individuals (deep versus shallow acetabulum, anteversion versus retroversion of femoral neck). There is no single universal foot stance width.
                                - **Lumbar Spine**: Must maintain relative stiffness and avoid end-range spinal flexion (the infamous 'butt wink') under axial compression.
                                
                                ### Physio Coaching Cue:
                                "Root the tripod of the foot (heel, first metatarsal head, fifth metatarsal head), screw your feet into the floor to generate external rotation torque at the hip, and descend between your knees."
                            """.trimIndent(),
                            keyTerms = listOf("Dorsiflexion", "Closed kinetic chain", "Acetabulum", "Lumbar shear", "Foot tripod"),
                            practicalApplication = "Perform the 5-inch wall test for ankle mobility: place big toe 5 inches from a wall and see if knee can touch the wall without heel lifting off the floor.",
                            questions = listOf(
                                KnowledgeCheckQuestion(
                                    id = "q-1-2-2-1",
                                    questionText = "What typically occurs when a lifter lacks adequate ankle dorsiflexion during a squat?",
                                    options = listOf(
                                        KnowledgeCheckOption("opt-1", "The torso must pitch forward excessively to keep center of mass over midfoot", true, "Correct. When knees cannot travel forward, the hips must push further back, increasing forward torso incline."),
                                        KnowledgeCheckOption("opt-2", "The lifter immediately falls forward onto their face", false, "The body compensates by flexing the hips and lower back."),
                                        KnowledgeCheckOption("opt-3", "The knees flex more easily without any hip movement", false, "Knee flexion is actually constrained by restricted dorsiflexion.")
                                    )
                                )
                            )
                        ),
                        CurriculumLesson(
                            id = "lesson-1-2-3",
                            chapterId = "ch-1",
                            subchapterId = "sub-1-2",
                            lessonIndex = 5,
                            title = "The Hinge Pattern & Posterior Chain",
                            subtitle = "Romanian deadlifts, glute recruitment, and pelvic tilt",
                            durationMinutes = 6,
                            category = TopicCategory.MOVEMENT_PATTERNS,
                            summary = "The hinge is the cornerstone of posterior chain power and back resilience. Learn hip-hinging without lumbar compensation.",
                            fullMarkdownText = """
                                # Mastering the Hip Hinge
                                
                                The hip hinge is a movement where the pelvis rotates around the femoral heads while maintaining a neutral, braced spine. 
                                
                                ### Key Differences:
                                In a squat, the hips travel down and forward/back simultaneously. In a pure hinge (e.g. Romanian Deadlift, Kettlebell Swing), the hips push straight back toward a rear wall, while shins remain nearly vertical.
                                
                                ### Protecting the Lumbar Spine:
                                By keeping the external load (barbell, kettlebell, or dumbbell) close to the body's center of gravity, the moment arm on the lumbar spine is minimized, transferring the tensile work to the powerful gluteus maximus and hamstrings.
                            """.trimIndent(),
                            keyTerms = listOf("Hip hinge", "Posterior chain", "Moment arm", "Pelvic anterior tilt", "Gluteus maximus"),
                            practicalApplication = "Stand 6 inches in front of a wall with hands in hip creases. Push your hips backward until your buttocks touch the wall without squatting down.",
                            questions = listOf(
                                KnowledgeCheckQuestion(
                                    id = "q-1-2-3-1",
                                    questionText = "During a Romanian Deadlift, what should the shins do?",
                                    options = listOf(
                                        KnowledgeCheckOption("opt-1", "Remain relatively vertical with minimal forward knee travel", true, "Correct! Keeping shins vertical emphasizes hip extension and posterior chain loading."),
                                        KnowledgeCheckOption("opt-2", "Travel far forward over the toes like a deep squat", false, "That shifts the movement into a quad-dominant squat pattern."),
                                        KnowledgeCheckOption("opt-3", "Rotate inward toward each other", false, "Internal knee collapse is a fault known as dynamic knee valgus.")
                                    )
                                )
                            ),
                            isGateMilestone = true
                        )
                    )
                )
            )
        )
    }

    private fun createChapter2(): CurriculumChapter {
        return CurriculumChapter(
            id = "ch-2",
            number = 2,
            title = "Anatomy & Physiology of Movement",
            description = "Explore connective tissues, fascia, bones as mechanical levers, muscle contraction mechanics, and joint trade-offs.",
            category = TopicCategory.ANATOMY_PHYSIOLOGY,
            subchapters = listOf(
                CurriculumSubchapter(
                    id = "sub-2-1",
                    chapterId = "ch-2",
                    title = "Musculoskeletal Systems & Levers",
                    description = "Mechanical advantage, lever classes, and tendon mechanics.",
                    lessons = listOf(
                        CurriculumLesson(
                            id = "lesson-2-1-1",
                            chapterId = "ch-2",
                            subchapterId = "sub-2-1",
                            lessonIndex = 6,
                            title = "Bones as Levers & Joint Fulcrums",
                            subtitle = "1st, 2nd, and 3rd class anatomical levers",
                            durationMinutes = 6,
                            category = TopicCategory.ANATOMY_PHYSIOLOGY,
                            summary = "The human skeleton functions as an interconnected system of rigid levers (bones) pivoting around axes (joints) powered by muscle tension.",
                            fullMarkdownText = """
                                # Anatomical Levers in Human Movement
                                
                                In biomechanics, a lever is a rigid rod that turns about a pivot (fulcrum). Most levers in the human body are **Third-Class Levers**, where the muscular effort is applied between the fulcrum and the load.
                                
                                - **1st Class Lever**: Fulcrum between Effort and Load (e.g. Atlanto-occipital joint of the neck, nodding head).
                                - **2nd Class Lever**: Load between Fulcrum and Effort (e.g. Calf raise standing on balls of feet; gives huge mechanical advantage).
                                - **3rd Class Lever**: Effort between Fulcrum and Load (e.g. Biceps brachii flexing the elbow). While 3rd class levers have a mechanical disadvantage in force production, they maximize velocity and range of motion!
                            """.trimIndent(),
                            keyTerms = listOf("Fulcrum", "Effort arm", "Resistance arm", "Mechanical advantage", "Third-class lever"),
                            practicalApplication = "Hold a 10 lb weight close to your shoulder versus at arm's length. Feel how dramatically heavier it feels when the resistance arm lengthens.",
                            questions = listOf(
                                KnowledgeCheckQuestion(
                                    id = "q-2-1-1-1",
                                    questionText = "What is the primary evolutionary advantage of human joints being primarily 3rd-class levers?",
                                    options = listOf(
                                        KnowledgeCheckOption("opt-1", "They maximize limb velocity and distance traveled at the extremity", true, "Correct! Small muscle shortenings produce large, rapid movements at the hand or foot."),
                                        KnowledgeCheckOption("opt-2", "They allow humans to lift 10x their bodyweight effortlessly", false, "3rd class levers actually operate at a mechanical force disadvantage."),
                                        KnowledgeCheckOption("opt-3", "They completely eliminate friction in tendons", false, "Tendon friction is managed by synovial sheaths and bursae.")
                                    )
                                )
                            )
                        ),
                        CurriculumLesson(
                            id = "lesson-2-1-2",
                            chapterId = "ch-2",
                            subchapterId = "sub-2-1",
                            lessonIndex = 7,
                            title = "Muscle Contraction Types: Isometric, Concentric, Eccentric",
                            subtitle = "Cross-bridge mechanics and tension under lengthening",
                            durationMinutes = 6,
                            category = TopicCategory.ANATOMY_PHYSIOLOGY,
                            summary = "Muscle can generate force while shortening (concentric), holding steady (isometric), or controlling lengthening under load (eccentric).",
                            fullMarkdownText = """
                                # The Spectrum of Muscle Actions
                                
                                Skeletal muscle fibers generate force through the sliding filament theory (actin-myosin cross-bridges):
                                
                                1. **Concentric Action**: Muscle generates force while shortening (e.g. rising from a squat, curling a dumbbell).
                                2. **Isometric Action**: Muscle produces tension without observable change in joint angle (e.g. pausing at the bottom of a goblet squat, holding a plank).
                                3. **Eccentric Action**: Muscle produces tension while being forcibly lengthened by external load (e.g. descending under control in a squat).
                                
                                Eccentric actions produce the highest force output per motor unit, stimulate significant collagen remodeling and muscle hypertrophy, but induce the highest micro-trauma requiring adequate recovery.
                            """.trimIndent(),
                            keyTerms = listOf("Concentric", "Eccentric", "Isometric", "Cross-bridge cycle", "Titina mechanical spring"),
                            practicalApplication = "Perform a push-up with a 4-second slow descent (eccentric phase), a 1-second pause at the bottom (isometric), and a quick 1-second press up (concentric).",
                            questions = listOf(
                                KnowledgeCheckQuestion(
                                    id = "q-2-1-2-1",
                                    questionText = "Which muscle action is capable of generating the highest mechanical force?",
                                    options = listOf(
                                        KnowledgeCheckOption("opt-1", "Eccentric (lengthening under tension)", true, "Eccentric contractions can generate up to 20-40% more force than concentric actions due to mechanical engagement of titin filaments."),
                                        KnowledgeCheckOption("opt-2", "Concentric (shortening)", false, "Concentric actions produce lower peak force as filaments slide past one another at higher velocities."),
                                        KnowledgeCheckOption("opt-3", "Resting state", false, "Resting muscle produces baseline passive viscoelastic tone only.")
                                    )
                                )
                            ),
                            isGateMilestone = true
                        )
                    )
                )
            )
        )
    }

    private fun createChapter3(): CurriculumChapter {
        return CurriculumChapter(
            id = "ch-3",
            number = 3,
            title = "Biomechanics & Motion Analysis",
            description = "Torque, moment arms, center of pressure, impulse, and ground reaction forces in real athletic lifts.",
            category = TopicCategory.BIOMECHANICS,
            subchapters = listOf(
                CurriculumSubchapter(
                    id = "sub-3-1",
                    chapterId = "ch-3",
                    title = "Torque & Moment Arms in Resistance Training",
                    description = "Why bar path and body proportions dictate muscular demand.",
                    lessons = listOf(
                        CurriculumLesson(
                            id = "lesson-3-1-1",
                            chapterId = "ch-3",
                            subchapterId = "sub-3-1",
                            lessonIndex = 8,
                            title = "Torque and Internal vs External Moment Arms",
                            subtitle = "Force times perpendicular distance = rotational load",
                            durationMinutes = 7,
                            category = TopicCategory.BIOMECHANICS,
                            summary = "Joints experience rotational torque (Force × Moment Arm). Altering the horizontal distance between joint and barbell changes muscle requirements.",
                            fullMarkdownText = """
                                # Understanding Torque in Weight Training
                                
                                Muscles do not push or pull in straight lines; they produce **Torque** ($\tau = F \times d_{\perp}$), which is rotational force around a joint axis.
                                
                                - **External Moment Arm**: The perpendicular distance from the line of action of gravity/resistance to the joint axis.
                                - **Internal Moment Arm**: The perpendicular distance from the tendon insertion point to the joint axis (fixed by human anatomy).
                                
                                When you squat with a low-bar position, the hips shift further back from the barbell, creating a longer external moment arm at the hip (more glute/erector load) and a shorter moment arm at the knee (less quad demand) compared to a high-bar or front squat.
                            """.trimIndent(),
                            keyTerms = listOf("Torque", "External moment arm", "Perpendicular distance", "Rotational force", "Line of action"),
                            practicalApplication = "Hold a light dumbbell by your side with elbows bent 90 degrees vs arms straight out. Feel how the torque on your shoulder joint multiplies when the moment arm lengthens.",
                            questions = listOf(
                                KnowledgeCheckQuestion(
                                    id = "q-3-1-1-1",
                                    questionText = "If you shift from a front squat to a low-bar back squat, what happens to the moment arm at the hip?",
                                    options = listOf(
                                        KnowledgeCheckOption("opt-1", "It increases, placing greater torque demand on hip extensors", true, "Correct! Low-bar squats incline the torso forward, extending the horizontal distance between hips and barbell."),
                                        KnowledgeCheckOption("opt-2", "It drops to zero", false, "Hips remain significantly displaced from the barbell."),
                                        KnowledgeCheckOption("opt-3", "It shifts entirely to the wrist", false, "Wrists only stabilize the bar against the upper back.")
                                    )
                                )
                            ),
                            isGateMilestone = true
                        )
                    )
                )
            )
        )
    }

    private fun createChapter4(): CurriculumChapter {
        return CurriculumChapter(
            id = "ch-4",
            number = 4,
            title = "Load, Fatigue, & Periodization",
            description = "The science of adaptation: General Adaptation Syndrome, supercompensation, systemic fatigue, and deload weeks.",
            category = TopicCategory.LOAD_AND_RECOVERY,
            subchapters = listOf(
                CurriculumSubchapter(
                    id = "sub-4-1",
                    chapterId = "ch-4",
                    title = "Fatigue Dynamics & Deload Cycles",
                    description = "Why planned reductions in volume lead to long-term adaptation.",
                    lessons = listOf(
                        CurriculumLesson(
                            id = "lesson-4-1-1",
                            chapterId = "ch-4",
                            subchapterId = "sub-4-1",
                            lessonIndex = 9,
                            title = "The Fitness-Fatigue Paradigm & Deload Cadence",
                            subtitle = "Every 6th week is a planned recovery week",
                            durationMinutes = 6,
                            category = TopicCategory.LOAD_AND_RECOVERY,
                            summary = "Training stimulates both fitness and fatigue. Because fatigue masks fitness, a planned deload every 6th week allows tissues to remodel and supercompensate.",
                            fullMarkdownText = """
                                # The Two-Factor Model of Training
                                
                                According to Bannister's Fitness-Fatigue model, any training session has two immediate effects:
                                1. It builds **Fitness** (slow to rise, slow to decay).
                                2. It generates **Fatigue** (fast to rise, fast to decay).
                                
                                Your actual performance (preparedness) is simply:
                                $$\text{Preparedness} = \text{Fitness} - \text{Fatigue}$$
                                
                                When training intensely for 4-5 consecutive weeks, cumulative systemic fatigue (central nervous system, tendon collagen micro-tears, glycogen depletion) rises higher than fitness.
                                
                                ### The 6th Week Deload Rule:
                                In PhysiApp, every 6th week is designated as a planned Light (Deload) Week:
                                - **Volume reduced by 40-50%** (2 sets instead of 4).
                                - **Intensity maintained** at moderate effort (RPE 5-6).
                                - Movement patterns are preserved; this is active recovery that promotes blood flow and tendon adaptation, never passive deconditioning!
                            """.trimIndent(),
                            keyTerms = listOf("Fitness-Fatigue paradigm", "Supercompensation", "Deload week", "Systemic recovery", "RPE"),
                            practicalApplication = "Review your training log: identify when you last felt joint aches or sluggish motivation. Notice how planning a 6th week deload prevents those burnout walls before they happen.",
                            questions = listOf(
                                KnowledgeCheckQuestion(
                                    id = "q-4-1-1-1",
                                    questionText = "What is the primary physiological purpose of a deload week?",
                                    options = listOf(
                                        KnowledgeCheckOption("opt-1", "To allow fatigue to dissipate while retaining neuromuscular fitness", true, "Yes! Fatigue dissipates quickly, revealing the underlying fitness gains and supercompensation."),
                                        KnowledgeCheckOption("opt-2", "To completely stop all physical activity and stay in bed", false, "Active movement preserves movement patterns, synovial fluid circulation, and recovery."),
                                        KnowledgeCheckOption("opt-3", "To punish athletes for poor workouts", false, "Deloads are an essential tool for top performance and injury prevention.")
                                    )
                                ),
                                KnowledgeCheckQuestion(
                                    id = "q-4-1-1-2",
                                    questionText = "In PhysiApp, what is the planned volume reduction during a deload week?",
                                    options = listOf(
                                        KnowledgeCheckOption("opt-1", "Around 40% to 50% fewer sets with moderate effort", true, "Correct! Reducing volume by ~40-50% allows connective tissue recovery without detraining."),
                                        KnowledgeCheckOption("opt-2", "Volume is tripled to force adaptation", false, "That would cause severe overtraining during an already fatigued state."),
                                        KnowledgeCheckOption("opt-3", "Zero exercise allowed", false, "PhysiApp emphasizes active recovery and movement pattern preservation.")
                                    )
                                )
                            ),
                            isGateMilestone = true
                        )
                    )
                )
            )
        )
    }

    private fun createChapter5(): CurriculumChapter {
        return CurriculumChapter(
            id = "ch-5",
            number = 5,
            title = "Weekly Training Planning & Split Architecture",
            description = "Build a sustainable weekly schedule: full-body versus upper/lower splits, minimum effective consistency, and life adaptations.",
            category = TopicCategory.PROGRAMMING_COACHING,
            subchapters = listOf(
                CurriculumSubchapter(
                    id = "sub-5-1",
                    chapterId = "ch-5",
                    title = "Split Architecture & Frequency",
                    description = "Matching workout splits to realistic weekly cadence.",
                    lessons = listOf(
                        CurriculumLesson(
                            id = "lesson-5-1-1",
                            chapterId = "ch-5",
                            subchapterId = "sub-5-1",
                            lessonIndex = 10,
                            title = "Consistency Over Intensity: Weekly Goal Architecture",
                            subtitle = "Why weekly cadence outperforms rigid day-of-week mandates",
                            durationMinutes = 5,
                            category = TopicCategory.PROGRAMMING_COACHING,
                            summary = "Life disruptions are inevitable. Designing training around flexible weekly workout targets (minimum 2 days/week) maintains long-term adherence without guilt.",
                            fullMarkdownText = """
                                # Consistency Over Intensity
                                
                                The single greatest predictor of strength and musculoskeletal health over a 5-year span is not the intensity of your hardest session — it is the consistency with which you show up week after week.
                                
                                ### The Dual-Track Streak Philosophy:
                                - **Learning is Daily**: 5-10 minutes of reading and mental application creates a daily habit loop without physically fatiguing joints.
                                - **Training is Weekly**: Life happens. Missing Tuesday should never derail your momentum. If your weekly goal is 3 workouts, completing them on Monday, Thursday, and Saturday or Tuesday, Wednesday, Friday achieves the identical physiological adaptation!
                                
                                ### Minimum Effective Consistency:
                                Two well-structured full body workouts per week preserve 85%+ of muscular adaptations for months. Flexible scheduling is not a flaw; it is smart training design.
                            """.trimIndent(),
                            keyTerms = listOf("Minimum effective dose", "Weekly goal architecture", "Dual-track habit", "Training adherence", "Autoregulation"),
                            practicalApplication = "Set a realistic weekly target in your app settings (e.g. 3 sessions/week). If you have a busy travel week, drop to 2 sessions without guilt.",
                            questions = listOf(
                                KnowledgeCheckQuestion(
                                    id = "q-5-1-1-1",
                                    questionText = "Why does PhysiApp track training on a weekly cadence rather than a rigid daily streak?",
                                    options = listOf(
                                        KnowledgeCheckOption("opt-1", "To allow flexible scheduling and avoid punishing vital rest days with guilt", true, "Exactly! Muscle synthesis and tendon remodeling happen on rest days; punishing rest breaks habits."),
                                        KnowledgeCheckOption("opt-2", "Because muscles forget training after 24 hours", false, "Muscular adaptations persist for days and weeks."),
                                        KnowledgeCheckOption("opt-3", "To discourage people from exercising", false, "Weekly flexibility dramatically improves 1-year adherence.")
                                    )
                                )
                            ),
                            isGateMilestone = true
                        )
                    )
                )
            )
        )
    }
}

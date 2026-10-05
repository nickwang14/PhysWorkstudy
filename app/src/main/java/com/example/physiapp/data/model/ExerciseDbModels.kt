package com.example.physiapp.data.model

data class ExerciseDbItem(
    val id: String,
    val name: String,
    val bodyPart: String,
    val equipment: String,
    val gifUrl: String,
    val target: String,
    val secondaryMuscles: List<String>,
    val instructions: List<String>
) {
    /**
     * Map target muscle / body part to one of PhysiApp's 6 Fundamental Movement Patterns
     */
    fun inferredPattern(): MovementPattern {
        val lowerTarget = target.lowercase()
        val lowerBody = bodyPart.lowercase()
        val lowerName = name.lowercase()

        return when {
            lowerName.contains("squat") || lowerName.contains("lunge") || lowerName.contains("leg press") ||
                    lowerTarget.contains("quad") || lowerTarget.contains("adductor") -> MovementPattern.SQUAT

            lowerName.contains("deadlift") || lowerName.contains("hinge") || lowerName.contains("good morning") ||
                    lowerTarget.contains("glute") || lowerTarget.contains("hamstring") -> MovementPattern.HINGE

            lowerName.contains("press") || lowerName.contains("push") || lowerName.contains("dip") ||
                    lowerTarget.contains("pectoral") || lowerTarget.contains("tricep") || lowerBody == "chest" -> MovementPattern.PUSH

            lowerName.contains("pull") || lowerName.contains("row") || lowerName.contains("chin") ||
                    lowerTarget.contains("lat") || lowerTarget.contains("bicep") || lowerBody == "back" -> MovementPattern.PULL

            lowerName.contains("carry") || lowerName.contains("walk") || lowerName.contains("hold") ||
                    lowerTarget.contains("forearm") || lowerTarget.contains("grip") -> MovementPattern.CARRY

            lowerName.contains("plank") || lowerName.contains("twist") || lowerName.contains("pallof") ||
                    lowerTarget.contains("ab") || lowerTarget.contains("oblique") || lowerBody == "waist" -> MovementPattern.ROTATION

            else -> MovementPattern.PUSH
        }
    }

    fun toExerciseDef(): ExerciseDef {
        val pattern = inferredPattern()
        val formattedName = name.split(" ")
            .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

        val secMusclesStr = if (secondaryMuscles.isNotEmpty()) " (${secondaryMuscles.joinToString()})" else ""

        return ExerciseDef(
            id = "edb_$id",
            name = formattedName,
            pattern = pattern,
            primaryMuscles = "${target.replaceFirstChar { it.uppercase() }}$secMusclesStr",
            equipment = equipment.replaceFirstChar { it.uppercase() },
            coachingCues = if (instructions.isNotEmpty()) instructions else listOf("Execute movement with stable kinetic chain."),
            commonMistakes = listOf("Avoid rushing the eccentric phase", "Maintain neutral spinal posture"),
            regression = "Perform bodyweight variation or reduce loading",
            progression = "Add progressive overload or incorporate isometric pauses"
        )
    }
}

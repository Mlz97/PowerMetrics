package com.example.feperfectsquat.models

enum class Sex(val label: String) { MALE("Male"), FEMALE("Female"), OTHER("Other") }

data class UserProfile(
    val name: String,
    val age: Int,
    val sex: Sex,
    val bodyWeightKg: Double,
    val heightCm: Double,
    val unit: WeightUnit
)

/** Profile form state; steppers clamp to the same limits as the design. */
data class ProfileDraft(
    val name: String = "",
    val age: Int = 28,
    val sex: Sex? = null,
    val bodyWeightKg: Double = 82.0,
    // Vertical jump analysis needs height to scale pixels to centimetres
    val heightCm: Double = 178.0,
    val unit: WeightUnit = WeightUnit.KG
) {
    val canContinue: Boolean get() = name.isNotBlank() && sex != null

    fun withAge(delta: Int) = copy(age = (age + delta).coerceIn(13, 99))
    fun withBodyWeight(deltaKg: Double) = copy(bodyWeightKg = (bodyWeightKg + deltaKg).coerceIn(35.0, 200.0))
    fun withHeight(deltaCm: Double) = copy(heightCm = (heightCm + deltaCm).coerceIn(120.0, 230.0))

    fun toProfile(): UserProfile? =
        if (canContinue) UserProfile(name.trim(), age, sex!!, bodyWeightKg, heightCm, unit) else null
}

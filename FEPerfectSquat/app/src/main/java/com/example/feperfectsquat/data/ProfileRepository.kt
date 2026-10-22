package com.example.feperfectsquat.data

import android.content.Context
import com.example.feperfectsquat.models.Sex
import com.example.feperfectsquat.models.UserProfile
import com.example.feperfectsquat.models.WeightUnit

/** Keeps the onboarding profile on the device; there is no user account on the backend yet. */
class ProfileRepository(context: Context) {
    private val prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE)

    fun load(): UserProfile? {
        val name = prefs.getString(KEY_NAME, null) ?: return null
        val sex = prefs.getString(KEY_SEX, null)?.let { runCatching { Sex.valueOf(it) }.getOrNull() } ?: return null
        val unit = prefs.getString(KEY_UNIT, null)?.let { runCatching { WeightUnit.valueOf(it) }.getOrNull() } ?: WeightUnit.KG
        return UserProfile(
            name = name,
            age = prefs.getInt(KEY_AGE, 28),
            sex = sex,
            bodyWeightKg = prefs.getFloat(KEY_BODY_WEIGHT, 82f).toDouble(),
            heightCm = prefs.getFloat(KEY_HEIGHT, 178f).toDouble(),
            unit = unit
        )
    }

    fun save(profile: UserProfile) {
        prefs.edit()
            .putString(KEY_NAME, profile.name)
            .putInt(KEY_AGE, profile.age)
            .putString(KEY_SEX, profile.sex.name)
            .putFloat(KEY_BODY_WEIGHT, profile.bodyWeightKg.toFloat())
            .putFloat(KEY_HEIGHT, profile.heightCm.toFloat())
            .putString(KEY_UNIT, profile.unit.name)
            .apply()
    }

    private companion object {
        const val KEY_NAME = "name"
        const val KEY_AGE = "age"
        const val KEY_SEX = "sex"
        const val KEY_BODY_WEIGHT = "bodyWeightKg"
        const val KEY_HEIGHT = "heightCm"
        const val KEY_UNIT = "unit"
    }
}

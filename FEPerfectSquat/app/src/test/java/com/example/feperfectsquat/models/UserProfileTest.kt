package com.example.feperfectsquat.models

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserProfileTest {

    @Test
    fun continueNeedsANameAndASex() {
        assertTrue(ProfileDraft(name = "Alex", sex = Sex.FEMALE).canContinue)
        assertFalse(ProfileDraft(name = "  ", sex = Sex.FEMALE).canContinue)
        assertFalse(ProfileDraft(name = "Alex", sex = null).canContinue)
    }

    @Test
    fun steppersStayInsideTheirLimits() {
        val draft = ProfileDraft(age = 13, bodyWeightKg = 35.0, heightCm = 120.0)

        assertTrue(draft.withAge(-1).age == 13)
        assertTrue(draft.withBodyWeight(-0.5).bodyWeightKg == 35.0)
        assertTrue(draft.withHeight(-1.0).heightCm == 120.0)
        assertTrue(ProfileDraft(age = 99).withAge(1).age == 99)
        assertTrue(ProfileDraft(bodyWeightKg = 82.0).withBodyWeight(0.5).bodyWeightKg == 82.5)
    }

    @Test
    fun draftBecomesAProfileWithTheNameTrimmed() {
        val profile = ProfileDraft(name = " Alex ", sex = Sex.MALE).toProfile()

        assertTrue(profile?.name == "Alex")
        assertTrue(ProfileDraft(name = "Alex").toProfile() == null)
    }
}

package com.example.feperfectsquat.models

import org.junit.Assert.assertEquals
import org.junit.Test

class UnitsTest {

    @Test
    fun kilogramsDropTrailingZeroAndKeepOneDecimal() {
        assertEquals("150 kg", formatWeight(150.0, WeightUnit.KG))
        assertEquals("82.5 kg", formatWeight(82.5, WeightUnit.KG))
        assertEquals("82.3 kg", formatWeight(82.26, WeightUnit.KG))
    }

    @Test
    fun poundsRoundToTheNearestFive() {
        // 150 kg = 330.7 lb -> 330 lb; 82 kg = 180.8 lb -> 180 lb
        assertEquals("330 lb", formatWeight(150.0, WeightUnit.LB))
        assertEquals("180 lb", formatWeight(82.0, WeightUnit.LB))
    }

    @Test
    fun numberWithoutUnitFollowsTheSameRounding() {
        assertEquals("150", formatWeightNumber(150.0, WeightUnit.KG))
        assertEquals("330", formatWeightNumber(150.0, WeightUnit.LB))
    }
}

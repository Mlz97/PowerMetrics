package com.example.feperfectsquat.models

import org.junit.Assert.assertEquals
import org.junit.Test

class CommunityRankingTest {

    private val heavy = CommunityEntry(name = "Heavy", bodyWeightKg = 100.0, loadKg = 200.0, meanVelocity = 0.25, powerW = 2000.0)
    private val light = CommunityEntry(name = "Light", bodyWeightKg = 60.0, loadKg = 150.0, meanVelocity = 0.35, powerW = 2100.0)

    @Test
    fun rankingSortsByTheChosenMetricDescending() {
        assertEquals(listOf("Heavy", "Light"), rank(listOf(light, heavy), CommunityMetric.LOAD).map { it.name })
        // Relative: 2.0 x BW vs 2.5 x BW
        assertEquals(listOf("Light", "Heavy"), rank(listOf(heavy, light), CommunityMetric.RELATIVE).map { it.name })
    }

    @Test
    fun jumpMetricsAreDerivedFromHeightAndBodyweight() {
        val jumper = CommunityEntry(name = "J", bodyWeightKg = 82.0, jumpHeightCm = 41.2)

        assertEquals(41.2, CommunityMetric.HEIGHT.value(jumper), 0.0)
        assertEquals(4160.0, CommunityMetric.JUMP_POWER.value(jumper), 0.0)
        assertEquals(4160.0 / 82.0,CommunityMetric.POWER_PER_KG.value(jumper), 1e-9)
        assertEquals(580.0, CommunityMetric.FLIGHT_TIME.value(jumper), 0.0)
    }

    @Test
    fun metricValuesAreFormattedWithTheirUnits() {
        val jumper = CommunityEntry(name = "J", bodyWeightKg = 82.0, jumpHeightCm = 41.2)

        assertEquals("200 kg", CommunityMetric.LOAD.format(heavy, WeightUnit.KG))
        assertEquals("440 lb", CommunityMetric.LOAD.format(heavy, WeightUnit.LB))
        assertEquals("2.00×BW", CommunityMetric.RELATIVE.format(heavy, WeightUnit.KG))
        assertEquals("0.25 m/s", CommunityMetric.MEAN_VELOCITY.format(heavy, WeightUnit.KG))
        assertEquals("2,000 W", CommunityMetric.POWER.format(heavy, WeightUnit.KG))
        assertEquals("41.2 cm", CommunityMetric.HEIGHT.format(jumper, WeightUnit.KG))
        assertEquals("4,160 W", CommunityMetric.JUMP_POWER.format(jumper, WeightUnit.KG))
        assertEquals("50.7 W/kg", CommunityMetric.POWER_PER_KG.format(jumper, WeightUnit.KG))
        assertEquals("580 ms", CommunityMetric.FLIGHT_TIME.format(jumper, WeightUnit.KG))
    }

    @Test
    fun eachLiftHasItsOwnMetricsWithTheFirstAsDefault() {
        assertEquals(CommunityMetric.LOAD, CommunityMetric.forLift(ExerciseType.SQUAT).first())
        assertEquals(CommunityMetric.HEIGHT, CommunityMetric.forLift(ExerciseType.VERTICAL_JUMP).first())
        assertEquals(4, CommunityMetric.forLift(ExerciseType.VERTICAL_JUMP).size)
    }
}

package com.example.feperfectsquat.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class HomeStatsTest {

    // 2026-10-22 12:00 local time
    private val now = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 22, 12, 0, 0) }.time

    @Test
    fun squatBestsComeFromTheLast30DaysAndCompareWithEarlierBests() {
        val stats = HomeStats.from(listOf(
            testSession(createdAt = "2026-10-20T10:00:00", estimatedRM = 150.0, avgConcentricVelocity = 0.48, validReps = 3),
            testSession(createdAt = "2026-10-15T10:00:00", estimatedRM = 145.0, avgConcentricVelocity = 0.55, validReps = 4),
            testSession(createdAt = "2026-08-01T10:00:00", estimatedRM = 140.0, avgConcentricVelocity = 0.50, validReps = 9)
        ), now)

        assertEquals(150.0, stats.squat.best1RmKg!!, 0.0)
        assertEquals(140.0, stats.squat.previousBest1RmKg!!, 0.0)
        assertEquals(0.55, stats.squat.bestVelocity!!, 0.0)
        assertEquals(0.50, stats.squat.previousBestVelocity!!, 0.0)
        assertEquals(7, stats.squat.validReps)
    }

    @Test
    fun jumpBestsUseHeightAndPeakPower() {
        val stats = HomeStats.from(listOf(
            testSession(type = ExerciseType.VERTICAL_JUMP, createdAt = "2026-10-18T10:00:00", bestJumpHeightCm = 41.2, peakPowerW = 3820.0),
            testSession(type = ExerciseType.VERTICAL_JUMP, createdAt = "2026-10-13T10:00:00", bestJumpHeightCm = 40.6, peakPowerW = 3900.0)
        ), now)

        assertEquals(41.2, stats.jump.bestHeightCm!!, 0.0)
        assertEquals(3900.0, stats.jump.bestPeakPowerW!!, 0.0)
        assertNull(stats.jump.previousBestHeightCm)
    }

    @Test
    fun unfinishedSessionsAndMissingDatesAreIgnored() {
        val stats = HomeStats.from(listOf(
            testSession(state = "ERROR", estimatedRM = 300.0),
            testSession(createdAt = null, estimatedRM = 250.0)
        ), now)

        assertNull(stats.squat.best1RmKg)
        assertEquals(0, stats.squat.validReps)
    }

    @Test
    fun lastSessionIsTheNewestFinishedOne() {
        val newest = testSession(id = 2, createdAt = "2026-10-20T10:00:00")
        val last = lastFinishedSession(listOf(
            testSession(id = 1, createdAt = "2026-10-10T10:00:00"),
            newest,
            testSession(id = 3, state = "PROCESSING", createdAt = "2026-10-21T10:00:00")
        ))

        assertEquals(newest, last)
    }

    @Test
    fun historyChartTakesTheLastEightValuesOldestFirst() {
        val sessions = (1..10).map {
            testSession(id = it.toLong(), createdAt = "2026-10-%02dT10:00:00".format(it), estimatedRM = 100.0 + it)
        }

        val points = historyChart(sessions, ExerciseType.SQUAT)

        assertEquals((3..10).map { 100.0 + it }, points)
    }
}

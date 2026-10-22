package com.example.feperfectsquat.data

import com.example.feperfectsquat.models.CommunityEntry
import com.example.feperfectsquat.models.CommunityMetric
import com.example.feperfectsquat.models.ExerciseType

/** Community leaderboards. The backend has no users or rankings yet, so only [SampleCommunityRepository] exists. */
interface CommunityRepository {
    /** True while the boards show sample data instead of real athletes. */
    val isSample: Boolean
    fun board(lift: ExerciseType): List<CommunityEntry>
    fun you(lift: ExerciseType, bodyWeightKg: Double): CommunityEntry
    fun yourRank(metric: CommunityMetric): Int
    fun memberCount(lift: ExerciseType): Int
    fun context(lift: ExerciseType): String
}

/** Sample boards from the PowerMetrics design; replace with a backend-backed implementation. */
object SampleCommunityRepository : CommunityRepository {
    override val isSample = true

    private val squat = listOf(
        CommunityEntry("Maya Rios", 82.4, loadKg = 247.5, meanVelocity = 0.29, powerW = 2480.0),
        CommunityEntry("Jonas Keller", 83.0, loadKg = 242.5, meanVelocity = 0.27, powerW = 2390.0),
        CommunityEntry("Tomás Iglesias", 81.2, loadKg = 240.0, meanVelocity = 0.31, powerW = 2610.0),
        CommunityEntry("Priya Nair", 80.1, loadKg = 235.0, meanVelocity = 0.33, powerW = 2550.0),
        CommunityEntry("Kenji Mori", 82.9, loadKg = 232.5, meanVelocity = 0.28, powerW = 2300.0),
        CommunityEntry("Lena Fischer", 77.5, loadKg = 228.0, meanVelocity = 0.35, powerW = 2700.0),
    )

    private val jump = listOf(
        CommunityEntry("Sam Okafor", 82.0, jumpHeightCm = 68.4),
        CommunityEntry("Dario Conti", 83.0, jumpHeightCm = 64.1),
        CommunityEntry("Lena Fischer", 76.0, jumpHeightCm = 61.7),
        CommunityEntry("Priya Nair", 80.5, jumpHeightCm = 59.2),
        CommunityEntry("Tomás Iglesias", 81.4, jumpHeightCm = 57.8),
        CommunityEntry("Kenji Mori", 83.0, jumpHeightCm = 55.3),
    )

    private val ranks = mapOf(
        CommunityMetric.LOAD to 214, CommunityMetric.RELATIVE to 198, CommunityMetric.MEAN_VELOCITY to 121,
        CommunityMetric.POWER to 176, CommunityMetric.HEIGHT to 342, CommunityMetric.JUMP_POWER to 288,
        CommunityMetric.POWER_PER_KG to 301, CommunityMetric.FLIGHT_TIME to 342,
    )

    override fun board(lift: ExerciseType) = if (lift == ExerciseType.VERTICAL_JUMP) jump else squat

    override fun you(lift: ExerciseType, bodyWeightKg: Double) =
        if (lift == ExerciseType.VERTICAL_JUMP) CommunityEntry("You", bodyWeightKg, jumpHeightCm = 41.2)
        else CommunityEntry("You", bodyWeightKg, loadKg = 150.0, meanVelocity = 0.30, powerW = 1840.0)

    override fun yourRank(metric: CommunityMetric) = ranks.getValue(metric)

    override fun memberCount(lift: ExerciseType) = 3902

    override fun context(lift: ExerciseType) =
        if (lift == ExerciseType.VERTICAL_JUMP) "All bodyweights · this month · video-verified"
        else "83 kg class · this month · video-verified"
}

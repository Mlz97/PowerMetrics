package com.example.feperfectsquat.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feperfectsquat.models.AnalysisSession
import com.example.feperfectsquat.models.ExerciseType
import com.example.feperfectsquat.models.HomeStats
import com.example.feperfectsquat.models.UserProfile
import com.example.feperfectsquat.models.flightTimeMs
import com.example.feperfectsquat.models.formatWeight
import com.example.feperfectsquat.models.formatWeightNumber
import com.example.feperfectsquat.models.isJump
import com.example.feperfectsquat.models.parseCreatedAt
import com.example.feperfectsquat.ui.components.PmCard
import com.example.feperfectsquat.ui.components.PmSegmented
import com.example.feperfectsquat.ui.components.StatTile
import com.example.feperfectsquat.ui.components.TwoColumnGrid
import com.example.feperfectsquat.ui.components.pressable
import com.example.feperfectsquat.ui.theme.PmAccent
import com.example.feperfectsquat.ui.theme.PmAccentDeep
import com.example.feperfectsquat.ui.theme.PmAccentLight
import com.example.feperfectsquat.ui.theme.PmAccentTint
import com.example.feperfectsquat.ui.theme.PmBackground
import com.example.feperfectsquat.ui.theme.PmCanvas
import com.example.feperfectsquat.ui.theme.PmText
import com.example.feperfectsquat.ui.theme.PmTextMuted
import com.example.feperfectsquat.ui.theme.PmTextSecondary
import com.example.feperfectsquat.ui.theme.pmDisplay
import com.example.feperfectsquat.ui.theme.pmEyebrow
import com.example.feperfectsquat.ui.theme.pmMono
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/** Summary of the user's community standing for the Home card. */
data class CommunityTeaser(val rank: Int, val members: Int, val context: String, val isSample: Boolean)

@Composable
fun HomeScreen(
    profile: UserProfile,
    today: Date,
    stats: HomeStats,
    lastSession: AnalysisSession?,
    community: CommunityTeaser,
    statsLift: ExerciseType,
    onStatsLiftChange: (ExerciseType) -> Unit,
    onAnalyze: () -> Unit,
    onHistory: () -> Unit,
    onCommunity: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 6.dp, end = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(SimpleDateFormat("EEEE, MMM d", Locale.US).format(today), fontSize = 14.sp, color = PmTextSecondary)
                Text("Ready to train, ${profile.name}?", style = pmDisplay(26.sp), color = PmText)
            }
            Box(
                Modifier.size(42.dp).clip(CircleShape).background(PmText),
                contentAlignment = Alignment.Center
            ) {
                Text(profile.name.take(1).uppercase(), color = PmBackground, fontWeight = FontWeight.Bold)
            }
        }

        AnalyzeHero(onAnalyze)

        PmCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Your stats", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = PmText)
                    Text("Best in the last 30 days", fontSize = 12.sp, color = PmTextMuted)
                }
                PmSegmented(
                    options = listOf("Squat", "Jump"),
                    selectedIndex = if (statsLift == ExerciseType.SQUAT) 0 else 1,
                    onSelect = { onStatsLiftChange(if (it == 0) ExerciseType.SQUAT else ExerciseType.VERTICAL_JUMP) },
                    itemHeight = 32.dp,
                    containerColor = PmCanvas,
                    containerBorder = false,
                    outerRadius = 100.dp,
                    innerRadius = 100.dp,
                    fontSize = 13.sp,
                    fillWidth = false
                )
            }
            val tiles = if (statsLift == ExerciseType.SQUAT) squatTiles(stats, profile) else jumpTiles(stats, profile)
            TwoColumnGrid(tiles) { tile ->
                StatTile(tile.label, tile.value, tile.unit, tile.delta, tile.deltaColor, Modifier.weight(1f))
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PmCard(
                modifier = Modifier.weight(1f),
                onClick = onHistory,
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                Text("Historic", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = PmText)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Last session", fontSize = 12.sp, color = PmTextMuted)
                    if (lastSession == null) {
                        Text("No sessions yet", style = pmMono(16.sp), color = PmText)
                        Text("Analyze your first set", fontSize = 13.sp, color = PmTextSecondary)
                    } else {
                        Text(sessionHeadline(lastSession, profile), style = pmMono(16.sp), color = PmText)
                        Text(
                            "${if (lastSession.isJump()) "Vertical jump" else "Squat"} · ${shortDate(lastSession)}",
                            fontSize = 13.sp, color = PmTextSecondary
                        )
                    }
                }
            }
            PmCard(
                modifier = Modifier.weight(1f),
                background = PmAccentTint,
                onClick = onCommunity,
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                Text("Community PRs", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = PmText)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Your squat rank", fontSize = 12.sp, color = PmTextSecondary)
                    Text("#${community.rank}", style = pmMono(16.sp), color = PmAccentLight)
                    Text(
                        "of ${"%,d".format(Locale.US, community.members)} · ${community.context}",
                        fontSize = 13.sp, color = PmTextSecondary
                    )
                    if (community.isSample) Text("Sample data", style = pmEyebrow(9.sp), color = PmTextMuted)
                }
            }
        }
    }
}

@Composable
private fun AnalyzeHero(onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(PmAccent)
            .pressable(pressedScale = 0.985f, onClick = onClick)
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(30.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("VIDEO · VBT", style = pmEyebrow(), color = Color.White.copy(alpha = 0.85f), modifier = Modifier.weight(1f))
            Box(Modifier.size(40.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                Text("↑", color = PmAccentDeep, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Analyze a video", style = pmDisplay(30.sp), color = Color.White)
            Text("Upload a squat or jump and get every rep measured.", fontSize = 15.sp, color = Color.White.copy(alpha = 0.92f))
        }
    }
}

private data class Tile(val label: String, val value: String, val unit: String, val delta: String, val deltaColor: Color)

private const val NO_DATA = "—"

/** Delta line against the best before the 30-day window. */
private fun delta(best: Double?, previous: Double?, format: (Double) -> String): Pair<String, Color> = when {
    best == null -> "No data yet" to PmTextMuted
    previous == null -> "First best" to PmTextMuted
    best > previous -> "▲ ${format(best - previous)}" to PmAccentLight
    best < previous -> "▼ ${format(abs(best - previous))}" to PmTextMuted
    else -> "— same" to PmTextMuted
}

private fun squatTiles(stats: HomeStats, profile: UserProfile): List<Tile> {
    val s = stats.squat
    val (rmDelta, rmColor) = delta(s.best1RmKg, s.previousBest1RmKg) { formatWeight(it, profile.unit) }
    val (velDelta, velColor) = delta(s.bestVelocity, s.previousBestVelocity) { "%.2f".format(Locale.US, it) }
    return listOf(
        Tile("Est. 1RM", s.best1RmKg?.let { formatWeightNumber(it, profile.unit) } ?: NO_DATA, profile.unit.label, rmDelta, rmColor),
        // The backend reports concentric velocity normalised to body size (1/s), not m/s
        Tile("Best mean vel.", s.bestVelocity?.let { "%.2f".format(Locale.US, it) } ?: NO_DATA, "1/s", velDelta, velColor),
        Tile("Valid reps", s.validReps.toString(), "reps", "last 30 days", PmTextMuted),
        Tile(
            "Relative 1RM",
            s.best1RmKg?.let { "%.2f".format(Locale.US, it / profile.bodyWeightKg) } ?: NO_DATA,
            "×BW",
            "at ${formatWeight(profile.bodyWeightKg, profile.unit)}",
            PmTextMuted
        ),
    )
}

private fun jumpTiles(stats: HomeStats, profile: UserProfile): List<Tile> {
    val j = stats.jump
    val (hDelta, hColor) = delta(j.bestHeightCm, j.previousBestHeightCm) { "%.1f cm".format(Locale.US, it) }
    val (pDelta, pColor) = delta(j.bestPeakPowerW, j.previousBestPeakPowerW) { diff ->
        "${((diff / j.previousBestPeakPowerW!!) * 100).roundToInt()}%"
    }
    return listOf(
        Tile("Best height", j.bestHeightCm?.let { "%.1f".format(Locale.US, it) } ?: NO_DATA, "cm", hDelta, hColor),
        Tile("Flight time", j.bestHeightCm?.let { flightTimeMs(it).toString() } ?: NO_DATA, "ms", "of your best jump", PmTextMuted),
        Tile("Peak power", j.bestPeakPowerW?.let { "%,d".format(Locale.US, it.roundToInt()) } ?: NO_DATA, "W", pDelta, pColor),
        Tile(
            "Relative power",
            j.bestPeakPowerW?.let { "%.1f".format(Locale.US, it / profile.bodyWeightKg) } ?: NO_DATA,
            "W/kg",
            "at ${formatWeight(profile.bodyWeightKg, profile.unit)}",
            PmTextMuted
        ),
    )
}

/** "150 kg × 3" for a squat, "41.2 cm best" for a jump. */
fun sessionHeadline(session: AnalysisSession, profile: UserProfile): String =
    if (session.isJump()) session.bestJumpHeightCm?.let { "%.1f cm best".format(Locale.US, it) } ?: "${session.totalJumps ?: 0} jumps"
    else {
        val reps = session.validReps ?: session.totalReps ?: 0
        session.weightKg?.let { "${formatWeight(it, profile.unit)} × $reps" } ?: "$reps reps"
    }

/** "Oct 20" from the session timestamp. */
fun shortDate(session: AnalysisSession): String =
    parseCreatedAt(session.createdAt)?.let { SimpleDateFormat("MMM d", Locale.US).format(it) } ?: "—"

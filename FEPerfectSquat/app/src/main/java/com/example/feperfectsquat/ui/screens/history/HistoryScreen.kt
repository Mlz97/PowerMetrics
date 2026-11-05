package com.example.feperfectsquat.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feperfectsquat.models.AnalysisSession
import com.example.feperfectsquat.models.ExerciseType
import com.example.feperfectsquat.models.UserProfile
import com.example.feperfectsquat.models.formatWeight
import com.example.feperfectsquat.models.historyChart
import com.example.feperfectsquat.models.isJump
import com.example.feperfectsquat.models.parseCreatedAt
import com.example.feperfectsquat.models.sessionsOf
import com.example.feperfectsquat.ui.components.BarChart
import com.example.feperfectsquat.ui.components.PmCard
import com.example.feperfectsquat.ui.screens.home.sessionHeadline
import com.example.feperfectsquat.ui.theme.PmAccent
import com.example.feperfectsquat.ui.theme.PmAccentSoft
import com.example.feperfectsquat.ui.theme.PmBackground
import com.example.feperfectsquat.ui.theme.PmError
import com.example.feperfectsquat.ui.theme.PmLineStrong
import com.example.feperfectsquat.ui.theme.PmSurface
import com.example.feperfectsquat.ui.theme.PmText
import com.example.feperfectsquat.ui.theme.PmTextMuted
import com.example.feperfectsquat.ui.theme.PmTextSecondary
import com.example.feperfectsquat.ui.theme.pmDisplay
import com.example.feperfectsquat.ui.theme.pmMono
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HistoryScreen(
    sessions: List<AnalysisSession>,
    isLoading: Boolean,
    errorMessage: String?,
    lift: ExerciseType,
    onLiftChange: (ExerciseType) -> Unit,
    profile: UserProfile,
    onRetry: () -> Unit,
) {
    val isSquat = lift == ExerciseType.SQUAT
    val rows = sessionsOf(sessions, lift)
    val chart = historyChart(sessions, lift)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Historic", style = pmDisplay(26.sp), color = PmText, modifier = Modifier.padding(horizontal = 6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(ExerciseType.SQUAT to "Squat", ExerciseType.VERTICAL_JUMP to "Vertical jump").forEach { (type, label) ->
                val selected = type == lift
                Text(
                    label,
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (selected) PmText else PmSurface)
                        .clickable(role = Role.Tab) { onLiftChange(type) }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    color = if (selected) PmBackground else PmText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }

        when {
            isLoading && sessions.isEmpty() -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PmAccent)
            }
            errorMessage != null && sessions.isEmpty() -> PmCard(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Couldn't load your sessions", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PmText)
                Text(errorMessage, fontSize = 13.sp, color = PmError)
                Text(
                    "Retry",
                    modifier = Modifier.clickable(role = Role.Button, onClick = onRetry).padding(vertical = 4.dp),
                    color = PmAccent, fontWeight = FontWeight.SemiBold
                )
            }
            rows.isEmpty() -> PmCard(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("No ${if (isSquat) "squat" else "jump"} sessions yet", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PmText)
                Text("Analyze a video and it will show up here.", fontSize = 14.sp, color = PmTextSecondary)
            }
            else -> {
                if (chart.isNotEmpty()) ProgressChart(chart, isSquat, profile)
                SessionList(rows, profile)
            }
        }
    }
}

@Composable
private fun ProgressChart(values: List<Double>, isSquat: Boolean, profile: UserProfile) {
    val floor = values.min() * 0.96
    val range = (values.max() - floor).takeIf { it > 0 } ?: 1.0
    PmCard(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                if (isSquat) "Estimated 1RM" else "Best jump height",
                modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PmText
            )
            val latest = values.last()
            Text(
                if (isSquat) formatWeight(latest, profile.unit) else "%.1f cm".format(Locale.US, latest),
                style = pmMono(16.sp), color = PmAccent
            )
        }
        BarChart(
            heights = values.map { ((it - floor) / range).toFloat() },
            colors = values.indices.map { if (it == values.lastIndex) PmAccent else PmAccentSoft },
            modifier = Modifier.fillMaxWidth().height(90.dp)
        )
        Row {
            Text("${values.size} SESSIONS AGO", modifier = Modifier.weight(1f), style = pmMono(10.sp, FontWeight.SemiBold), color = PmTextMuted)
            Text("LATEST", style = pmMono(10.sp, FontWeight.SemiBold), color = PmTextMuted)
        }
    }
}

@Composable
private fun SessionList(rows: List<AnalysisSession>, profile: UserProfile) {
    PmCard(padding = 0.dp, verticalArrangement = Arrangement.Top) {
        rows.forEachIndexed { i, session ->
            if (i > 0) HorizontalDivider(color = PmLineStrong, thickness = 1.dp)
            SessionRow(session, profile)
        }
    }
}

@Composable
private fun SessionRow(session: AnalysisSession, profile: UserProfile) {
    val date = parseCreatedAt(session.createdAt)
    val jump = session.isJump()
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(Modifier.width(44.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(date?.let { SimpleDateFormat("d", Locale.US).format(it) } ?: "—", style = pmMono(16.sp), color = PmText)
            Text(
                date?.let { SimpleDateFormat("MMM", Locale.US).format(it).uppercase(Locale.US) } ?: "",
                style = pmMono(10.sp, FontWeight.SemiBold), color = PmTextMuted
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(if (jump) "Vertical jump" else "Squat", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PmText)
            Text(
                if (jump) "${session.totalJumps ?: 0} jumps" else sessionHeadline(session, profile),
                fontSize = 13.sp, color = PmTextSecondary
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            val metric = if (jump) session.bestJumpHeightCm?.let { "%.1f".format(Locale.US, it) }
            else session.avgConcentricVelocity?.let { "%.2f".format(Locale.US, it) }
            Text(metric ?: "—", style = pmMono(15.sp), color = PmText)
            Text(if (jump) "cm best" else "1/s mean", fontSize = 11.sp, color = PmTextMuted)
        }
    }
}

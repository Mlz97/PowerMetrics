package com.example.feperfectsquat.ui.screens.community

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feperfectsquat.data.CommunityRepository
import com.example.feperfectsquat.models.CommunityEntry
import com.example.feperfectsquat.models.CommunityMetric
import com.example.feperfectsquat.models.ExerciseType
import com.example.feperfectsquat.models.UserProfile
import com.example.feperfectsquat.models.rank
import com.example.feperfectsquat.ui.components.PmCard
import com.example.feperfectsquat.ui.components.PmSegmented
import com.example.feperfectsquat.ui.theme.PmAccent
import com.example.feperfectsquat.ui.theme.PmAccentLight
import com.example.feperfectsquat.ui.theme.PmAccentTint
import com.example.feperfectsquat.ui.theme.PmCanvas
import com.example.feperfectsquat.ui.theme.PmLine
import com.example.feperfectsquat.ui.theme.PmLineStrong
import com.example.feperfectsquat.ui.theme.PmText
import com.example.feperfectsquat.ui.theme.PmTextMuted
import com.example.feperfectsquat.ui.theme.PmTextSecondary
import com.example.feperfectsquat.ui.theme.pmDisplay
import com.example.feperfectsquat.ui.theme.pmEyebrow
import com.example.feperfectsquat.ui.theme.pmMono

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommunityScreen(
    repository: CommunityRepository,
    profile: UserProfile,
    lift: ExerciseType,
    metric: CommunityMetric,
    onLiftChange: (ExerciseType) -> Unit,
    onMetricChange: (CommunityMetric) -> Unit,
) {
    val metrics = CommunityMetric.forLift(lift)
    val selected = metric.takeIf { it.lift == lift } ?: metrics.first()
    val others = { e: CommunityEntry -> metrics.filter { it != selected }.joinToString(" · ") { it.format(e, profile.unit) } }
    val board = rank(repository.board(lift), selected)
    val you = repository.you(lift, profile.bodyWeightKg)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Community PRs", style = pmDisplay(26.sp), color = PmText, modifier = Modifier.padding(horizontal = 6.dp))

        if (repository.isSample) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, PmLineStrong, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SAMPLE DATA", style = pmEyebrow(10.sp), color = PmAccentLight)
                Text("Community rankings aren't live yet. These boards are an example.", fontSize = 12.sp, color = PmTextSecondary)
            }
        }

        PmSegmented(
            options = listOf("Squat", "Vertical jump"),
            selectedIndex = if (lift == ExerciseType.SQUAT) 0 else 1,
            onSelect = { onLiftChange(if (it == 0) ExerciseType.SQUAT else ExerciseType.VERTICAL_JUMP) },
            itemHeight = 40.dp,
            modifier = Modifier.fillMaxWidth()
        )

        Column(Modifier.padding(vertical = 2.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("RANK BY", style = pmEyebrow(10.sp), color = PmTextMuted, modifier = Modifier.padding(horizontal = 6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                metrics.forEach { m ->
                    val on = m == selected
                    Text(
                        m.label,
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .border(1.5.dp, if (on) PmAccent else PmLineStrong, RoundedCornerShape(100.dp))
                            .background(if (on) PmAccentTint else Color.Transparent)
                            .clickable(role = Role.Tab) { onMetricChange(m) }
                            .padding(horizontal = 13.dp, vertical = 8.dp),
                        color = if (on) PmAccentLight else PmTextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Text(repository.context(lift), fontSize = 13.sp, color = PmTextSecondary, modifier = Modifier.padding(horizontal = 6.dp))

        PmCard(padding = 0.dp, verticalArrangement = Arrangement.Top) {
            board.forEachIndexed { i, entry ->
                if (i > 0) HorizontalDivider(color = PmLineStrong, thickness = 1.dp)
                BoardRow(
                    rank = "${i + 1}",
                    rankColor = if (i < 3) PmAccent else PmTextMuted,
                    entry = entry,
                    main = selected.format(entry, profile.unit),
                    secondary = others(entry)
                )
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(PmAccentTint)
                .border(1.dp, PmLine, RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("#${repository.yourRank(selected)}", modifier = Modifier.width(44.dp), style = pmMono(14.sp), color = PmAccentLight)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("You", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PmText)
                Text(others(you), style = pmMono(11.sp, FontWeight.Medium), color = PmTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(selected.format(you, profile.unit), style = pmMono(16.sp), color = PmText)
        }
    }
}

@Composable
private fun BoardRow(rank: String, rankColor: Color, entry: CommunityEntry, main: String, secondary: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(rank, modifier = Modifier.width(18.dp), style = pmMono(14.sp), color = rankColor)
        Box(Modifier.size(36.dp).clip(CircleShape).background(PmCanvas), contentAlignment = Alignment.Center) {
            Text(entry.initials, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PmText)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(entry.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PmText)
            Text(secondary, style = pmMono(11.sp, FontWeight.Medium), color = PmTextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(main, style = pmMono(16.sp), color = PmText)
    }
}

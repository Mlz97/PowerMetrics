package com.example.feperfectsquat.ui.screens.analyze

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feperfectsquat.models.BarTone
import com.example.feperfectsquat.models.ExerciseType
import com.example.feperfectsquat.models.WeightUnit
import com.example.feperfectsquat.models.formatWeight
import com.example.feperfectsquat.models.summarize
import com.example.feperfectsquat.ui.components.BarChart
import com.example.feperfectsquat.ui.components.PlaceholderLabel
import com.example.feperfectsquat.ui.components.PmCard
import com.example.feperfectsquat.ui.components.PmOutlineButton
import com.example.feperfectsquat.ui.components.PmPrimaryButton
import com.example.feperfectsquat.ui.components.PmSegmented
import com.example.feperfectsquat.ui.components.SheetHandle
import com.example.feperfectsquat.ui.components.StepButton
import com.example.feperfectsquat.ui.components.pressable
import com.example.feperfectsquat.ui.components.stripes
import com.example.feperfectsquat.ui.theme.PmAccent
import com.example.feperfectsquat.ui.theme.PmAccentLight
import com.example.feperfectsquat.ui.theme.PmAccentMid
import com.example.feperfectsquat.ui.theme.PmAccentSoft
import com.example.feperfectsquat.ui.theme.PmBackground
import com.example.feperfectsquat.ui.theme.PmCanvas
import com.example.feperfectsquat.ui.theme.PmError
import com.example.feperfectsquat.ui.theme.PmHandle
import com.example.feperfectsquat.ui.theme.PmLine
import com.example.feperfectsquat.ui.theme.PmLineStrong
import com.example.feperfectsquat.ui.theme.PmStripe
import com.example.feperfectsquat.ui.theme.PmSurface
import com.example.feperfectsquat.ui.theme.PmText
import com.example.feperfectsquat.ui.theme.PmTextMuted
import com.example.feperfectsquat.ui.theme.PmTextSecondary
import com.example.feperfectsquat.ui.theme.pmDisplay
import com.example.feperfectsquat.ui.theme.pmEyebrow
import com.example.feperfectsquat.ui.theme.pmMono
import com.example.feperfectsquat.ui.viewmodels.AnalysisStage
import com.example.feperfectsquat.ui.viewmodels.MainViewModel
import com.example.feperfectsquat.video.MIN_RECOMMENDED_FPS
import kotlin.math.roundToInt

private val SQUAT_TIPS = listOf(
    "Film side-on, 2–3 m from the bar.",
    "Keep the whole plate in frame for every rep.",
    "One set per video. Trim long pauses before uploading.",
)
private val JUMP_TIPS = listOf(
    "Film side-on with your whole body in frame.",
    "Keep the phone still, ideally on a tripod or bench.",
    "Record at 60 fps or more for accurate flight time.",
)

@Composable
fun AnalyzeScreen(
    state: MainViewModel.UiState,
    lift: ExerciseType,
    onLiftChange: (ExerciseType) -> Unit,
    loadKg: Double,
    onLoadChange: (Double) -> Unit,
    unit: WeightUnit,
    onPickVideo: () -> Unit,
    onRetry: () -> Unit,
    onDone: () -> Unit,
    onOpenHistory: () -> Unit,
    onWatchVideo: () -> Unit,
) {
    val isSquat = lift == ExerciseType.SQUAT
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Analyze", style = pmDisplay(26.sp), color = PmText, modifier = Modifier.padding(horizontal = 6.dp))
            PmSegmented(
                options = listOf("Squat", "Vertical jump"),
                selectedIndex = if (isSquat) 0 else 1,
                onSelect = { if (!state.isBusy) onLiftChange(if (it == 0) ExerciseType.SQUAT else ExerciseType.VERTICAL_JUMP) },
                modifier = Modifier.fillMaxWidth()
            )
            if (isSquat) LoadStepper(loadKg, unit, onLoadChange)

            when (state.stage) {
                AnalysisStage.IDLE, AnalysisStage.DONE -> UploadDropZone(onPickVideo)
                AnalysisStage.ERROR -> ErrorCard(state.errorMessage, onRetry)
                else -> ProcessingCard(state)
            }

            Column(Modifier.padding(start = 6.dp, end = 6.dp, top = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("FILMING TIPS", style = pmEyebrow(), color = PmTextMuted)
                (if (isSquat) SQUAT_TIPS else JUMP_TIPS).forEachIndexed { i, tip ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("%02d".format(i + 1), style = pmMono(12.sp), color = PmAccent, modifier = Modifier.padding(top = 2.dp))
                        Text(tip, fontSize = 15.sp, lineHeight = 21.sp, color = PmText)
                    }
                }
            }
        }

        if (state.stage == AnalysisStage.DONE) {
            state.analysisResult?.let { result ->
                ResultSheet(
                    state = state,
                    unit = unit,
                    onDone = onDone,
                    onOpenHistory = onOpenHistory,
                    onWatchVideo = onWatchVideo,
                    lowFps = result.lowFpsWarning == true,
                    fps = result.videoFps
                )
            }
        }
    }
}

@Composable
private fun LoadStepper(loadKg: Double, unit: WeightUnit, onLoadChange: (Double) -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(PmSurface)
            .border(1.dp, PmLine, shape)
            .padding(start = 16.dp, top = 6.dp, end = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Load on the bar", modifier = Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PmTextSecondary)
        StepButton("−", 40.dp, "Decrease load") { onLoadChange(-2.5) }
        Text(formatWeight(loadKg, unit), modifier = Modifier.widthIn(min = 84.dp), style = pmMono(18.sp), color = PmText, textAlign = TextAlign.Center)
        StepButton("+", 40.dp, "Increase load") { onLoadChange(2.5) }
    }
}

@Composable
private fun UploadDropZone(onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(PmBackground)
            .drawBehind {
                drawRoundRect(
                    color = PmAccentSoft,
                    cornerRadius = CornerRadius(22.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)))
                )
            }
            .pressable(pressedScale = 0.985f, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.size(56.dp).clip(CircleShape).background(PmAccent), contentAlignment = Alignment.Center) {
            Text("↑", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Text("Upload a video", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PmText)
        Text(
            "MP4 or MOV from your gallery · one set per video",
            fontSize = 14.sp, color = PmTextSecondary, textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProcessingCard(state: MainViewModel.UiState) {
    val isSquat = state.exerciseType == ExerciseType.SQUAT
    val stageLabel = when (state.stage) {
        AnalysisStage.UPLOADING -> "Uploading video…"
        AnalysisStage.ANALYZING -> if (isSquat) "Tracking every rep…" else "Measuring flight time…"
        else -> "Preparing your tracked video…"
    }
    PmCard(padding = 16.dp, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(
            Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(14.dp)).stripes(base = PmStripe, stripe = PmLine),
            contentAlignment = Alignment.Center
        ) {
            PlaceholderLabel(state.fileName)
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(stageLabel, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PmText)
            state.uploadProgress?.let {
                Text("${(it * 100).roundToInt()}%", style = pmMono(14.sp), color = PmAccent)
            }
        }
        val barModifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
        val progress = state.uploadProgress
        if (progress != null) {
            LinearProgressIndicator(
                progress = { progress }, modifier = barModifier, color = PmAccent, trackColor = PmLineStrong,
                gapSize = 0.dp, drawStopIndicator = {}
            )
        } else {
            LinearProgressIndicator(modifier = barModifier, color = PmAccent, trackColor = PmLineStrong, gapSize = 0.dp)
        }
        if (state.stage == AnalysisStage.ANALYZING) {
            Text("This can take a minute. You can keep the app open while we work.", fontSize = 13.sp, color = PmTextMuted)
        }
        state.videoFps?.takeIf { it < MIN_RECOMMENDED_FPS }?.let { fps ->
            Text(
                "This video is ${fps.roundToInt()} fps: jump height may be off by a few centimetres. Record at ${MIN_RECOMMENDED_FPS.toInt()} fps or more.",
                fontSize = 13.sp, color = PmAccentLight
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String?, onRetry: () -> Unit) {
    PmCard(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("We couldn't analyze that video", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = PmText)
        Text(message ?: "Something went wrong.", fontSize = 14.sp, color = PmError)
        PmPrimaryButton("Try again", onClick = onRetry, modifier = Modifier.fillMaxWidth(), height = 52.dp)
    }
}

@Composable
private fun ResultSheet(
    state: MainViewModel.UiState,
    unit: WeightUnit,
    onDone: () -> Unit,
    onOpenHistory: () -> Unit,
    onWatchVideo: () -> Unit,
    lowFps: Boolean,
    fps: Double?,
) {
    val summary = summarize(state.analysisResult ?: return, unit)
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDone)
        )
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(PmBackground)
                // Swallow taps so they do not reach the scrim
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SheetHandle(Modifier.align(Alignment.CenterHorizontally))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(summary.title, style = pmDisplay(22.sp), color = PmText)
                Text(summary.note, fontSize = 14.sp, color = PmTextSecondary)
                if (lowFps) {
                    Text(
                        "Filmed at ${fps?.roundToInt() ?: "?"} fps: record at ${MIN_RECOMMENDED_FPS.toInt()} fps or more for an accurate height.",
                        fontSize = 13.sp, color = PmAccentLight
                    )
                }
            }
            if (summary.bars.isNotEmpty()) {
                BarChart(
                    heights = summary.bars.map { it.fraction },
                    colors = summary.bars.map { it.tone.color() },
                    labels = summary.bars.map { it.label },
                    modifier = Modifier.fillMaxWidth().height(90.dp),
                    gap = 8.dp,
                    radius = 5.dp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                summary.stats.forEach { stat ->
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(PmCanvas).padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(stat.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = PmTextMuted)
                        Text(stat.value, style = pmMono(16.sp), color = PmText)
                    }
                }
            }
            if (state.processedVideoUri != null) {
                Text(
                    "▶  Watch tracked video",
                    modifier = Modifier.clickable(role = Role.Button, onClick = onWatchVideo).padding(vertical = 4.dp),
                    color = PmAccent, fontWeight = FontWeight.SemiBold, fontSize = 15.sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PmOutlineButton("Done", onClick = onDone, modifier = Modifier.weight(1f))
                // Every analysis is already stored by the backend; this jumps to it
                PmPrimaryButton("See in Historic", onClick = onOpenHistory, modifier = Modifier.weight(2f), height = 52.dp)
            }
        }
    }
}

private fun BarTone.color(): Color = when (this) {
    BarTone.TOP -> PmAccent
    BarTone.NORMAL -> PmAccentMid
    BarTone.LOW -> PmAccentSoft
    BarTone.INVALID -> PmHandle
}

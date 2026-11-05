package com.example.feperfectsquat.ui.screens.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.feperfectsquat.ui.components.PlaceholderLabel
import com.example.feperfectsquat.ui.components.PmPrimaryButton
import com.example.feperfectsquat.ui.components.stripes
import com.example.feperfectsquat.ui.theme.PmAccent
import com.example.feperfectsquat.ui.theme.PmBackground
import com.example.feperfectsquat.ui.theme.PmHandle
import com.example.feperfectsquat.ui.theme.PmLine
import com.example.feperfectsquat.ui.theme.PmText
import com.example.feperfectsquat.ui.theme.PmTextSecondary
import com.example.feperfectsquat.ui.theme.pmDisplay
import com.example.feperfectsquat.ui.theme.pmEyebrow
import com.example.feperfectsquat.ui.theme.pmMono
import kotlinx.coroutines.launch

private data class Slide(
    val tag: String,
    val title: String,
    val body: String,
    val image: String,
    val chipKey: String,
    val chipValue: String
)

private val SLIDES = listOf(
    Slide(
        "SQUAT", "Squat smarter.",
        "Upload a video of your set. We measure bar speed, power and velocity loss on every rep, and estimate your 1RM without maxing out.",
        "ATHLETE SQUATTING, SIDE VIEW — PHOTO", "MEAN VELOCITY", "0.62 m/s"
    ),
    Slide(
        "VERTICAL JUMP", "Jump higher.",
        "Film a countermovement jump and get your jump height, flight time and peak power from a single video.",
        "ATHLETE MID-JUMP — PHOTO", "JUMP HEIGHT", "41.2 cm"
    ),
    Slide(
        "COMMUNITY", "Lift together.",
        "Coach your athletes from anywhere, compare PRs with the community and celebrate every new best.",
        "COACH + ATHLETE — PHOTO", "NEW PR", "Squat 150 kg"
    ),
)

/** Three-slide welcome carousel; [onFinish] runs on "Skip" or "Get started". */
@Composable
fun WelcomeScreen(onFinish: () -> Unit, initialSlide: Int = 0) {
    val pager = rememberPagerState(initialPage = initialSlide) { SLIDES.size }
    val scope = rememberCoroutineScope()
    val lastSlide = pager.currentPage == SLIDES.lastIndex

    Column(
        Modifier
            .fillMaxSize()
            .background(PmBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrandMark()
            Text(
                "Skip",
                color = PmTextSecondary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.clickable(role = Role.Button, onClick = onFinish).padding(vertical = 8.dp)
            )
        }

        HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { page ->
            SlidePage(SLIDES[page])
        }

        Row(
            Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SLIDES.indices.forEach { i ->
                    val selected = i == pager.currentPage
                    val width by animateDpAsState(if (selected) 24.dp else 8.dp, label = "dotWidth")
                    Box(
                        Modifier
                            .size(width = width, height = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (selected) PmAccent else PmHandle)
                            .clickable(onClickLabel = "Go to slide ${i + 1}") { scope.launch { pager.animateScrollToPage(i) } }
                    )
                }
            }
            PmPrimaryButton(
                text = if (lastSlide) "Get started" else "Next",
                onClick = { if (lastSlide) onFinish() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
                modifier = Modifier.width(if (lastSlide) 160.dp else 112.dp),
            )
        }
    }
}

@Composable
private fun SlidePage(slide: Slide) {
    Column(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(22.dp))
                .border(1.dp, PmLine, RoundedCornerShape(22.dp))
                .stripes(),
            contentAlignment = Alignment.Center
        ) {
            PlaceholderLabel(slide.image)
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PmAccent)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(slide.chipKey, style = pmEyebrow(9.sp), color = Color.White.copy(alpha = 0.8f))
                Text(slide.chipValue, style = pmMono(20.sp), color = Color.White)
            }
        }
        Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 20.dp)) {
            Text(slide.tag, style = pmEyebrow(), color = PmAccent)
            Spacer(Modifier.height(10.dp))
            Text(slide.title, style = pmDisplay(40.sp), color = PmText)
            Spacer(Modifier.height(14.dp))
            Text(slide.body, fontSize = 16.sp, lineHeight = 23.sp, color = PmTextSecondary)
        }
    }
}

@Composable
fun BrandMark() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(16.dp).clip(RoundedCornerShape(4.dp)).background(PmAccent))
        Text("POWERMETRICS", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, letterSpacing = 0.04.em, color = PmText)
    }
}

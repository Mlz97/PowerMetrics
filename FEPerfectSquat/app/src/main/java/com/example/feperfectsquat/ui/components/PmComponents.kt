package com.example.feperfectsquat.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feperfectsquat.ui.theme.PmAccent
import com.example.feperfectsquat.ui.theme.PmAccentSoft
import com.example.feperfectsquat.ui.theme.PmCanvas
import com.example.feperfectsquat.ui.theme.PmHandle
import com.example.feperfectsquat.ui.theme.PmLine
import com.example.feperfectsquat.ui.theme.PmLineStrong
import com.example.feperfectsquat.ui.theme.PmStripe
import com.example.feperfectsquat.ui.theme.PmSurface
import com.example.feperfectsquat.ui.theme.PmText
import com.example.feperfectsquat.ui.theme.PmTextFaint
import com.example.feperfectsquat.ui.theme.PmTextMuted
import com.example.feperfectsquat.ui.theme.PmTextSecondary
import com.example.feperfectsquat.ui.theme.pmMono

/** Clickable that shrinks slightly while pressed, like the design's `style-active` scale. */
fun Modifier.pressable(
    enabled: Boolean = true,
    pressedScale: Float = 0.97f,
    onClick: () -> Unit
): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) pressedScale else 1f, label = "pressScale")
    graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = interaction, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
}

/** Full-width orange call to action (56 dp, radius 16). */
@Composable
fun PmPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 56.dp
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) PmAccent else PmAccentSoft)
            .pressable(enabled = enabled, pressedScale = 0.98f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
    }
}

@Composable
fun PmOutlineButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, PmLineStrong, RoundedCornerShape(16.dp))
            .pressable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = PmText, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

/** Rounded dark card with the design's hairline border. */
@Composable
fun PmCard(
    modifier: Modifier = Modifier,
    background: Color = PmSurface,
    radius: Dp = 22.dp,
    padding: Dp = 18.dp,
    onClick: (() -> Unit)? = null,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(radius)
    Column(
        modifier = modifier
            .clip(shape)
            .background(background)
            .border(1.dp, PmLine, shape)
            .then(if (onClick != null) Modifier.pressable(pressedScale = 0.98f, onClick = onClick) else Modifier)
            .padding(padding),
        verticalArrangement = verticalArrangement,
        content = content
    )
}

/** Segmented control: one option is filled, the rest are transparent. */
@Composable
fun PmSegmented(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 44.dp,
    containerColor: Color = PmSurface,
    containerBorder: Boolean = true,
    outerRadius: Dp = 14.dp,
    innerRadius: Dp = 10.dp,
    selectedColor: Color = PmAccent,
    selectedTextColor: Color = Color.White,
    unselectedTextColor: Color = PmTextSecondary,
    fontSize: TextUnit = 15.sp,
    fillWidth: Boolean = true
) {
    val outer = RoundedCornerShape(outerRadius)
    Row(
        modifier = modifier
            .clip(outer)
            .background(containerColor)
            .then(if (containerBorder) Modifier.border(1.dp, PmLine, outer) else Modifier)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .then(if (fillWidth) Modifier.weight(1f) else Modifier)
                    .height(itemHeight)
                    .clip(RoundedCornerShape(innerRadius))
                    .background(if (selected) selectedColor else Color.Transparent)
                    .clickable(role = Role.Tab) { onSelect(index) }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (selected) selectedTextColor else unselectedTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize
                )
            }
        }
    }
}

/** Square −/+ button used by the steppers. */
@Composable
fun StepButton(symbol: String, size: Dp, contentDescription: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(PmCanvas)
            .clickable(role = Role.Button, onClickLabel = contentDescription, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, color = PmText, fontSize = 20.sp)
    }
}

/** Value between a − and + button inside a bordered field. */
@Composable
fun PmStepper(
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 38.dp,
    valueStyle: TextStyle = pmMono(20.sp)
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(PmSurface)
            .border(1.5.dp, PmLineStrong, shape)
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepButton("−", buttonSize, "Decrease $label", onMinus)
        Text(value, modifier = Modifier.weight(1f), style = valueStyle, color = PmText, textAlign = TextAlign.Center)
        StepButton("+", buttonSize, "Increase $label", onPlus)
    }
}

/** Metric tile: label, value with unit, and a delta line. */
@Composable
fun StatTile(label: String, value: String, unit: String, delta: String, deltaColor: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(PmCanvas)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(label, color = PmTextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Text(
            buildAnnotatedString {
                append(value)
                withStyle(SpanStyle(fontSize = 12.sp, color = PmTextSecondary)) { append(" $unit") }
            },
            style = pmMono(22.sp),
            color = PmText
        )
        Text(delta, style = pmMono(11.sp, FontWeight.SemiBold), color = deltaColor)
    }
}

/** Two equal-width columns, like the design's `grid-template-columns: 1fr 1fr`. */
@Composable
fun <T> TwoColumnGrid(items: List<T>, gap: Dp = 8.dp, cell: @Composable RowScope.(T) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                row.forEach { cell(it) }
                if (row.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}

/** Vertical bars scaled to [heights] (0..1), optionally with a value label above each one. */
@Composable
fun BarChart(
    heights: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    labels: List<String>? = null,
    gap: Dp = 6.dp,
    radius: Dp = 4.dp
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.Bottom) {
        heights.forEachIndexed { i, h ->
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Bottom)
            ) {
                labels?.let { Text(it[i], style = pmMono(10.sp, FontWeight.SemiBold), color = PmTextSecondary) }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(h.coerceIn(0.03f, 1f))
                        .clip(RoundedCornerShape(radius))
                        .background(colors[i])
                )
            }
        }
    }
}

/** Diagonal-stripe placeholder standing in for photos and video frames. */
fun Modifier.stripes(base: Color = PmSurface, stripe: Color = PmStripe, width: Dp = 10.dp): Modifier = drawBehind {
    drawRect(base)
    val step = width.toPx() * 2
    var x = -size.height
    while (x < size.width) {
        drawLine(
            color = stripe,
            start = Offset(x, size.height),
            end = Offset(x + size.height, 0f),
            strokeWidth = width.toPx() * 0.7f
        )
        x += step
    }
}

@Composable
fun PlaceholderLabel(text: String) {
    Text(text, style = pmMono(11.sp, FontWeight.Medium), color = PmTextFaint, letterSpacing = 0.66.sp, textAlign = TextAlign.Center)
}

/** Grabber at the top of a bottom sheet. */
@Composable
fun SheetHandle(modifier: Modifier = Modifier) {
    Box(
        modifier
            .padding(bottom = 2.dp)
            .size(width = 40.dp, height = 5.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(PmHandle)
    )
}

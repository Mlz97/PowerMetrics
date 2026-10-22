package com.example.feperfectsquat.ui.screens.onboarding

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feperfectsquat.models.ProfileDraft
import com.example.feperfectsquat.models.Sex
import com.example.feperfectsquat.models.WeightUnit
import com.example.feperfectsquat.models.formatBodyWeight
import com.example.feperfectsquat.ui.components.PmPrimaryButton
import com.example.feperfectsquat.ui.components.PmSegmented
import com.example.feperfectsquat.ui.components.PmStepper
import com.example.feperfectsquat.ui.theme.PmAccent
import com.example.feperfectsquat.ui.theme.PmBackground
import com.example.feperfectsquat.ui.theme.PmCanvas
import com.example.feperfectsquat.ui.theme.PmLineStrong
import com.example.feperfectsquat.ui.theme.PmSans
import com.example.feperfectsquat.ui.theme.PmSurface
import com.example.feperfectsquat.ui.theme.PmText
import com.example.feperfectsquat.ui.theme.PmTextFaint
import com.example.feperfectsquat.ui.theme.PmTextMuted
import com.example.feperfectsquat.ui.theme.PmTextSecondary
import com.example.feperfectsquat.ui.theme.pmDisplay
import com.example.feperfectsquat.ui.theme.pmMono

/** "Let's get to know you": name, age, units, sex, bodyweight and height. */
@Composable
fun ProfileScreen(
    draft: ProfileDraft,
    onDraftChange: ((ProfileDraft) -> ProfileDraft) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(PmBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
    ) {
        Box(
            Modifier
                .padding(start = 24.dp, top = 12.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(PmCanvas)
                .clickable(role = Role.Button, onClickLabel = "Back", onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("←", color = PmText, fontSize = 18.sp)
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 28.dp)
        ) {
            Text("Let's get to know you", style = pmDisplay(30.sp), color = PmText)
            Spacer(Modifier.height(10.dp))
            Text(
                "A few basics so we can calculate your power output accurately.",
                fontSize = 15.sp, lineHeight = 22.sp, color = PmTextSecondary
            )

            FieldLabel("First name", top = 24.dp)
            NameField(draft.name) { name -> onDraftChange { it.copy(name = name) } }

            Row(Modifier.padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    FieldLabel("Age")
                    PmStepper(
                        value = draft.age.toString(),
                        onMinus = { onDraftChange { it.withAge(-1) } },
                        onPlus = { onDraftChange { it.withAge(1) } },
                        label = "age"
                    )
                }
                Column(Modifier.weight(1f)) {
                    FieldLabel("Units")
                    PmSegmented(
                        options = WeightUnit.entries.map { it.label },
                        selectedIndex = draft.unit.ordinal,
                        onSelect = { i -> onDraftChange { it.copy(unit = WeightUnit.entries[i]) } },
                        itemHeight = 40.dp,
                        containerColor = PmCanvas,
                        containerBorder = false,
                        innerRadius = 8.dp,
                        selectedColor = PmSurface,
                        selectedTextColor = PmText,
                        unselectedTextColor = PmTextMuted,
                    )
                }
            }

            FieldLabel("Sex", top = 20.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Sex.entries.forEach { sex ->
                    val selected = draft.sex == sex
                    val shape = RoundedCornerShape(14.dp)
                    Box(
                        Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(shape)
                            .background(if (selected) PmAccent else PmSurface)
                            .border(2.dp, if (selected) PmAccent else PmLineStrong, shape)
                            .clickable(role = Role.RadioButton) { onDraftChange { it.copy(sex = sex) } },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(sex.label, color = if (selected) Color.White else PmText, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                }
            }

            FieldLabel("Bodyweight", top = 20.dp)
            PmStepper(
                value = formatBodyWeight(draft.bodyWeightKg, draft.unit),
                onMinus = { onDraftChange { it.withBodyWeight(-0.5) } },
                onPlus = { onDraftChange { it.withBodyWeight(0.5) } },
                label = "bodyweight",
                buttonSize = 44.dp,
                valueStyle = pmMono(24.sp)
            )

            FieldLabel("Height", top = 20.dp)
            PmStepper(
                value = "${draft.heightCm.toInt()} cm",
                onMinus = { onDraftChange { it.withHeight(-1.0) } },
                onPlus = { onDraftChange { it.withHeight(1.0) } },
                label = "height",
                buttonSize = 44.dp,
                valueStyle = pmMono(24.sp)
            )
            Spacer(Modifier.height(8.dp))
            Text("Used to measure jump depth in your videos.", fontSize = 12.sp, color = PmTextMuted)
            Spacer(Modifier.height(16.dp))
        }

        PmPrimaryButton(
            text = "Continue",
            onClick = onContinue,
            enabled = draft.canContinue,
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 20.dp)
        )
    }
}

@Composable
private fun FieldLabel(text: String, top: Dp = 0.dp) {
    Text(
        text,
        modifier = Modifier.padding(top = top, bottom = 8.dp),
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = PmTextSecondary
    )
}

@Composable
private fun NameField(value: String, onValueChange: (String) -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(fontFamily = PmSans, fontSize = 17.sp, color = PmText),
        cursorBrush = SolidColor(PmAccent),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Box(
                Modifier
                    .height(52.dp)
                    .clip(shape)
                    .background(PmSurface)
                    .border(1.5.dp, if (value.isEmpty()) PmLineStrong else PmAccent.copy(alpha = 0.6f), shape)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) Text("Alex", fontSize = 17.sp, color = PmTextFaint)
                inner()
            }
        }
    )
}

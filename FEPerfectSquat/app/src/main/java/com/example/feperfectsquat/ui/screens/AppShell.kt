package com.example.feperfectsquat.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feperfectsquat.ui.theme.PmAccent
import com.example.feperfectsquat.ui.theme.PmAccentTint
import com.example.feperfectsquat.ui.theme.PmBackground
import com.example.feperfectsquat.ui.theme.PmCanvas
import com.example.feperfectsquat.ui.theme.PmLineStrong
import com.example.feperfectsquat.ui.theme.PmText
import com.example.feperfectsquat.ui.theme.PmTextMuted

enum class AppTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Outlined.Home),
    ANALYZE("Analyze", Icons.Outlined.FileUpload),
    HISTORY("Historic", Icons.Outlined.Timeline),
    COMMUNITY("Community", Icons.Outlined.EmojiEvents),
}

/** Tabbed app frame: the selected tab's content above the bottom navigation bar. */
@Composable
fun AppShell(
    tab: AppTab,
    onTabChange: (AppTab) -> Unit,
    content: @Composable (AppTab) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(PmCanvas)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
    ) {
        Box(Modifier.weight(1f)) { content(tab) }
        HorizontalDivider(color = PmLineStrong, thickness = 1.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .background(PmBackground)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            AppTab.entries.forEach { item ->
                TabItem(item, selected = item == tab, onClick = { onTabChange(item) }, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TabItem(tab: AppTab, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val pill by animateColorAsState(if (selected) PmAccentTint else Color.Transparent, label = "tabPill")
    Column(
        modifier.clickable(role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            Modifier.size(width = 56.dp, height = 30.dp).clip(RoundedCornerShape(15.dp)).background(pill),
            contentAlignment = Alignment.Center
        ) {
            Icon(tab.icon, contentDescription = null, tint = if (selected) PmAccent else PmTextMuted, modifier = Modifier.size(18.dp))
        }
        Text(
            tab.label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) PmText else PmTextMuted
        )
    }
}

package com.jjrapps.sleepnoise.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jjrapps.sleepnoise.R
import com.jjrapps.sleepnoise.ui.common.ScreenHeader
import com.jjrapps.sleepnoise.ui.theme.SleepNoiseColors
import com.jjrapps.sleepnoise.ui.theme.SleepNoiseTheme

/**
 * Settings. The two sections of the specification §4 — playback and about — land in
 * H7; for now the screen exists so the navigation and the theme can be verified.
 */
@Composable
fun SettingsScreen(onBack: () -> Unit, onOpenChangelog: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        ScreenHeader(
            title = stringResource(R.string.settings_title),
            onBack = onBack,
            modifier = Modifier.padding(end = 24.dp, top = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clickable(onClick = onOpenChangelog)
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.changelog_title),
                style = MaterialTheme.typography.bodyLarge,
                color = SleepNoiseColors.OnBackground
            )
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = SleepNoiseColors.OnBackgroundMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    SleepNoiseTheme {
        SettingsScreen(onBack = {}, onOpenChangelog = {})
    }
}

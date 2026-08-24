package com.jjrapps.sleepnoise.ui.player

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jjrapps.sleepnoise.R
import com.jjrapps.sleepnoise.ui.theme.SleepNoiseColors
import com.jjrapps.sleepnoise.ui.theme.SleepNoiseTheme
import com.jjrapps.sleepnoise.ui.timer.TimerSheet

/**
 * The player. In this milestone it carries the frame — wordmark, sound name, timer
 * row — and none of the controls: the volume ring, the play button and the sound
 * pills arrive in H4, once there is a service to talk to.
 */
@Composable
fun PlayerScreen(onOpenSettings: () -> Unit) {
    var showTimerSheet by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.app_name).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = SleepNoiseColors.OnBackgroundMuted,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onOpenSettings) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings),
                    contentDescription = stringResource(R.string.a11y_open_settings),
                    tint = SleepNoiseColors.OnBackgroundVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.sound_brown_name),
                style = MaterialTheme.typography.displaySmall,
                color = SleepNoiseColors.OnBackground,
                textAlign = TextAlign.Center
            )
        }

        HorizontalDivider(thickness = 1.dp, color = SleepNoiseColors.Outline)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .clickable { showTimerSheet = true },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_timer),
                    contentDescription = null,
                    tint = SleepNoiseColors.OnBackgroundVariant,
                    modifier = Modifier.size(19.dp)
                )
                Text(
                    text = stringResource(R.string.timer_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = SleepNoiseColors.OnBackgroundVariant,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.timer_none),
                    style = MaterialTheme.typography.bodyLarge,
                    color = SleepNoiseColors.OnBackgroundMuted
                )
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = SleepNoiseColors.OnBackgroundMuted,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(16.dp)
                )
            }
        }
    }

    if (showTimerSheet) {
        TimerSheet(onDismiss = { showTimerSheet = false })
    }
}

@Preview
@Composable
private fun PlayerScreenPreview() {
    SleepNoiseTheme {
        PlayerScreen(onOpenSettings = {})
    }
}

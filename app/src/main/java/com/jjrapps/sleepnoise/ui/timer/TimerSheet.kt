package com.jjrapps.sleepnoise.ui.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jjrapps.sleepnoise.R
import com.jjrapps.sleepnoise.ui.theme.SleepNoiseColors

/**
 * The sleep timer, as a modal sheet over the player rather than a destination: it is
 * a choice made on top of what is playing, and putting it in the back stack would
 * mean pressing back twice to leave the app.
 *
 * Presets in minutes, from the specification §4. They are also what the screenshot
 * pipeline of H9 photographs, so changing them changes the store listing.
 */
val TIMER_PRESETS = listOf(15, 30, 45, 60, 90, 120)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerSheet(
    selectedMinutes: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SleepNoiseColors.SurfaceRaised
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.timer_title),
                style = MaterialTheme.typography.titleLarge,
                color = SleepNoiseColors.OnBackground,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )
            TimerOption(
                label = stringResource(R.string.timer_none),
                selected = selectedMinutes == 0,
                onClick = { onSelect(0) }
            )
            TIMER_PRESETS.forEach { minutes ->
                TimerOption(
                    label = labelFor(minutes),
                    selected = selectedMinutes == minutes,
                    onClick = { onSelect(minutes) }
                )
            }
        }
    }
}

@Composable
private fun labelFor(minutes: Int): String = when {
    minutes % 60 == 0 -> stringResource(R.string.timer_hours_only, minutes / 60)
    minutes > 60 -> stringResource(R.string.timer_hours_minutes, minutes / 60, minutes % 60)
    else -> stringResource(R.string.timer_minutes_only, minutes)
}

@Composable
private fun TimerOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                if (selected) SleepNoiseColors.SurfaceSelected else SleepNoiseColors.SurfaceRaised,
                MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) SleepNoiseColors.OnBackground
            else SleepNoiseColors.OnBackgroundVariant
        )
        if (selected) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = SleepNoiseColors.Accent,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

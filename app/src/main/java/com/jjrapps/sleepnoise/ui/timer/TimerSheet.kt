package com.jjrapps.sleepnoise.ui.timer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jjrapps.sleepnoise.R
import com.jjrapps.sleepnoise.ui.theme.SleepNoiseColors

/**
 * The sleep timer, as a modal sheet rather than a destination: it is a choice made on
 * top of the player, and putting it in the back stack would mean pressing back twice
 * to leave the app. The presets and the countdown arrive in H6.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SleepNoiseColors.SurfaceRaised
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(R.string.timer_title),
                style = MaterialTheme.typography.titleLarge,
                color = SleepNoiseColors.OnBackground
            )
            Text(
                text = stringResource(R.string.timer_none),
                style = MaterialTheme.typography.bodyLarge,
                color = SleepNoiseColors.OnBackgroundMuted,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

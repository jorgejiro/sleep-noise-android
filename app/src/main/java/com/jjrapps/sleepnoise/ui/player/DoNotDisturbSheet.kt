package com.jjrapps.sleepnoise.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jjrapps.sleepnoise.R
import com.jjrapps.sleepnoise.ui.theme.SleepNoiseColors
import com.jjrapps.sleepnoise.ui.theme.SleepNoiseTheme

/**
 * Explains, once, why an app for making noise wants to silence the phone — and asks
 * for the access it cannot grant itself (RF-22).
 *
 * A sheet over the player at the first sound, and not a welcome screen: the app opens
 * playing, and putting anything in front of that would break the one promise it makes.
 * This is the same reasoning, and the same moment, as the notification permission in
 * [NotificationPermissionRequest] — it is asked where it means something.
 *
 * Whatever the user does with it, it never comes back. Dismissing it counts as an
 * answer: an app asking twice for something it was already refused is nagging, and the
 * switch in Settings is there for anyone who changes their mind.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoNotDisturbSheet(
    onGrant: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SleepNoiseColors.SurfaceRaised
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.dnd_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                color = SleepNoiseColors.OnBackground
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.dnd_sheet_body),
                style = MaterialTheme.typography.bodyMedium,
                color = SleepNoiseColors.OnBackgroundVariant
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.dnd_sheet_alarms),
                style = MaterialTheme.typography.bodyMedium,
                color = SleepNoiseColors.OnBackgroundVariant
            )
            Spacer(Modifier.height(10.dp))
            // Dice dónde va a aterrizar y qué buscar allí, porque el intent que lleva
            // directo a la fila de esta app no es API pública: la pantalla del sistema
            // es una lista de aplicaciones y hay que encontrarse en ella.
            Text(
                text = stringResource(R.string.dnd_sheet_where),
                style = MaterialTheme.typography.labelMedium,
                color = SleepNoiseColors.OnBackgroundMuted
            )
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.dnd_sheet_not_now),
                        style = MaterialTheme.typography.bodyLarge,
                        color = SleepNoiseColors.OnBackgroundVariant
                    )
                }
                Spacer(Modifier.padding(horizontal = 4.dp))
                Button(
                    onClick = onGrant,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SleepNoiseColors.Accent,
                        contentColor = SleepNoiseColors.OnAccent
                    )
                ) {
                    Text(
                        text = stringResource(R.string.dnd_sheet_grant),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun DoNotDisturbSheetPreview() {
    SleepNoiseTheme { DoNotDisturbSheet(onGrant = {}, onDismiss = {}) }
}

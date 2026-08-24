package com.jjrapps.sleepnoise.ui.player

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jjrapps.sleepnoise.R
import com.jjrapps.sleepnoise.domain.model.NoiseType
import com.jjrapps.sleepnoise.playback.PlaybackState
import com.jjrapps.sleepnoise.ui.common.animationsEnabled
import com.jjrapps.sleepnoise.ui.common.iconRes
import com.jjrapps.sleepnoise.ui.common.nameRes
import com.jjrapps.sleepnoise.ui.common.shortNameRes
import com.jjrapps.sleepnoise.ui.common.formatRemaining
import com.jjrapps.sleepnoise.ui.theme.SleepNoiseColors
import com.jjrapps.sleepnoise.ui.theme.SleepNoiseTheme
import com.jjrapps.sleepnoise.ui.timer.TimerSheet

@Composable
fun PlayerScreen(
    onOpenSettings: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.connect() }
    NotificationPermissionRequest()

    PlayerContent(
        state = state,
        onOpenSettings = onOpenSettings,
        onTogglePlay = viewModel::togglePlay,
        onSelectNoise = viewModel::selectNoise,
        onVolumeChange = viewModel::setVolume,
        onTimerChange = viewModel::setTimer
    )
}

@Composable
private fun PlayerContent(
    state: PlaybackState,
    onOpenSettings: () -> Unit,
    onTogglePlay: () -> Unit,
    onSelectNoise: (NoiseType) -> Unit,
    onVolumeChange: (Int) -> Unit,
    onTimerChange: (Int) -> Unit
) {
    var showTimerSheet by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BreathingHalo(playing = state.isPlaying) {
                VolumeRing(
                    volume = state.volume,
                    onVolumeChange = onVolumeChange,
                    accessibilityLabel = stringResource(R.string.a11y_volume_ring)
                ) {
                    PlayButton(isPlaying = state.isPlaying, onClick = onTogglePlay)
                }
            }

            Spacer(Modifier.height(30.dp))
            Text(
                text = stringResource(state.noise.nameRes()),
                style = MaterialTheme.typography.displaySmall,
                color = SleepNoiseColors.OnBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(9.dp))
            Text(
                text = stringResource(R.string.player_volume_label, state.volume).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = SleepNoiseColors.OnBackgroundMuted
            )
        }

        // The plain slider: the ring above is the shortcut, this is the path that
        // does not need discovering. ADR 002.
        Slider(
            value = state.volume.toFloat(),
            onValueChange = { onVolumeChange(it.toInt()) },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = SleepNoiseColors.Accent,
                activeTrackColor = SleepNoiseColors.Accent,
                inactiveTrackColor = SleepNoiseColors.Outline
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        )

        // Two by two rather than a row of four: at 390 dp a fourth pill leaves room
        // for about five characters, and "Enmascarador" is not five characters. The
        // grid also keeps every target well past 48 dp.
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            NoiseType.entries.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { type ->
                        NoisePill(
                            type = type,
                            selected = state.noise == type,
                            onClick = { onSelectNoise(type) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        HorizontalDivider(thickness = 1.dp, color = SleepNoiseColors.Outline)
        TimerRow(
            state = state,
            onClick = { showTimerSheet = true }
        )
    }

    if (showTimerSheet) {
        TimerSheet(
            selectedMinutes = state.timerMinutes,
            onSelect = {
                onTimerChange(it)
                showTimerSheet = false
            },
            onDismiss = { showTimerSheet = false }
        )
    }
}

/**
 * Asks for the notification permission, once, when the player first appears.
 *
 * Asked here and not at some earlier splash because this is the moment it means
 * something: from now on there is sound, and the notification is the only way to
 * pause it without opening the app. Denying it does not break anything — the noise
 * plays either way — so there is no second ask and no nagging.
 */
@Composable
private fun NotificationPermissionRequest() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current
    val granted = ContextCompat.checkSelfPermission(
        context, Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
    var asked by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    LaunchedEffect(granted, asked) {
        if (!granted && !asked) {
            asked = true
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

/**
 * The halo breathes on a nine-second cycle — the pace the app is asking the user to
 * slow down to. It stops when nothing is playing, and it stops when the system has
 * animations turned off, which is an accessibility setting and not a preference.
 */
@Composable
private fun BreathingHalo(playing: Boolean, content: @Composable () -> Unit) {
    val animate = playing && animationsEnabled()
    val scale = if (!animate) 1f else {
        val transition = rememberInfiniteTransition(label = "halo")
        transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.09f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4_500),
                repeatMode = RepeatMode.Reverse
            ),
            label = "halo-scale"
        ).value
    }
    Box(contentAlignment = Alignment.Center, modifier = Modifier.graphicsLayer { }) {
        Box(modifier = Modifier.scale(scale)) { content() }
    }
}

@Composable
private fun PlayButton(isPlaying: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(100.dp)
            .background(SleepNoiseColors.SurfaceRaised, CircleShape)
            .clickable(onClick = onClick)
    ) {
        Icon(
            painter = painterResource(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            ),
            contentDescription = stringResource(
                if (isPlaying) R.string.a11y_pause else R.string.a11y_play
            ),
            tint = SleepNoiseColors.Accent,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
private fun NoisePill(
    type: NoiseType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = if (selected) SleepNoiseColors.SurfaceSelected else SleepNoiseColors.Surface
    val border = if (selected) SleepNoiseColors.Accent else SleepNoiseColors.Outline
    val ink = if (selected) SleepNoiseColors.OnBackground else SleepNoiseColors.OnBackgroundMuted
    Row(
        modifier = modifier
            .height(58.dp)
            .background(background, MaterialTheme.shapes.medium)
            .border(1.dp, border, MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(type.iconRes()),
            contentDescription = null,
            tint = ink,
            modifier = Modifier
                .padding(end = 9.dp)
                .size(17.dp)
        )
        Text(
            text = stringResource(type.shortNameRes()),
            style = MaterialTheme.typography.bodyLarge,
            color = ink,
            maxLines = 1
        )
    }
}

@Composable
private fun TimerRow(state: PlaybackState, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .clickable(onClick = onClick),
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
                text = if (state.hasTimer) {
                    formatRemaining(state.timerRemainingMillis)
                } else {
                    stringResource(R.string.timer_none)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = if (state.hasTimer) SleepNoiseColors.Accent
                else SleepNoiseColors.OnBackgroundMuted
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

@Preview
@Composable
private fun PlayerPreview() {
    SleepNoiseTheme {
        PlayerContent(
            state = PlaybackState(connected = true, isPlaying = true, volume = 50),
            onOpenSettings = {}, onTogglePlay = {}, onSelectNoise = {},
            onVolumeChange = {}, onTimerChange = {}
        )
    }
}

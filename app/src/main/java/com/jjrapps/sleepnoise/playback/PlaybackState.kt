package com.jjrapps.sleepnoise.playback

import com.jjrapps.sleepnoise.domain.model.NoiseType

/**
 * Everything the player screen needs, and nothing it does not. One immutable value
 * so a recomposition can never see half of a state change.
 */
data class PlaybackState(
    val connected: Boolean = false,
    val isPlaying: Boolean = false,
    val noise: NoiseType = NoiseType.Default,
    /** What the user sees on the dial, 0..100, not a gain. */
    val volume: Int = PlaybackService.DEFAULT_VOLUME,
    /** Length of the running timer in minutes; zero when there is none. */
    val timerMinutes: Int = 0,
    val timerRemainingMillis: Long = 0L
) {
    val hasTimer: Boolean get() = timerMinutes > 0
}

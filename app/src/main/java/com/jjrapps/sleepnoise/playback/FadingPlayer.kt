package com.jjrapps.sleepnoise.playback

import androidx.annotation.OptIn
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Wraps the player so that nothing ever starts or stops abruptly, and so that
 * exactly one thing owns the gain.
 *
 * **Why a wrapper.** Pause arrives from three places — the button, the notification
 * and a headset — and all three go through `Player.pause()`. Fading in the screen's
 * click handler would leave the other two abrupt. Wrapping is the only place that
 * catches all of them.
 *
 * **Why one owner of the gain.** Three separate things want to scale the volume: the
 * user's own setting, the fade around play and pause, and the sleep timer's closing
 * fade. Written independently they overwrite each other — the timer's fade would be
 * undone by a volume tap, or a pause fade would cancel the timer's. So each keeps
 * its own factor here and the player only ever sees the product.
 */
@OptIn(UnstableApi::class)
class FadingPlayer(
    private val player: ExoPlayer,
    private val scope: CoroutineScope
) : ForwardingPlayer(player) {

    private var userGain = 1f
    private var transitionGain = 1f
    private var timerGain = 1f
    private var fadeJob: Job? = null

    /** The user's volume, 0..100. */
    fun setUserVolume(volume: Int) {
        userGain = volumeToGain(volume)
        apply()
    }

    /**
     * The sleep timer's closing fade, from 0 (untouched) to 1 (silent). Squared so
     * the ramp is even in decibels: a ramp that is linear in amplitude sounds like it
     * hangs near the top and then drops away at the end.
     */
    fun setTimerFade(progress: Float) {
        val remaining = (1f - progress.coerceIn(0f, 1f))
        timerGain = remaining * remaining
        apply()
    }

    override fun play() {
        if (player.isPlaying) return
        transitionGain = 0f
        apply()
        player.play()
        fade(to = 1f, durationMillis = FADE_IN_MILLIS)
    }

    override fun pause() {
        if (!player.isPlaying) {
            player.pause()
            return
        }
        fade(to = 0f, durationMillis = FADE_OUT_MILLIS) {
            player.pause()
            // Left ready for the next start, which begins from silence again.
            transitionGain = 0f
            apply()
        }
    }

    /** Pauses without a fade, for when the sound is already silent. */
    fun pauseImmediately() {
        fadeJob?.cancel()
        player.pause()
        transitionGain = 0f
        apply()
    }

    fun releaseFades() {
        fadeJob?.cancel()
    }

    private fun fade(to: Float, durationMillis: Long, onEnd: () -> Unit = {}) {
        fadeJob?.cancel()
        val from = transitionGain
        fadeJob = scope.launch {
            val steps = (durationMillis / STEP_MILLIS).toInt().coerceAtLeast(1)
            for (step in 1..steps) {
                transitionGain = from + (to - from) * step / steps
                apply()
                delay(STEP_MILLIS)
            }
            transitionGain = to
            apply()
            onEnd()
        }
    }

    private fun apply() {
        player.volume = userGain * transitionGain * timerGain
    }

    companion object {
        /** RF-01 and RF-05: starting is gentle. */
        const val FADE_IN_MILLIS = 1_500L

        /** RF-05: stopping is quick but not abrupt. */
        const val FADE_OUT_MILLIS = 400L

        /** About one frame; finer than this is inaudible and just burns CPU. */
        const val STEP_MILLIS = 16L
    }
}

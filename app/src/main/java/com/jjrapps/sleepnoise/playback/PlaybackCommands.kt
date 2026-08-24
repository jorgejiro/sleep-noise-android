package com.jjrapps.sleepnoise.playback

import android.os.Bundle
import androidx.media3.session.SessionCommand
import com.jjrapps.sleepnoise.domain.model.NoiseType

/**
 * The app's own commands and state, on top of what a media session already knows.
 *
 * A media session speaks play, pause and volume fluently and knows nothing about
 * which colour of noise is playing or how long is left on a sleep timer. Custom
 * commands carry the app's questions to the service, and the session's extras carry
 * the answers back — which is the plumbing Media3 provides for exactly this, rather
 * than a second channel of our own alongside it.
 */
object PlaybackCommands {

    const val SET_NOISE = "com.jjrapps.sleepnoise.SET_NOISE"
    const val SET_VOLUME = "com.jjrapps.sleepnoise.SET_VOLUME"
    const val SET_TIMER = "com.jjrapps.sleepnoise.SET_TIMER"
    const val EXTEND_TIMER = "com.jjrapps.sleepnoise.EXTEND_TIMER"

    const val ARG_NOISE = "noise"
    const val ARG_VOLUME = "volume"
    const val ARG_MINUTES = "minutes"

    /** Extras the session publishes so the UI can render state it does not own. */
    const val EXTRA_NOISE = "noise"
    const val EXTRA_VOLUME = "volume"
    const val EXTRA_TIMER_MINUTES = "timer_minutes"
    const val EXTRA_TIMER_REMAINING_MS = "timer_remaining_ms"

    val all: List<SessionCommand> = listOf(SET_NOISE, SET_VOLUME, SET_TIMER, EXTEND_TIMER)
        .map { SessionCommand(it, Bundle.EMPTY) }

    fun setNoise(type: NoiseType): Pair<SessionCommand, Bundle> =
        SessionCommand(SET_NOISE, Bundle.EMPTY) to Bundle().apply { putString(ARG_NOISE, type.key) }

    /** [volume] is the 0..100 the user sees, not a gain. See [volumeToGain]. */
    fun setVolume(volume: Int): Pair<SessionCommand, Bundle> =
        SessionCommand(SET_VOLUME, Bundle.EMPTY) to Bundle().apply { putInt(ARG_VOLUME, volume) }

    /** [minutes] of 0 means no timer. */
    fun setTimer(minutes: Int): Pair<SessionCommand, Bundle> =
        SessionCommand(SET_TIMER, Bundle.EMPTY) to Bundle().apply { putInt(ARG_MINUTES, minutes) }

    fun extendTimer(minutes: Int): Pair<SessionCommand, Bundle> =
        SessionCommand(EXTEND_TIMER, Bundle.EMPTY) to Bundle().apply { putInt(ARG_MINUTES, minutes) }
}

/**
 * The volume curve: what the user sets, 0 to 100, into the gain the player takes.
 *
 * Squared, not linear. Hearing is roughly logarithmic, so a linear slider spends
 * most of its travel on changes nobody can hear and crams the useful range into the
 * bottom fifth. Squared puts 50 at about -12 dB, which is a *sensible* middle rather
 * than an arithmetic one — and 50 is where the app starts (RF-01, RF-04).
 */
fun volumeToGain(volume: Int): Float {
    val clamped = volume.coerceIn(0, 100) / 100f
    return clamped * clamped
}

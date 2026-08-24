package com.jjrapps.sleepnoise.playback

/**
 * The sleep timer's arithmetic, kept apart from the service so it can be tested
 * without one — a countdown is exactly the kind of thing that is easy to get wrong
 * around pausing and easy to verify with a fake clock.
 *
 * Times are milliseconds on a monotonic clock (`elapsedRealtime`), never wall clock:
 * a user crossing a timezone or the network nudging the date should not add or
 * remove an hour of sleep.
 */
class SleepTimer(private val nowMillis: () -> Long) {

    private var endsAt: Long? = null
    private var frozenRemaining: Long? = null

    var totalMinutes: Int = 0
        private set

    /** Starts a countdown of [minutes]; zero cancels it. */
    fun start(minutes: Int) {
        totalMinutes = minutes.coerceAtLeast(0)
        if (totalMinutes == 0) {
            cancel()
            return
        }
        frozenRemaining = null
        endsAt = nowMillis() + totalMinutes * 60_000L
    }

    fun cancel() {
        endsAt = null
        frozenRemaining = null
        totalMinutes = 0
    }

    /** Adds [minutes] to a running timer. Does nothing when none is set. */
    fun extend(minutes: Int) {
        if (!isSet) return
        totalMinutes += minutes
        frozenRemaining?.let { frozenRemaining = it + minutes * 60_000L }
            ?: run { endsAt = (endsAt ?: nowMillis()) + minutes * 60_000L }
    }

    /**
     * Freezes the countdown. Pausing the noise has to pause the timer too: a timer
     * that kept running while paused would expire during the pause and the user
     * would come back to an app that had silently given up.
     */
    fun freeze() {
        if (isSet && frozenRemaining == null) {
            frozenRemaining = remainingMillis
        }
    }

    fun resume() {
        val frozen = frozenRemaining ?: return
        frozenRemaining = null
        endsAt = nowMillis() + frozen
    }

    val isSet: Boolean get() = endsAt != null || frozenRemaining != null

    val isFrozen: Boolean get() = frozenRemaining != null

    val remainingMillis: Long
        get() = frozenRemaining ?: endsAt?.let { (it - nowMillis()).coerceAtLeast(0L) } ?: 0L

    val hasExpired: Boolean get() = isSet && !isFrozen && remainingMillis == 0L

    /**
     * How far into the closing fade the timer is, from 0 (not yet) to 1 (silent).
     *
     * The last minute fades out instead of stopping dead, because a hard stop wakes
     * up the person who was finally falling asleep — which is the opposite of the
     * app's job.
     */
    fun fadeProgress(fadeMillis: Long = FADE_OUT_MILLIS): Float {
        if (!isSet || isFrozen) return 0f
        val remaining = remainingMillis
        if (remaining >= fadeMillis) return 0f
        return 1f - remaining.toFloat() / fadeMillis
    }

    companion object {
        /** Specification RF-07: the closing fade lasts the last minute. */
        const val FADE_OUT_MILLIS = 60_000L

        /** What the notification's button adds (RF-08). */
        const val EXTEND_MINUTES = 15
    }
}

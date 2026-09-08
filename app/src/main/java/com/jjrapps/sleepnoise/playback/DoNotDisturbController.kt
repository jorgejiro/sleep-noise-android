package com.jjrapps.sleepnoise.playback

/**
 * The system's Do Not Disturb, reduced to the two things this app needs from it.
 *
 * An interface and not a call to `NotificationManager` so the bookkeeping below can
 * be tested on the JVM: what matters is *when* the app takes the phone's attention
 * away and *whether it always gives it back*, and that is arithmetic, not Android.
 */
interface ZenGateway {
    /**
     * Whether the user has granted Do Not Disturb access in system settings. No app
     * can grant it to itself, and without it every call here does nothing.
     */
    val isAccessGranted: Boolean

    /** Silences the phone, remembering what it was set to before. */
    fun silence()

    /** Puts the filter back the way [silence] found it. */
    fun restore()
}

/**
 * Turns Do Not Disturb on while the noise plays and off when it stops (RF-22).
 *
 * The whole point of the class is the invariant in [isHeld]: **the app only ever
 * gives back what it took**. Someone who turned Do Not Disturb on themselves before
 * opening the app has to find it still on when the noise stops, and pausing must
 * never be able to switch off a silence the app did not ask for.
 */
class DoNotDisturbController(private val gateway: ZenGateway) {

    /** True while this app is the reason the phone is silent. */
    var isHeld: Boolean = false
        private set

    val isAccessGranted: Boolean get() = gateway.isAccessGranted

    /**
     * Asks for silence. Returns whether the app now holds it — `false` means the
     * access was never granted, which is not an error: the noise plays either way.
     */
    fun acquire(): Boolean {
        if (isHeld) return true
        if (!gateway.isAccessGranted) return false
        gateway.silence()
        isHeld = true
        return true
    }

    /**
     * Gives it back. Always attempts the call, even with the access apparently gone:
     * the gateway swallows a refusal, so trying costs nothing and skipping would
     * throw away the one chance to release in a race where the access came back.
     *
     * Losing the access for real while holding the silence is a dead end, and it is
     * the platform's, not ours: the app's zen rule stays active and no API left to
     * this app can switch it off. Measured on API 37 — see ADR 008.
     */
    fun release() {
        if (!isHeld) return
        isHeld = false
        gateway.restore()
    }

    /**
     * Hands back a silence left over from a session that never got to end — a
     * process killed by the system mid-night, which never ran [release].
     *
     * This is the one case where the app touches a filter it cannot prove it set,
     * and it does so knowingly: a phone left silent for days is a worse failure than
     * one Do Not Disturb switched off once. It restores the default rather than the
     * previous filter, because whatever [silence] remembered died with the process.
     */
    fun releaseStale() {
        isHeld = false
        gateway.restore()
    }
}

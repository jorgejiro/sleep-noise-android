package com.jjrapps.sleepnoise.playback

import android.app.NotificationManager
import android.os.Build
import timber.log.Timber

/**
 * The real Do Not Disturb, on the far side of [ZenGateway].
 *
 * Two things about this API are worth knowing before touching it:
 *
 * **The filter is [NotificationManager.INTERRUPTION_FILTER_PRIORITY], not
 * `INTERRUPTION_FILTER_NONE`.** Priority is what the switch in the system's own quick
 * settings does: it silences by the rules the user already chose, and those let alarms
 * through. Total silence would also swallow the alarm clock, and an app for sleeping
 * that makes people oversleep has no defence.
 *
 * **From Android 15 the call no longer changes the phone's global state.** The system
 * turns it into an implicit automatic zen rule owned by this app —
 * `implicit_com.jjrapps.sleepnoise`, verified in `dumpsys notification` — switched on
 * and off by these very calls, and combines it with everyone else's under a
 * most-restrictive-wins scheme. That is better than what it replaced: asking for
 * silence can no longer overwrite the user's own Do Not Disturb, and giving it back
 * can no longer switch off someone else's.
 *
 * Which is why **giving it back is done differently on either side of that line**, and
 * this is the part that is easy to get wrong. Remembering the previous filter and
 * putting it back is the pre-15 idiom, and it is correct there because the filter *is*
 * the phone's state. From 15 it is not: setting the remembered `PRIORITY` back does not
 * restore anybody's Do Not Disturb, it **switches this app's own rule on again** — and
 * then the app believes it holds nothing while its rule sits there active. Measured on
 * an API 37 emulator: with the user's own Do Not Disturb on, pause left
 * `implicit_com.jjrapps.sleepnoise` at `state=STATE_TRUE`. From 15 the release is
 * always `INTERRUPTION_FILTER_ALL`, which deactivates this app's rule and, by that same
 * behaviour change, cannot touch anyone else's.
 *
 * Every call is guarded. Do Not Disturb access is revocable at any moment, from a
 * screen this app does not control, and losing it mid-session must cost the noise
 * nothing.
 */
class SystemZenGateway(private val manager: NotificationManager) : ZenGateway {

    /**
     * Only meaningful below Android 15, where the interruption filter is the phone's
     * global state and putting it back is what "giving it back" means.
     */
    private var previousFilter: Int? = null

    private val filterIsGlobalState: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM

    override val isAccessGranted: Boolean
        get() = runCatching { manager.isNotificationPolicyAccessGranted }.getOrDefault(false)

    override fun silence() {
        previousFilter = if (filterIsGlobalState) {
            runCatching { manager.currentInterruptionFilter }.getOrNull()
        } else {
            null
        }
        setFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
    }

    override fun restore() {
        setFilter(previousFilter ?: NotificationManager.INTERRUPTION_FILTER_ALL)
        previousFilter = null
    }

    private fun setFilter(filter: Int) {
        runCatching { manager.setInterruptionFilter(filter) }
            .onFailure { Timber.w(it, "could not set the interruption filter to %d", filter) }
    }
}

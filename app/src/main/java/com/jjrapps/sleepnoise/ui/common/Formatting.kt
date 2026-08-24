package com.jjrapps.sleepnoise.ui.common

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.jjrapps.sleepnoise.R

/**
 * The countdown in units a person reads at night: "1 h 26 min", never "01:26:04".
 *
 * Seconds are left out on purpose. A ticking second counter invites you to watch the
 * clock, which is the opposite of what an app for falling asleep should encourage.
 */
@Composable
fun formatRemaining(remainingMillis: Long): String {
    val totalMinutes = ((remainingMillis + 59_999L) / 60_000L).toInt()   // round up
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        totalMinutes <= 0 -> stringResource(R.string.timer_finishing)
        hours == 0 -> stringResource(R.string.timer_minutes_only, minutes)
        minutes == 0 -> stringResource(R.string.timer_hours_only, hours)
        else -> stringResource(R.string.timer_hours_minutes, hours, minutes)
    }
}

/**
 * Whether the system wants animations at all.
 *
 * `ANIMATOR_DURATION_SCALE` at zero is how a user says "stop moving things", either
 * for accessibility or to save battery. Respecting it is why the breathing halo can
 * be there at all — and it is also what keeps the screenshot pipeline of H9 able to
 * compare two captures for a swapped language.
 */
@Composable
fun animationsEnabled(): Boolean {
    val resolver = LocalContext.current.contentResolver
    val scale = Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    return scale > 0f
}

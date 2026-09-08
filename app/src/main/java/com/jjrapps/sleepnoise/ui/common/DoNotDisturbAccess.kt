package com.jjrapps.sleepnoise.ui.common

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import timber.log.Timber

/**
 * Whether the user has granted Do Not Disturb access, re-read every time the screen
 * comes back.
 *
 * It has to be re-read, and there is no flow to observe: the access is granted on a
 * screen belonging to the system, and the only news of it is that the app is on
 * screen again. Nothing would repaint otherwise, and the row asking for it would sit
 * there asking after it had been given.
 */
@Composable
fun rememberDoNotDisturbAccess(): Boolean {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(hasDoNotDisturbAccess(context)) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) granted = hasDoNotDisturbAccess(context)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    return granted
}

fun hasDoNotDisturbAccess(context: Context): Boolean = runCatching {
    context.getSystemService(NotificationManager::class.java).isNotificationPolicyAccessGranted
}.getOrDefault(false)

/**
 * Opens the system's list of apps allowed to silence the phone.
 *
 * It lands on the list and not on this app's own row, because the intent that goes
 * straight there is not public API. That is why the copy asking for it names the app:
 * whoever arrives has to find it themselves.
 */
fun openDoNotDisturbAccessSettings(context: Context) {
    runCatching {
        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
    }.onFailure { Timber.w(it, "no Do Not Disturb access screen on this device") }
}

package com.jjrapps.sleepnoise.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jjrapps.sleepnoise.domain.model.NoiseType
import com.jjrapps.sleepnoise.domain.model.PlaybackPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sleep_noise")

/**
 * The preferences of the specification §8, and nothing else.
 *
 * The running timer is deliberately **not** stored: if the process dies the sleep
 * session is already lost, and resuming an orphaned countdown would be worse than
 * forgetting it.
 */
class PlaybackPreferencesDataSource(private val context: Context) {

    val preferences: Flow<PlaybackPreferences> = context.dataStore.data.map { stored ->
        PlaybackPreferences(
            lastSound = NoiseType.fromKey(stored[Keys.LAST_SOUND]),
            volume = stored[Keys.VOLUME] ?: 50,
            timerMinutes = stored[Keys.TIMER_MINUTES] ?: 60,
            autoplayOnOpen = stored[Keys.AUTOPLAY] ?: true,
            language = stored[Keys.LANGUAGE] ?: PlaybackPreferences.LANGUAGE_AUTO,
            lastSeenChangelog = stored[Keys.LAST_SEEN_CHANGELOG] ?: 0,
            notificationRationaleShown = stored[Keys.NOTIF_RATIONALE] ?: false,
            doNotDisturbWhilePlaying = stored[Keys.DND_WHILE_PLAYING] ?: true,
            doNotDisturbAsked = stored[Keys.DND_ASKED] ?: false,
            doNotDisturbHeld = stored[Keys.DND_HELD] ?: false
        )
    }

    suspend fun setLastSound(type: NoiseType) = edit { it[Keys.LAST_SOUND] = type.key }
    suspend fun setVolume(volume: Int) = edit { it[Keys.VOLUME] = volume.coerceIn(0, 100) }
    suspend fun setTimerMinutes(minutes: Int) = edit { it[Keys.TIMER_MINUTES] = minutes }
    suspend fun setAutoplayOnOpen(enabled: Boolean) = edit { it[Keys.AUTOPLAY] = enabled }
    suspend fun setLanguage(language: String) = edit { it[Keys.LANGUAGE] = language }
    suspend fun setLastSeenChangelog(code: Int) = edit { it[Keys.LAST_SEEN_CHANGELOG] = code }
    suspend fun setNotificationRationaleShown(shown: Boolean) =
        edit { it[Keys.NOTIF_RATIONALE] = shown }

    suspend fun setDoNotDisturbWhilePlaying(enabled: Boolean) =
        edit { it[Keys.DND_WHILE_PLAYING] = enabled }

    suspend fun setDoNotDisturbAsked(asked: Boolean) = edit { it[Keys.DND_ASKED] = asked }

    suspend fun setDoNotDisturbHeld(held: Boolean) = edit { it[Keys.DND_HELD] = held }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }

    private object Keys {
        val LAST_SOUND = stringPreferencesKey("last_sound")
        val VOLUME = intPreferencesKey("volume")
        val TIMER_MINUTES = intPreferencesKey("timer_minutes")
        val AUTOPLAY = booleanPreferencesKey("autoplay_on_open")
        val LANGUAGE = stringPreferencesKey("language")
        val LAST_SEEN_CHANGELOG = intPreferencesKey("last_seen_changelog")
        val NOTIF_RATIONALE = booleanPreferencesKey("notif_rationale_shown")
        val DND_WHILE_PLAYING = booleanPreferencesKey("dnd_while_playing")
        val DND_ASKED = booleanPreferencesKey("dnd_asked")
        val DND_HELD = booleanPreferencesKey("dnd_held")
    }
}

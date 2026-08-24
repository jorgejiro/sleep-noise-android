package com.jjrapps.sleepnoise.data.repository

import com.jjrapps.sleepnoise.data.datastore.PlaybackPreferencesDataSource
import com.jjrapps.sleepnoise.domain.model.NoiseType
import com.jjrapps.sleepnoise.domain.model.PlaybackPreferences
import com.jjrapps.sleepnoise.domain.repository.PlaybackPreferencesRepository
import kotlinx.coroutines.flow.Flow

class PlaybackPreferencesRepositoryImpl(
    private val source: PlaybackPreferencesDataSource
) : PlaybackPreferencesRepository {
    override val preferences: Flow<PlaybackPreferences> = source.preferences
    override suspend fun setLastSound(type: NoiseType) = source.setLastSound(type)
    override suspend fun setVolume(volume: Int) = source.setVolume(volume)
    override suspend fun setTimerMinutes(minutes: Int) = source.setTimerMinutes(minutes)
    override suspend fun setAutoplayOnOpen(enabled: Boolean) = source.setAutoplayOnOpen(enabled)
    override suspend fun setLanguage(language: String) = source.setLanguage(language)
    override suspend fun setLastSeenChangelog(versionCode: Int) =
        source.setLastSeenChangelog(versionCode)
    override suspend fun setNotificationRationaleShown(shown: Boolean) =
        source.setNotificationRationaleShown(shown)
}

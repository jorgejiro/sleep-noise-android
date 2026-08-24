package com.jjrapps.sleepnoise.domain.repository

import com.jjrapps.sleepnoise.domain.model.NoiseType
import com.jjrapps.sleepnoise.domain.model.PlaybackPreferences
import kotlinx.coroutines.flow.Flow

interface PlaybackPreferencesRepository {
    val preferences: Flow<PlaybackPreferences>
    suspend fun setLastSound(type: NoiseType)
    suspend fun setVolume(volume: Int)
    suspend fun setTimerMinutes(minutes: Int)
    suspend fun setAutoplayOnOpen(enabled: Boolean)
    suspend fun setLanguage(language: String)
    suspend fun setLastSeenChangelog(versionCode: Int)
    suspend fun setNotificationRationaleShown(shown: Boolean)
}

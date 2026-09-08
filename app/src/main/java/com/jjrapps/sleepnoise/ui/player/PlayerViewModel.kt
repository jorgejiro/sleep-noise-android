package com.jjrapps.sleepnoise.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jjrapps.sleepnoise.domain.model.NoiseType
import com.jjrapps.sleepnoise.domain.model.PlaybackPreferences
import com.jjrapps.sleepnoise.domain.repository.PlaybackPreferencesRepository
import com.jjrapps.sleepnoise.playback.PlaybackConnection
import com.jjrapps.sleepnoise.playback.PlaybackState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playback: PlaybackConnection,
    private val preferencesRepository: PlaybackPreferencesRepository
) : ViewModel() {

    val state: StateFlow<PlaybackState> = playback.state

    /**
     * Null until DataStore has actually answered, and not a default value.
     *
     * The one-time Do Not Disturb sheet is decided from these, and a default would
     * read as "never asked" for the fraction of a second before the real answer
     * arrives — which is long enough to flash the sheet at someone who dismissed it
     * months ago.
     */
    val preferences: StateFlow<PlaybackPreferences?> = preferencesRepository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun connect() = playback.connect()
    fun togglePlay() = playback.togglePlay()
    fun selectNoise(type: NoiseType) = playback.setNoise(type)
    fun setVolume(volume: Int) = playback.setVolume(volume)
    fun setTimer(minutes: Int) = playback.setTimer(minutes)

    /** The sheet has had its one showing, whatever the user did with it. */
    fun markDoNotDisturbAsked() = viewModelScope.launch {
        preferencesRepository.setDoNotDisturbAsked(true)
    }
}

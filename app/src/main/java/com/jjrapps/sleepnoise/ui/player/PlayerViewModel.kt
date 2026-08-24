package com.jjrapps.sleepnoise.ui.player

import androidx.lifecycle.ViewModel
import com.jjrapps.sleepnoise.domain.model.NoiseType
import com.jjrapps.sleepnoise.playback.PlaybackConnection
import com.jjrapps.sleepnoise.playback.PlaybackState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playback: PlaybackConnection
) : ViewModel() {

    val state: StateFlow<PlaybackState> = playback.state

    fun connect() = playback.connect()
    fun togglePlay() = playback.togglePlay()
    fun selectNoise(type: NoiseType) = playback.setNoise(type)
    fun setVolume(volume: Int) = playback.setVolume(volume)
    fun setTimer(minutes: Int) = playback.setTimer(minutes)
}

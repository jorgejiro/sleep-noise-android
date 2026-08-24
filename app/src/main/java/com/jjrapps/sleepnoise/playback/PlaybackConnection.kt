package com.jjrapps.sleepnoise.playback

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.jjrapps.sleepnoise.domain.model.NoiseType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

/**
 * The one bridge between the UI and the playback service.
 *
 * A single long-lived controller for the whole app rather than one per screen: the
 * connection is asynchronous, and reconnecting on every navigation would mean a
 * screen that flickers into place while it waits.
 */
@OptIn(UnstableApi::class)
class PlaybackConnection(private val context: Context) {

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private var controller: MediaController? = null
    private var playWhenConnected = false

    fun connect() {
        if (controller != null) return
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        MediaController.Builder(context, token)
            // Two listeners, because they carry different things and neither implies
            // the other. Player.Listener reports play, pause and errors; the session
            // extras — which noise, what volume, how long is left — arrive only
            // through MediaController.Listener.onExtrasChanged. Wiring just the first
            // one left the timer row frozen at "no timer" while the countdown was
            // running underneath.
            .setListener(controllerListener)
            .buildAsync().apply {
            addListener({
                controller = runCatching { get() }
                    .onFailure { Timber.w(it, "could not reach the playback service") }
                    .getOrNull()
                controller?.let { ready ->
                    ready.addListener(listener)
                    readFrom(ready)
                    if (playWhenConnected) {
                        playWhenConnected = false
                        // The service starts playing by itself when "play on open" is
                        // on; this covers the case where it is off.
                        if (!ready.isPlaying) ready.play()
                    }
                }
            }, MoreExecutors.directExecutor())
        }
    }

    fun release() {
        controller?.removeListener(listener)
        controller?.release()
        controller = null
        _state.value = PlaybackState()
    }

    // ------------------------------------------------------------------ actions

    /**
     * Play or pause.
     *
     * Pausing ends the session, so playing again may need the service to put its
     * source back — but that is the service's job, not this one's. An earlier version
     * tried to handle it here by reconnecting, and it did not work: after a pause the
     * screen is still bound, so the service is still alive and reconnecting hands back
     * the same session with the same empty queue. Play did nothing at all.
     *
     * The only case this has to handle is the service being gone, which happens when
     * the app was closed after ending a session.
     */
    fun togglePlay() {
        val player = controller
        if (player == null) {
            playWhenConnected = true
            connect()
            return
        }
        if (player.isPlaying) player.pause() else player.play()
    }

    fun play() {
        controller?.takeIf { !it.isPlaying }?.play()
    }

    fun setNoise(type: NoiseType) = send(PlaybackCommands.setNoise(type))

    fun setVolume(volume: Int) {
        // Reflected locally straight away: a slider that waits for a round trip
        // through the service before it moves feels broken.
        _state.value = _state.value.copy(volume = volume.coerceIn(0, 100))
        send(PlaybackCommands.setVolume(volume))
    }

    fun setTimer(minutes: Int) = send(PlaybackCommands.setTimer(minutes))

    fun extendTimer(minutes: Int = SleepTimer.EXTEND_MINUTES) =
        send(PlaybackCommands.extendTimer(minutes))

    /** Que el servicio vuelva a publicar sus textos, tras un cambio de idioma. */
    fun refreshLabels() = send(PlaybackCommands.refreshLabels())

    private fun send(command: Pair<androidx.media3.session.SessionCommand, Bundle>) {
        controller?.sendCustomCommand(command.first, command.second)
    }

    // -------------------------------------------------------------------- state

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            controller?.let { readFrom(it) }
        }
    }

    private val controllerListener = object : MediaController.Listener {
        override fun onExtrasChanged(controller: MediaController, extras: Bundle) {
            readFrom(controller)
        }

        override fun onDisconnected(controller: MediaController) {
            _state.value = PlaybackState()
        }
    }

    private fun readFrom(player: MediaController) {
        val extras = player.sessionExtras
        _state.value = PlaybackState(
            connected = true,
            isPlaying = player.isPlaying,
            noise = NoiseType.fromKey(extras.getString(PlaybackCommands.EXTRA_NOISE)),
            volume = extras.getInt(PlaybackCommands.EXTRA_VOLUME, PlaybackService.DEFAULT_VOLUME),
            timerMinutes = extras.getInt(PlaybackCommands.EXTRA_TIMER_MINUTES, 0),
            timerRemainingMillis = extras.getLong(PlaybackCommands.EXTRA_TIMER_REMAINING_MS, 0L)
        )
    }
}

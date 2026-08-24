package com.jjrapps.sleepnoise.playback

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.jjrapps.sleepnoise.R
import com.jjrapps.sleepnoise.audio.SAMPLE_RATE
import com.jjrapps.sleepnoise.audio.StereoNoiseSource
import com.jjrapps.sleepnoise.domain.model.NoiseType
import com.jjrapps.sleepnoise.domain.repository.PlaybackPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Where the noise actually plays.
 *
 * A [MediaSessionService] rather than a service of our own: it builds and maintains
 * the MediaStyle notification, answers the headset buttons and the system's media
 * controls, and handles going in and out of the foreground. Writing that by hand
 * would be a week of work to arrive at something worse.
 */
@OptIn(UnstableApi::class)
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject
    lateinit var preferences: PlaybackPreferencesRepository

    private var session: MediaSession? = null
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var player: FadingPlayer

    /**
     * The generator outlives every media item, which is what makes changing sound and
     * repeating the source inaudible. See [com.jjrapps.sleepnoise.audio.NoiseDataSource].
     */
    private lateinit var noise: StereoNoiseSource

    private val timer = SleepTimer { SystemClock.elapsedRealtime() }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var timerJob: Job? = null

    private var volume: Int = DEFAULT_VOLUME
    private var currentType: NoiseType = NoiseType.Default

    override fun onCreate() {
        super.onCreate()
        noise = StereoNoiseSource(currentType, sampleRate = SAMPLE_RATE)
        exoPlayer = NoisePlayer.create(this).apply {
            setMediaSource(NoisePlayer.sourceFor(currentType, noise, this@PlaybackService))
            prepare()
        }
        // Every gain change goes through this wrapper: the fades around play and
        // pause, the user's volume and the timer's closing fade would otherwise
        // overwrite each other.
        player = FadingPlayer(exoPlayer, scope)
        player.setUserVolume(volume)
        player.addListener(playerListener)

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this)
                .setChannelId(CHANNEL_ID)
                .setChannelName(R.string.notification_channel_name)
                .build()
        )

        session = MediaSession.Builder(this, player)
            .setCallback(SessionCallback())
            .build()
        publishState()
        restorePreferences()
    }

    /**
     * Reads what was playing last time and picks up where it left off (RF-01).
     *
     * Read here rather than in the UI on purpose: the screen is not the only way the
     * service starts — a headset button or the system's media controls can do it too
     * — and the sound that comes back has to be the right one every way in.
     */
    private fun restorePreferences() {
        scope.launch {
            val stored = preferences.preferences.first()
            volume = stored.volume
            player.setUserVolume(volume)
            if (stored.lastSound != currentType) {
                currentType = stored.lastSound
                noise.crossfadeTo(stored.lastSound)
                exoPlayer.replaceMediaItem(0, NoisePlayer.mediaItemFor(stored.lastSound, this@PlaybackService))
            }
            publishState()
            if (stored.autoplayOnOpen && !player.isPlaying) {
                player.play()
            }
        }
    }

    /**
     * Sliding the app out of recents does **not** stop the noise.
     *
     * This goes against Media3's own recommendation, which is written for music: for
     * music, dismissing the app means "I'm done". Here it means "I'm going to sleep",
     * and stopping would be the opposite of what was asked. It stops from the
     * notification, or when the timer runs out.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.isPlaying) {
            // Nothing is playing, so there is nothing to keep alive and no reason to
            // hold a foreground service and a notification the user cannot use.
            stopSelf()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onDestroy() {
        timerJob?.cancel()
        player.releaseFades()
        scope.cancel()
        session?.run {
            player.removeListener(playerListener)
            player.release()
            release()
        }
        session = null
        super.onDestroy()
    }

    // ---------------------------------------------------------------- playback

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                timer.resume()
                startTicking()
            } else {
                timer.freeze()
                timerJob?.cancel()
            }
            publishState()
        }
    }

    private fun changeNoise(type: NoiseType) {
        if (type == currentType) return
        currentType = type
        scope.launch { preferences.setLastSound(type) }
        // The generator crossfades internally, so the player is never told anything
        // changed — no new source, no discarded buffer, no gap. Only the title of the
        // notification is replaced.
        noise.crossfadeTo(type)
        player.replaceMediaItem(0, NoisePlayer.mediaItemFor(type, this))
        publishState()
    }

    private fun changeVolume(newVolume: Int) {
        volume = newVolume.coerceIn(0, 100)
        // Safe to write at any time: FadingPlayer keeps the user's volume, the
        // transition fade and the timer fade as separate factors.
        player.setUserVolume(volume)
        scope.launch { preferences.setVolume(volume) }
        publishState()
    }

    // ------------------------------------------------------------------- timer

    private fun startTimer(minutes: Int) {
        timer.start(minutes)
        player.setTimerFade(0f)
        scope.launch { preferences.setTimerMinutes(minutes) }
        if (timer.isSet) startTicking() else timerJob?.cancel()
        publishState()
    }

    private fun startTicking() {
        timerJob?.cancel()
        if (!timer.isSet) return
        timerJob = scope.launch {
            while (timer.isSet && !timer.isFrozen) {
                player.setTimerFade(timer.fadeProgress())
                if (timer.hasExpired) {
                    Timber.d("sleep timer expired, pausing")
                    // Already silent from the closing fade, so no second fade: it
                    // would only add 400 ms of nothing.
                    player.pauseImmediately()
                    player.setTimerFade(0f)
                    timer.cancel()
                    publishState()
                    return@launch
                }
                publishState()
                delay(TICK_MILLIS)
            }
        }
    }

    // ------------------------------------------------------------------ state

    /**
     * Publishes what the session cannot express by itself — which noise, what volume,
     * how long is left — as session extras, which is where a Media3 controller looks.
     */
    private fun publishState() {
        val extras = Bundle().apply {
            putString(PlaybackCommands.EXTRA_NOISE, currentType.key)
            putInt(PlaybackCommands.EXTRA_VOLUME, volume)
            putInt(PlaybackCommands.EXTRA_TIMER_MINUTES, timer.totalMinutes)
            putLong(PlaybackCommands.EXTRA_TIMER_REMAINING_MS, timer.remainingMillis)
        }
        session?.setSessionExtras(extras)
        session?.setCustomLayout(customLayout())
    }

    /** The extra button in the notification, only while there is a timer to extend. */
    private fun customLayout(): ImmutableList<CommandButton> {
        if (!timer.isSet) return ImmutableList.of()
        return ImmutableList.of(
            CommandButton.Builder(CommandButton.ICON_PLUS)
                .setSessionCommand(PlaybackCommands.extendTimer(SleepTimer.EXTEND_MINUTES).first)
                .setDisplayName(
                    getString(R.string.timer_extend, SleepTimer.EXTEND_MINUTES)
                )
                .build()
        )
    }

    private inner class SessionCallback : MediaSession.Callback {

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            // The app's own controller needs the custom commands; Media3 1.11 no
            // longer hands session data to untrusted controllers by default, which is
            // the behaviour we want for everyone else.
            val available = MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS
                .buildUpon()
            PlaybackCommands.all.forEach { available.add(it) }
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(available.build())
                .setCustomLayout(customLayout())
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                PlaybackCommands.SET_NOISE ->
                    changeNoise(NoiseType.fromKey(args.getString(PlaybackCommands.ARG_NOISE)))
                PlaybackCommands.SET_VOLUME ->
                    changeVolume(args.getInt(PlaybackCommands.ARG_VOLUME, volume))
                PlaybackCommands.SET_TIMER ->
                    startTimer(args.getInt(PlaybackCommands.ARG_MINUTES, 0))
                PlaybackCommands.EXTEND_TIMER -> {
                    timer.extend(args.getInt(PlaybackCommands.ARG_MINUTES, SleepTimer.EXTEND_MINUTES))
                    // Back up from wherever the closing fade had got to (RF-08).
                    player.setTimerFade(0f)
                    if (!player.isPlaying) player.play()
                    startTicking()
                    publishState()
                }
                else -> return Futures.immediateFuture(
                    SessionResult(SessionError.ERROR_NOT_SUPPORTED)
                )
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    companion object {
        const val CHANNEL_ID = "playback"

        /** Specification RF-01: mid volume on a fresh install. */
        const val DEFAULT_VOLUME = 50

        /** Once a second is enough for a countdown shown in minutes. */
        const val TICK_MILLIS = 1_000L
    }
}

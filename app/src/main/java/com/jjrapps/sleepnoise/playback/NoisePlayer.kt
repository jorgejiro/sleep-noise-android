package com.jjrapps.sleepnoise.playback

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.jjrapps.sleepnoise.R
import com.jjrapps.sleepnoise.audio.NoiseDataSource
import com.jjrapps.sleepnoise.audio.StereoNoiseSource
import com.jjrapps.sleepnoise.domain.model.NoiseType

/**
 * Builds the ExoPlayer the app plays noise with, and every setting here is a
 * decision from the specification §7 rather than a default worth keeping.
 */
@UnstableApi
object NoisePlayer {

    fun create(context: Context): ExoPlayer =
        ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    // USAGE_MEDIA so the noise rides the media volume the user
                    // already knows, and CONTENT_TYPE_MUSIC so the system ducks it
                    // for a notification instead of doing something stranger.
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true
            )
            // Unplugging headphones pauses. Counter-intuitive for a sleep app right
            // up to the moment you picture white noise blasting out of the speaker
            // at four in the morning.
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
            .apply {
                // The item is finite because a WAV header cannot say otherwise, so
                // it repeats. The generator keeps its state across the repeat, which
                // is what makes the seam inaudible. See WavHeader.MAX_DATA_BYTES.
                repeatMode = Player.REPEAT_MODE_ONE
            }

    /**
     * The media source for one noise. The [generator] is owned by the caller and
     * outlives every repeat.
     */
    fun sourceFor(
        type: NoiseType,
        generator: StereoNoiseSource,
        context: Context
    ): ProgressiveMediaSource = ProgressiveMediaSource
        .Factory(NoiseDataSource.Factory(generator))
        .createMediaSource(mediaItemFor(type, context))

    /**
     * A context carrying the language the user chose in Settings.
     *
     * The service is not an Activity, so it does not get recreated when
     * `AppCompatDelegate.setApplicationLocales` is called: its resources keep
     * answering in the previous language, and the notification — which is generated
     * here — stays in it too. The screenshot pipeline caught this: the Spanish
     * capture of the notification said "Masking noise".
     *
     * Below API 33 AppCompat only emulates the per-app language, so this is the only
     * way the service learns about it at all.
     */
    fun localized(context: Context): Context {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) return context
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocales(LocaleList.forLanguageTags(locales.toLanguageTags()))
        return context.createConfigurationContext(configuration)
    }

    /** Title for the notification and every system media surface. */
    fun titleFor(type: NoiseType, context: Context): String = localized(context).getString(
        when (type) {
            NoiseType.White -> R.string.sound_white_name
            NoiseType.Pink -> R.string.sound_pink_name
            NoiseType.Brown -> R.string.sound_brown_name
            NoiseType.Masking -> R.string.sound_masking_name
        }
    )

    /**
     * Metadata matters more than it looks: this title is what the notification and
     * every system media surface show, so it goes through string resources like any
     * other visible text.
     */
    fun mediaItemFor(type: NoiseType, context: Context): MediaItem {
        return MediaItem.Builder()
            .setUri(NoiseDataSource.URI)
            .setMediaId(type.key)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(titleFor(type, context))
                    .setArtist(localized(context).getString(R.string.app_name))
                    .setIsBrowsable(false)
                    .setIsPlayable(true)
                    .build()
            )
            .build()
    }
}

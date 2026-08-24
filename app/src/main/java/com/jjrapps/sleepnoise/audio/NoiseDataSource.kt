package com.jjrapps.sleepnoise.audio

import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import kotlin.math.min

/**
 * Serves the synthesiser to Media3 as if it were a WAV file being read.
 *
 * The URI carries only which noise to make (`sleepnoise://white`), and the source
 * answers with a 44-byte header followed by PCM for as long as anyone keeps
 * reading. Nothing is ever stored.
 *
 * **The generator outlives the source.** It is supplied from outside rather than
 * created here, and that is deliberate: when the player repeats the item — which it
 * must, because a WAV cannot declare more than about 6.2 hours — this source is
 * closed and a new one opened at position zero. If the generator were owned here,
 * that repeat would restart the noise from its first sample and the listener would
 * hear the same stretch again every six hours. Held outside, the stream simply
 * carries on.
 */
@UnstableApi
class NoiseDataSource(
    private val generator: StereoNoiseSource
) : BaseDataSource(/* isNetwork = */ false) {

    private var uri: Uri? = null
    private var headerRemaining = 0
    private var header = ByteArray(0)
    private var opened = false

    private var frames = FloatArray(0)
    private var frameBytes = ByteArray(0)

    override fun open(dataSpec: DataSpec): Long {
        transferInitializing(dataSpec)
        uri = dataSpec.uri
        header = WavHeader.create()
        headerRemaining = header.size
        // The generator is NOT reset here: see the class comment. A repeat has to be
        // inaudible, and resetting is exactly what would make it audible.
        opened = true
        transferStarted(dataSpec)
        // Unset rather than a number: the stream has no end, and claiming one would
        // only invite the player to try to seek to it.
        return C.LENGTH_UNSET.toLong()
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) return 0
        check(opened) { "read before open" }

        // The header goes out first, in as many pieces as the caller's buffer needs.
        if (headerRemaining > 0) {
            val chunk = min(headerRemaining, length)
            header.copyInto(buffer, offset, header.size - headerRemaining, header.size - headerRemaining + chunk)
            headerRemaining -= chunk
            bytesTransferred(chunk)
            return chunk
        }

        // Whole frames only. Handing back half a frame would shift every following
        // sample by one channel and swap left for right for the rest of the night.
        val frameCount = length / BYTES_PER_FRAME
        if (frameCount == 0) return 0

        if (frames.size < frameCount * CHANNELS) frames = FloatArray(frameCount * CHANNELS)
        if (frameBytes.size < frameCount * BYTES_PER_FRAME) {
            frameBytes = ByteArray(frameCount * BYTES_PER_FRAME)
        }

        generator.generate(frames, frameCount)
        val written = PcmEncoder.encode(frames, frameCount * CHANNELS, frameBytes, 0)
        frameBytes.copyInto(buffer, offset, 0, written)
        bytesTransferred(written)
        return written
    }

    override fun getUri(): Uri? = uri

    override fun close() {
        if (opened) {
            opened = false
            transferEnded()
        }
        uri = null
    }

    companion object {
        const val CHANNELS = 2
        const val BYTES_PER_FRAME = CHANNELS * PcmEncoder.BYTES_PER_SAMPLE

        /**
         * One URI for the whole app, with no sound in it.
         *
         * Which noise is playing is the generator's business, not the player's. If
         * the URI named the sound, changing sound would mean a new media item, and
         * that would restart the source and throw away whatever was buffered — an
         * audible jump. With a single item the player never notices a change at all;
         * only the notification's title is replaced.
         */
        val URI: Uri = "sleepnoise://noise".toUri()
    }

    /**
     * Hands Media3 a source per playback. The generator is created once per factory
     * and shared, so the stream survives the repeats.
     */
    @UnstableApi
    class Factory(private val generator: StereoNoiseSource) : DataSource.Factory {
        override fun createDataSource(): DataSource = NoiseDataSource(generator)
    }
}

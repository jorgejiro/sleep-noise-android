package com.jjrapps.sleepnoise.audio

import com.jjrapps.sleepnoise.domain.model.NoiseType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The whole audio stream of the app: two independent generators per sound, one per
 * channel, plus the crossfade that moves between sounds.
 *
 * **Independent channels.** Copying one channel to both is cheaper and sounds
 * worse: identical channels collapse to a point in the middle of the head, which
 * is tiring over a night of headphone listening. Decorrelated channels feel wide
 * and outside the head.
 *
 * **The crossfade lives here, not in the player.** Changing sound could have been
 * done by handing ExoPlayer a new media source, but then the old sound has to be
 * faded out, the source swapped and the new one faded in — and however short, there
 * is a moment of silence in the middle, plus a buffer of the old sound to throw
 * away. Since the generator is ours, both sounds can simply be produced at once and
 * mixed: no silence, no discarded buffers, and the player never learns that
 * anything changed.
 */
class StereoNoiseSource(
    initialType: NoiseType,
    private val seed: Long = WhiteNoiseGenerator.DEFAULT_SEED,
    private val sampleRate: Int = SAMPLE_RATE,
    private val crossfadeMillis: Int = DEFAULT_CROSSFADE_MILLIS
) {
    private var current = channelPair(initialType)
    private var incoming: ChannelPair? = null

    /** Frames remaining in the current crossfade; zero when not fading. */
    private var fadeRemaining = 0
    private var fadeLength = 0

    var type: NoiseType = initialType
        private set

    /** The sound being faded in, if any. Otherwise null. */
    var pendingType: NoiseType? = null
        private set

    private var mono = FloatArray(0)
    private var monoIn = FloatArray(0)

    /**
     * Starts a crossfade towards [target]. Calling it with the sound already playing
     * does nothing; calling it mid-fade retargets from wherever the mix currently is,
     * so tapping back and forth never produces a jump.
     */
    fun crossfadeTo(target: NoiseType) {
        if (target == type && pendingType == null) return
        if (target == pendingType) return
        if (target == type && pendingType != null) {
            // Turning back to what is already fading out: swap the roles rather than
            // starting a fade towards a sound that is on its way out.
            val stillFading = incoming
            incoming = current
            current = stillFading!!
            type = pendingType!!
            pendingType = target
            fadeRemaining = fadeLength - fadeRemaining
            return
        }
        incoming = channelPair(target)
        pendingType = target
        fadeLength = (crossfadeMillis * sampleRate / 1000).coerceAtLeast(1)
        fadeRemaining = fadeLength
    }

    /** Fills [out] with [frames] interleaved stereo frames, so `2 * frames` samples. */
    fun generate(out: FloatArray, frames: Int) {
        require(out.size >= frames * 2) {
            "buffer holds ${out.size} samples, need ${frames * 2} for $frames frames"
        }
        if (mono.size < frames) mono = FloatArray(frames)
        if (monoIn.size < frames) monoIn = FloatArray(frames)

        for (channel in 0..1) {
            current.generatorFor(channel).generate(mono, frames)
            val fading = incoming
            if (fading == null) {
                for (i in 0 until frames) out[i * 2 + channel] = mono[i]
                continue
            }
            fading.generatorFor(channel).generate(monoIn, frames)
            // Equal-power weights, not linear ones. The two sounds are uncorrelated,
            // so their powers add: linear weights would dip 3 dB in the middle of
            // every change, which is heard as a dent.
            var remaining = fadeRemaining
            for (i in 0 until frames) {
                val progress = if (fadeLength == 0) 1f else 1f - remaining.toFloat() / fadeLength
                val angle = progress * PI.toFloat() / 2f
                out[i * 2 + channel] = mono[i] * cos(angle) + monoIn[i] * sin(angle)
                if (remaining > 0) remaining--
            }
            // Only the last channel commits the advance, or the second channel would
            // fade at twice the speed of the first.
            if (channel == 1) fadeRemaining = remaining
        }

        if (incoming != null && fadeRemaining == 0) {
            current = incoming!!
            incoming = null
            type = pendingType!!
            pendingType = null
        }
    }

    fun reset() {
        current = channelPair(type)
        incoming = null
        pendingType = null
        fadeRemaining = 0
    }

    private fun channelPair(type: NoiseType) = ChannelPair(
        left = createGenerator(type, seed, sampleRate),
        right = createGenerator(type, seed + CHANNEL_SEED_STRIDE, sampleRate)
    )

    private class ChannelPair(val left: NoiseGenerator, val right: NoiseGenerator) {
        fun generatorFor(channel: Int) = if (channel == 0) left else right
    }

    companion object {
        /**
         * A large odd offset, the golden-ratio constant splitmix64 uses. Any big odd
         * number would do; the point is that the two channels do not sit next door in
         * the sequence.
         */
        const val CHANNEL_SEED_STRIDE = -0x61c8864680b583ebL

        /** Specification §3.4. Long enough to be unnoticed, short enough to feel immediate. */
        const val DEFAULT_CROSSFADE_MILLIS = 800
    }
}

/** The one place that maps a [NoiseType] to the arithmetic that produces it. */
fun createGenerator(
    type: NoiseType,
    seed: Long = WhiteNoiseGenerator.DEFAULT_SEED,
    sampleRate: Int = SAMPLE_RATE
): NoiseGenerator = when (type) {
    NoiseType.White -> WhiteNoiseGenerator(seed)
    NoiseType.Pink -> PinkNoiseGenerator(seed, sampleRate)
    NoiseType.Brown -> BrownNoiseGenerator(seed, sampleRate)
    NoiseType.Masking -> MaskingNoiseGenerator(seed, sampleRate)
}

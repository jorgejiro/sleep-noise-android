package com.jjrapps.sleepnoise.audio

import com.jjrapps.sleepnoise.domain.model.NoiseType

/**
 * Two independent generators, one per channel, interleaved into the buffer the
 * audio sink wants.
 *
 * Independent is the whole point. Copying one channel to both is cheaper and
 * sounds worse: identical channels collapse to a point in the middle of the head,
 * which is fatiguing over hours of headphone listening. Decorrelated channels feel
 * wide and outside the head, which is what you want to fall asleep to.
 *
 * The two seeds are deliberately far apart in the sequence rather than adjacent —
 * `seed` and `seed + 1` on xoshiro give streams that are independent in theory but
 * needlessly close in practice.
 */
class StereoNoiseSource(
    type: NoiseType,
    seed: Long = WhiteNoiseGenerator.DEFAULT_SEED,
    sampleRate: Int = SAMPLE_RATE
) {
    private val left = createGenerator(type, seed, sampleRate)
    private val right = createGenerator(type, seed + CHANNEL_SEED_STRIDE, sampleRate)

    private var scratch = FloatArray(0)

    /**
     * Fills [out] with [frames] interleaved stereo frames, so `2 * frames` samples.
     */
    fun generate(out: FloatArray, frames: Int) {
        require(out.size >= frames * 2) {
            "buffer holds ${out.size} samples, need ${frames * 2} for $frames frames"
        }
        if (scratch.size < frames) scratch = FloatArray(frames)

        left.generate(scratch, frames)
        for (i in 0 until frames) out[i * 2] = scratch[i]

        right.generate(scratch, frames)
        for (i in 0 until frames) out[i * 2 + 1] = scratch[i]
    }

    fun reset() {
        left.reset()
        right.reset()
    }

    private companion object {
        /**
         * A large odd offset, from the golden-ratio constant used by splitmix64.
         * Any big odd number would do; the point is not to sit next door.
         */
        const val CHANNEL_SEED_STRIDE = -0x61c8864680b583ebL
    }
}

/** The one place that maps a [NoiseType] to the arithmetic that produces it. */
fun createGenerator(
    type: NoiseType,
    seed: Long = WhiteNoiseGenerator.DEFAULT_SEED,
    sampleRate: Int = SAMPLE_RATE
): NoiseGenerator = when (type) {
    NoiseType.White -> WhiteNoiseGenerator(seed)
    NoiseType.Brown -> BrownNoiseGenerator(seed, sampleRate)
}

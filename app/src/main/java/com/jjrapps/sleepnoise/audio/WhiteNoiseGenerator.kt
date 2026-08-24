package com.jjrapps.sleepnoise.audio

import kotlin.math.ln
import kotlin.math.sqrt

/**
 * White noise: Gaussian samples, flat power across the spectrum.
 *
 * Gaussian rather than uniform, which is the shortcut most implementations take.
 * Uniform noise is also spectrally flat, so it looks identical on an analyser, but
 * its amplitude distribution is wrong and it sounds subtly harsher — the ear
 * notices the missing tail. Real acoustic noise is Gaussian, so this is Gaussian.
 *
 * The polar (Marsaglia) form of Box-Muller: two normal deviates per pass, no
 * trigonometry, only a square root and a logarithm. The second value is kept for
 * the next call rather than thrown away, which halves the work.
 */
class WhiteNoiseGenerator(
    private val seed: Long = DEFAULT_SEED,
    private val targetRms: Float = TARGET_RMS
) : NoiseGenerator {

    private var random = Xoshiro256PlusPlus(seed)
    private var spare = 0.0
    private var hasSpare = false

    override fun generate(out: FloatArray, count: Int) {
        for (i in 0 until count) {
            // A standard normal has unit variance, so scaling by the target RMS
            // *is* the level: no measure-and-correct pass needed.
            out[i] = clampToUnit((nextGaussian() * targetRms).toFloat())
        }
    }

    override fun reset() {
        random = Xoshiro256PlusPlus(seed)
        hasSpare = false
    }

    private fun nextGaussian(): Double {
        if (hasSpare) {
            hasSpare = false
            return spare
        }
        var u: Double
        var v: Double
        var s: Double
        do {
            u = random.nextDouble() * 2.0 - 1.0
            v = random.nextDouble() * 2.0 - 1.0
            s = u * u + v * v
            // Outside the unit circle the transform is not valid, and exactly zero
            // would divide by zero. Both are rare, so the loop almost never spins.
        } while (s >= 1.0 || s == 0.0)

        val factor = sqrt(-2.0 * ln(s) / s)
        spare = v * factor
        hasSpare = true
        return u * factor
    }

    companion object {
        /**
         * A fixed default seed. The stream is unpredictable enough for noise, and a
         * fixed seed makes every test and every measurement reproducible; seeding
         * from the clock would only make a failure harder to chase.
         */
        const val DEFAULT_SEED: Long = 0x5EED_51EE_0000_0001L
    }
}

/**
 * Guards the format boundary, not the ear: a Gaussian sample has no upper bound,
 * so at -18 dBFS RMS reaching full scale takes about eight standard deviations.
 * That happens roughly once in 10^15 samples — once in a few thousand years at
 * 48 kHz — but PCM has no room for it, and an overflow there would wrap around
 * into an audible click rather than just clip.
 */
internal fun clampToUnit(value: Float): Float = when {
    value > 1f -> 1f
    value < -1f -> -1f
    else -> value
}

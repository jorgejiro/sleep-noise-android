package com.jjrapps.sleepnoise.audio

import kotlin.math.sqrt

/**
 * Shared machinery for every noise that is white noise put through a filter — which
 * is all of them except white itself.
 *
 * Two things every one of them needs, and both were learned the hard way on the
 * brown generator:
 *
 * **The gain has to be measured, not derived.** The attenuation of a single filter
 * can be computed on paper, but a chain of them cannot: the DC blocker after brown's
 * integrator removes energy exactly where brown keeps all of it, and the first
 * version of that class came out 4 dB quiet because the arithmetic did not know
 * about it. Measuring once at construction costs under two milliseconds and stays
 * right when the filters change.
 *
 * **The filters have to be warmed up.** They all start at zero, so the first
 * fraction of a second is a transient — the level ramps up from nothing and any DC
 * blocker has not settled. On its own that is inaudible, but it lands in the middle
 * of a crossfade when the user changes sound, and there it is a dent.
 */
abstract class FilteredNoiseGenerator(
    seed: Long,
    protected val sampleRate: Int,
    private val targetRms: Float
) : NoiseGenerator {

    protected val white = WhiteNoiseGenerator(seed, targetRms = 1f)
    private val scratch = FloatArray(SCRATCH_SIZE)

    /** Applies the filter to one sample. Implementations keep their own state. */
    protected abstract fun filter(sample: Float): Float

    /** Returns the filter to its initial state. */
    protected abstract fun resetFilter()

    private var gain: Float = 1f

    /**
     * Called by the subclass once its own fields are initialised.
     *
     * Not done in this class's `init`: Kotlin runs the base constructor before the
     * subclass's fields exist, so filtering from here would read coefficients that
     * are still zero — a silent, and very confusing, wrong answer.
     */
    protected fun calibrate() {
        val probe = FloatArray(CALIBRATION_SAMPLES)
        render(probe, 1f, CALIBRATION_SAMPLES)
        val settled = CALIBRATION_SAMPLES / 4       // skip the opening ramp
        var sum = 0.0
        for (i in settled until CALIBRATION_SAMPLES) sum += probe[i].toDouble() * probe[i]
        val measured = sqrt(sum / (CALIBRATION_SAMPLES - settled))
        gain = if (measured <= 0.0) 1f else (targetRms / measured).toFloat()
        white.reset()
        resetFilter()
        warmUp()
    }

    override fun generate(out: FloatArray, count: Int) {
        render(out, gain, count)
        // Limiting every generator, not just the loud ones: at -12 dBFS a Gaussian
        // peak reaches full scale about once a minute, and PCM has nowhere to put it.
        for (i in 0 until count) out[i] = softLimit(out[i])
    }

    override fun reset() {
        white.reset()
        resetFilter()
        warmUp()
    }

    private fun render(out: FloatArray, gain: Float, count: Int) {
        var written = 0
        while (written < count) {
            val block = minOf(SCRATCH_SIZE, count - written)
            white.generate(scratch, block)
            for (i in 0 until block) out[written + i] = filter(scratch[i]) * gain
            written += block
        }
    }

    private fun warmUp() {
        val discard = FloatArray(SCRATCH_SIZE)
        var done = 0
        while (done < WARMUP_SAMPLES) {
            val block = minOf(SCRATCH_SIZE, WARMUP_SAMPLES - done)
            render(discard, gain, block)
            done += block
        }
    }

    protected companion object {
        const val SCRATCH_SIZE = 1024

        /** Two seconds at 48 kHz: enough for a stable RMS, cheap enough not to notice. */
        const val CALIBRATION_SAMPLES = 96_000

        /** Half a second: well past where any of these filters is still moving. */
        const val WARMUP_SAMPLES = 24_000

        const val TWO_PI = 2.0 * Math.PI
    }
}

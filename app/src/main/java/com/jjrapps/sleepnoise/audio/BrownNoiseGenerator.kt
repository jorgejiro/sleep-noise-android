package com.jjrapps.sleepnoise.audio

import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * Brown noise: white noise through a leaky integrator, which is what gives it the
 * -6 dB per octave slope and the deep, rolling character — closer to distant rain
 * than to the hiss of white noise.
 *
 * Three things this does that a naive integrator does not, and all three are
 * audible if you skip them:
 *
 * 1. **The integrator leaks.** A pure integrator is a random walk: its variance
 *    grows without bound, so it wanders off towards one rail and stays there. The
 *    leak makes the variance finite at the cost of flattening the response below
 *    [CORNER_HZ], which is under the hearing threshold anyway.
 * 2. **DC is blocked.** Even leaky, the output drifts slowly around zero. That
 *    drift is not heard as sound but as pressure, and it eats headroom from the
 *    part of the signal that is heard.
 * 3. **Peaks are limited softly.** The energy sits in the low frequencies, so the
 *    crest factor is far higher than white noise's: excursions that would clip are
 *    routine. Hard clipping them would add harmonics — a buzz — so the limiter is
 *    a smooth curve instead.
 */
class BrownNoiseGenerator(
    seed: Long = WhiteNoiseGenerator.DEFAULT_SEED,
    sampleRate: Int = SAMPLE_RATE,
    private val targetRms: Float = TARGET_RMS
) : NoiseGenerator {

    private val white = WhiteNoiseGenerator(seed, targetRms = 1f)

    /** One-pole low-pass coefficient for [CORNER_HZ]. */
    private val leak = (TWO_PI * CORNER_HZ / sampleRate).toFloat()

    /** DC blocker pole. Closer to 1 means a lower corner. */
    private val dcPole = (1.0 - TWO_PI * DC_BLOCK_HZ / sampleRate).toFloat()

    private var integrator = 0f
    private var dcLastInput = 0f
    private var dcLastOutput = 0f

    private val scratch = FloatArray(SCRATCH_SIZE)

    /**
     * How much the chain has to be scaled back up.
     *
     * The leaky integrator attenuates enormously — its output variance is
     * `leak / (2 - leak)` times its input's — and that part is exact arithmetic.
     * The DC blocker after it is not: it is a high-pass, and it removes energy from
     * precisely where brown noise keeps all of its own, which costs another 4 dB
     * that no closed-form factor was accounting for. The first version of this
     * class used only the integrator's factor and came out at -22.2 dBFS instead
     * of -18.
     *
     * So the gain is measured once, at construction, over two seconds of output.
     * That costs under two milliseconds and it stays correct if the corner
     * frequencies ever change, which a hand-tuned constant would not.
     */
    private val makeUpGain: Float = run {
        val analytic = (1.0 / sqrt(leak / (2.0 - leak))).toFloat()
        val probe = FloatArray(CALIBRATION_SAMPLES)
        renderRaw(probe, analytic)
        // Skip the first half second: the integrator starts at zero, so the opening
        // ramp would drag the measurement down.
        val settled = CALIBRATION_SAMPLES / 4
        var sum = 0.0
        for (i in settled until CALIBRATION_SAMPLES) sum += probe[i].toDouble() * probe[i]
        val measured = sqrt(sum / (CALIBRATION_SAMPLES - settled))
        resetFilters()
        white.reset()
        if (measured <= 0.0) analytic else (analytic * (targetRms / measured)).toFloat()
    }

    init {
        // Both filters start at zero, so the first fraction of a second is a
        // transient: the level ramps up from nothing and the DC blocker has not
        // settled, which shows up as an offset around 1e-3. Inaudible on its own —
        // the app fades in over 1,5 s anyway — but it lands in the middle of a
        // crossfade when the user switches sound, where it *is* a dent in the level.
        // So the generator is warmed up before anyone can read from it, and starts
        // settled.
        warmUp()
    }

    override fun generate(out: FloatArray, count: Int) {
        renderRaw(out, makeUpGain, count)
        for (i in 0 until count) out[i] = softLimit(out[i])
    }

    override fun reset() {
        white.reset()
        resetFilters()
        // Reset has to land in the same settled state the constructor leaves, or
        // "reset" and "new" would sound different.
        warmUp()
    }

    /** Runs the chain for a moment and throws the output away. */
    private fun warmUp() {
        val discard = FloatArray(SCRATCH_SIZE)
        var done = 0
        while (done < WARMUP_SAMPLES) {
            val block = minOf(SCRATCH_SIZE, WARMUP_SAMPLES - done)
            renderRaw(discard, makeUpGain, block)
            done += block
        }
    }

    /**
     * The filter chain at a given gain, without the limiter. Shared by generation
     * and by calibration on purpose: calibrating against a different signal path
     * than the one that plays would measure the wrong thing.
     */
    private fun renderRaw(out: FloatArray, gain: Float, count: Int = out.size) {
        var written = 0
        while (written < count) {
            val block = minOf(SCRATCH_SIZE, count - written)
            white.generate(scratch, block)
            for (i in 0 until block) {
                integrator += leak * (scratch[i] - integrator)
                val blocked = integrator - dcLastInput + dcPole * dcLastOutput
                dcLastInput = integrator
                dcLastOutput = blocked
                out[written + i] = blocked * gain
            }
            written += block
        }
    }

    private fun resetFilters() {
        integrator = 0f
        dcLastInput = 0f
        dcLastOutput = 0f
    }

    private companion object {
        const val TWO_PI = 2.0 * Math.PI

        /**
         * Where the -6 dB/octave slope starts. 16 Hz is below what anyone hears, so
         * the whole audible band gets the real slope while the integrator stays
         * bounded.
         */
        const val CORNER_HZ = 16.0

        /** DC blocker corner, well under the audible band. */
        const val DC_BLOCK_HZ = 5.0

        /** Block size for the white noise the integrator eats. */
        const val SCRATCH_SIZE = 1024

        /** Two seconds at 48 kHz: enough for a stable RMS, cheap enough to not notice. */
        const val CALIBRATION_SAMPLES = 96_000

        /**
         * Half a second, which is about thirty time constants of the 16 Hz corner:
         * long past where either filter is still moving.
         */
        const val WARMUP_SAMPLES = 24_000
    }
}

/**
 * A smooth ceiling. Below [THRESHOLD] the signal passes untouched, so ordinary
 * material is not compressed at all; above it, `tanh` bends the curve towards 1
 * with a continuous derivative, which is what keeps the limiter inaudible where
 * hard clipping would buzz.
 */
internal fun softLimit(value: Float): Float {
    val magnitude = abs(value)
    if (magnitude <= THRESHOLD) return value
    val excess = (magnitude - THRESHOLD) / (1f - THRESHOLD)
    val limited = THRESHOLD + (1f - THRESHOLD) * tanh(excess)
    return if (value < 0f) -limited else limited
}

private const val THRESHOLD = 0.85f

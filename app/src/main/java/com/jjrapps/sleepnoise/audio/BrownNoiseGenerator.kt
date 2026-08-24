package com.jjrapps.sleepnoise.audio

/**
 * Brown noise: white noise through a leaky integrator, which is what gives it the
 * -6 dB per octave slope and its deep, rolling character — closer to distant rain
 * than to the hiss of white noise.
 *
 * Two filters, and both matter:
 *
 * 1. **The integrator leaks.** A pure integrator is a random walk: its variance
 *    grows without bound, so it wanders off towards one rail and stays there. The
 *    leak makes the variance finite, at the cost of flattening the response below
 *    [CORNER_HZ], which is under the hearing threshold anyway.
 * 2. **DC is blocked.** Even leaky, the output drifts slowly around zero. That drift
 *    is not heard as sound but as pressure, and it eats headroom from the part of the
 *    signal that is heard.
 *
 * Worth knowing what this sound is *not* good at: nearly all of its energy sits below
 * 250 Hz, so it barely touches a conversation. For covering voices, see
 * [MaskingNoiseGenerator].
 */
class BrownNoiseGenerator(
    seed: Long = WhiteNoiseGenerator.DEFAULT_SEED,
    sampleRate: Int = SAMPLE_RATE,
    targetRms: Float = TARGET_RMS
) : FilteredNoiseGenerator(seed, sampleRate, targetRms) {

    /** One-pole low-pass coefficient for [CORNER_HZ]. */
    private val leak = (TWO_PI * CORNER_HZ / sampleRate).toFloat()

    /** DC blocker pole. Closer to 1 means a lower corner. */
    private val dcPole = (1.0 - TWO_PI * DC_BLOCK_HZ / sampleRate).toFloat()

    private var integrator = 0f
    private var dcLastInput = 0f
    private var dcLastOutput = 0f

    init {
        calibrate()
    }

    override fun filter(sample: Float): Float {
        integrator += leak * (sample - integrator)
        val blocked = integrator - dcLastInput + dcPole * dcLastOutput
        dcLastInput = integrator
        dcLastOutput = blocked
        return blocked
    }

    override fun resetFilter() {
        integrator = 0f
        dcLastInput = 0f
        dcLastOutput = 0f
    }

    private companion object {
        /**
         * Where the -6 dB/octave slope starts. 16 Hz is below what anyone hears, so
         * the whole audible band gets the real slope while the integrator stays
         * bounded.
         */
        const val CORNER_HZ = 16.0

        /** DC blocker corner, well under the audible band. */
        const val DC_BLOCK_HZ = 5.0
    }
}

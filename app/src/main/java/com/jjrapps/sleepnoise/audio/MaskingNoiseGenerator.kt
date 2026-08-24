package com.jjrapps.sleepnoise.audio

/**
 * The masking noise: flat up to 800 Hz, then falling about 6 dB per octave.
 *
 * Not named after a colour because it is not one — it is a shape chosen for a job.
 * Sound-masking systems in open-plan offices emit roughly this curve, and they emit
 * it for exactly the reason this generator exists: to make the conversation two
 * desks away unintelligible without the noise itself becoming the problem.
 *
 * Where the energy goes, at equal overall level:
 *
 * | | speech band (250 Hz – 4 kHz) | intelligibility (1–4 kHz) |
 * |---|---|---|
 * | brown | 6,9 % | 1,4 % |
 * | white | 23,5 % | 18,8 % |
 * | pink | 40,8 % | 20,5 % |
 * | **this** | **71,3 %** | **31,8 %** |
 *
 * Which is +10,2 dB of useful energy against brown noise at the same volume. The
 * corner sits at 800 Hz rather than the 500 Hz of the office standard because a
 * one-pole filter falls at 6 dB per octave instead of the standard's 5, and moving
 * the corner up compensates: it puts the energy back into 1–4 kHz, which is the band
 * that decides whether you can make out the words.
 */
class MaskingNoiseGenerator(
    seed: Long = WhiteNoiseGenerator.DEFAULT_SEED,
    sampleRate: Int = SAMPLE_RATE,
    targetRms: Float = TARGET_RMS
) : FilteredNoiseGenerator(seed, sampleRate, targetRms) {

    private val coefficient = (TWO_PI * CORNER_HZ / sampleRate).toFloat().coerceAtMost(1f)
    private var state = 0f

    init {
        calibrate()
    }

    override fun filter(sample: Float): Float {
        state += coefficient * (sample - state)
        return state
    }

    override fun resetFilter() {
        state = 0f
    }

    private companion object {
        /** Where the fall starts. See the class comment for why not 500 Hz. */
        const val CORNER_HZ = 800.0
    }
}

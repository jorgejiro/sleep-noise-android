package com.jjrapps.sleepnoise.audio

/**
 * Pink noise: equal energy per octave, a fall of 3 dB per octave.
 *
 * The all-rounder, and the one that sounds "flat" to a human ear — hearing works in
 * octaves, not in hertz, so a spectrum that is flat per hertz (white) sounds bright
 * and hissy, while pink sounds even. It is also the best general-purpose masker of
 * the three colours: it puts 40 % of its energy in the 250 Hz – 4 kHz band where
 * speech lives, against white's 23 % and brown's 7 %.
 *
 * The filter is Paul Kellett's refined pink-noise approximation: six one-pole
 * sections in parallel plus a direct path, accurate to about ±0,05 dB from 10 Hz to
 * 20 kHz. A true -3 dB/octave filter is a half-order one and has no exact form as a
 * simple IIR, which is why every implementation of pink noise is an approximation of
 * some kind; this is the one worth using.
 */
class PinkNoiseGenerator(
    seed: Long = WhiteNoiseGenerator.DEFAULT_SEED,
    sampleRate: Int = SAMPLE_RATE,
    targetRms: Float = TARGET_RMS
) : FilteredNoiseGenerator(seed, sampleRate, targetRms) {

    private var b0 = 0f
    private var b1 = 0f
    private var b2 = 0f
    private var b3 = 0f
    private var b4 = 0f
    private var b5 = 0f
    private var b6 = 0f

    /**
     * DC blocker, the same one brown noise needs.
     *
     * Pink noise has unbounded energy as frequency approaches zero, and Kellett's
     * longest pole (0,99886, about 9 Hz) leaves a slow wander that measured 2,9·10⁻³
     * of offset — three times the limit, and headroom spent on something nobody can
     * hear. Below 5 Hz there is no sound to lose.
     */
    private val dcPole = (1.0 - TWO_PI * DC_BLOCK_HZ / sampleRate).toFloat()
    private var dcLastInput = 0f
    private var dcLastOutput = 0f

    init {
        calibrate()
    }

    override fun filter(sample: Float): Float {
        b0 = 0.99886f * b0 + sample * 0.0555179f
        b1 = 0.99332f * b1 + sample * 0.0750759f
        b2 = 0.96900f * b2 + sample * 0.1538520f
        b3 = 0.86650f * b3 + sample * 0.3104856f
        b4 = 0.55000f * b4 + sample * 0.5329522f
        b5 = -0.7616f * b5 - sample * 0.0168980f
        val pink = b0 + b1 + b2 + b3 + b4 + b5 + b6 + sample * 0.5362f
        b6 = sample * 0.115926f

        val blocked = pink - dcLastInput + dcPole * dcLastOutput
        dcLastInput = pink
        dcLastOutput = blocked
        return blocked
    }

    override fun resetFilter() {
        b0 = 0f; b1 = 0f; b2 = 0f; b3 = 0f; b4 = 0f; b5 = 0f; b6 = 0f
        dcLastInput = 0f
        dcLastOutput = 0f
    }

    private companion object {
        const val DC_BLOCK_HZ = 5.0
    }
}

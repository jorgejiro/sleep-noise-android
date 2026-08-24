package com.jjrapps.sleepnoise.audio

/**
 * A source of one channel of noise, in float samples nominally within [-1, 1].
 *
 * Deliberately free of every Android dependency: this is arithmetic, so it can be
 * tested on the JVM — RMS, DC drift, clipping and spectral slope — long before
 * there is a player to hear it through. See ADR 001.
 *
 * Implementations are **not** thread safe: one instance per channel.
 */
interface NoiseGenerator {

    /**
     * Fills `out[0 until count]` with the next samples. Successive calls continue
     * the same stream, so a generator's state carries across buffer boundaries —
     * that is what makes the output seamless where a looped file would seam.
     */
    fun generate(out: FloatArray, count: Int = out.size)

    /** Returns the generator to its initial state. */
    fun reset()
}

/**
 * The level every generator aims for: -18 dBFS RMS.
 *
 * Low on purpose. It leaves 18 dB of headroom for the peaks of a Gaussian signal,
 * which are unbounded in principle, and it means the app's own volume control has
 * somewhere to go rather than starting at the ceiling.
 */
const val TARGET_RMS: Float = 0.12589254f  // 10^(-18/20)

/** The only sample rate the app uses. 48 kHz is what modern Android audio runs at. */
const val SAMPLE_RATE: Int = 48_000

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
 * The level every generator aims for: -12 dBFS RMS.
 *
 * Raised from -18 dBFS, and the reason is one of the app's real uses: covering the
 * noise of a room you cannot leave, through earplugs and headphones. At -18 the app
 * was giving away 6 dB it had no reason to keep — the measured peaks sat 4 dB below
 * full scale and the rest was unused headroom.
 *
 * -12 is where the arithmetic says to stop. A Gaussian signal has no maximum, so the
 * choice is really about how often a sample runs into the ceiling: at -14 dBFS that
 * is once a second, at -12 about thirty times, and at -10 it is several hundred and
 * starts to be audible as a crackle. Thirty soft-limited peaks a second cannot be
 * heard; the limiter is in [softLimit] and every generator now goes through it.
 */
const val TARGET_RMS: Float = 0.25118864f  // 10^(-12/20)

/** The only sample rate the app uses. 48 kHz is what modern Android audio runs at. */
const val SAMPLE_RATE: Int = 48_000

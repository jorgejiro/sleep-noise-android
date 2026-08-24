package com.jjrapps.sleepnoise.audio

/**
 * xoshiro256++ — the pseudo-random generator that feeds the noise.
 *
 * `java.util.Random` was the alternative and it is the wrong tool here: it is
 * synchronised on every call and produces 48 bits per step from a 48-bit state.
 * This one is five lines of arithmetic, has a period of 2^256-1, and passes the
 * statistical batteries that matter for audio. At 48 kHz × 2 channels it runs
 * about a hundred thousand times a second, so "five lines" is the point.
 *
 * Not thread safe, by design: one instance per channel, each owned by the thread
 * that fills its buffer.
 */
class Xoshiro256PlusPlus(seed: Long) {

    private var s0: Long
    private var s1: Long
    private var s2: Long
    private var s3: Long

    init {
        // splitmix64 to spread one seed over the four words. Seeding xoshiro
        // directly from a small number leaves the first outputs correlated.
        var z = seed
        fun splitMix64(): Long {
            z += -0x61c8864680b583ebL
            var x = z
            x = (x xor (x ushr 30)) * -0x40a7b892e31b1a47L
            x = (x xor (x ushr 27)) * -0x6b2fb644ecceee15L
            return x xor (x ushr 31)
        }
        s0 = splitMix64()
        s1 = splitMix64()
        s2 = splitMix64()
        s3 = splitMix64()
        // An all-zero state is a fixed point that only ever emits zeros.
        if (s0 == 0L && s1 == 0L && s2 == 0L && s3 == 0L) s0 = 1L
    }

    fun nextLong(): Long {
        val result = java.lang.Long.rotateLeft(s0 + s3, 23) + s0
        val t = s1 shl 17
        s2 = s2 xor s0
        s3 = s3 xor s1
        s1 = s1 xor s2
        s0 = s0 xor s3
        s2 = s2 xor t
        s3 = java.lang.Long.rotateLeft(s3, 45)
        return result
    }

    /** Uniform in [0, 1). Uses the top 53 bits, which are the well-mixed ones. */
    fun nextDouble(): Double = (nextLong() ushr 11) * DOUBLE_UNIT

    private companion object {
        /** 2^-53 */
        const val DOUBLE_UNIT = 1.0 / (1L shl 53)
    }
}

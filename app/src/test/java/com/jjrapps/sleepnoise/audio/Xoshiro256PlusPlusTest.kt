package com.jjrapps.sleepnoise.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Not a statistical audit of xoshiro — that has been done by people with better
 * tools. These are the failures that a hand-typed implementation actually has:
 * a transcribed rotation constant, a shift in the wrong direction, or a seeding
 * routine that leaves the first outputs correlated.
 */
class Xoshiro256PlusPlusTest {

    @Test
    fun `the same seed gives the same sequence`() {
        val a = Xoshiro256PlusPlus(42).let { rng -> LongArray(64) { rng.nextLong() } }
        val b = Xoshiro256PlusPlus(42).let { rng -> LongArray(64) { rng.nextLong() } }
        assertTrue(a.contentEquals(b))
    }

    @Test
    fun `adjacent seeds give unrelated sequences`() {
        // This is what splitmix64 seeding buys. Feeding the state directly from a
        // small number leaves neighbouring seeds producing near-identical output,
        // and the two stereo channels would end up correlated.
        val a = Xoshiro256PlusPlus(1).let { rng -> LongArray(256) { rng.nextLong() } }
        val b = Xoshiro256PlusPlus(2).let { rng -> LongArray(256) { rng.nextLong() } }
        val shared = a.indices.count { a[it] == b[it] }
        assertEquals("$shared of 256 values coincide", 0, shared)
    }

    @Test
    fun `doubles stay inside the unit interval`() {
        val rng = Xoshiro256PlusPlus(7)
        repeat(200_000) {
            val value = rng.nextDouble()
            assertTrue("out of range: $value", value >= 0.0 && value < 1.0)
        }
    }

    @Test
    fun `doubles are spread evenly, one bucket per tenth`() {
        val rng = Xoshiro256PlusPlus(99)
        val buckets = IntArray(10)
        val draws = 1_000_000
        repeat(draws) { buckets[(rng.nextDouble() * 10).toInt()]++ }
        val expected = draws / 10
        for ((index, count) in buckets.withIndex()) {
            // ±1% is loose for a million draws, and that is deliberate: this test
            // should fail on a broken generator, not on a bad afternoon.
            assertEquals("bucket $index held $count", expected.toDouble(), count.toDouble(), expected * 0.01)
        }
    }

    @Test
    fun `a zero seed still produces noise`() {
        // An all-zero xoshiro state is a fixed point that emits nothing but zeros
        // for ever. Seeding through splitmix64 avoids it; this proves it.
        val rng = Xoshiro256PlusPlus(0)
        val values = LongArray(32) { rng.nextLong() }
        assertTrue("the generator produced only zeros", values.any { it != 0L })
        assertNotEquals(values[0], values[1])
    }
}

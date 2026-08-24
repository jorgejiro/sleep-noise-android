package com.jjrapps.sleepnoise.playback

import kotlin.math.log10
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The volume curve is one line of code and the single most-touched control in the
 * app, so it is worth pinning down what it promises.
 */
class VolumeCurveTest {

    private fun db(gain: Float) = 20.0 * log10(gain.toDouble())

    @Test
    fun `the ends are the ends`() {
        assertEquals(0f, volumeToGain(0), 0f)
        assertEquals(1f, volumeToGain(100), 0f)
    }

    @Test
    fun `the middle of the slider is about minus twelve decibels`() {
        // Which is a sensible middle rather than an arithmetic one: linear would put
        // 50 at -6 dB, barely quieter than full, and cram everything usable into the
        // bottom of the travel.
        assertEquals(-12.0, db(volumeToGain(50)), 0.1)
    }

    @Test
    fun `the curve never goes backwards`() {
        var previous = -1f
        for (volume in 0..100) {
            val gain = volumeToGain(volume)
            assertTrue("gain fell at $volume", gain >= previous)
            previous = gain
        }
    }

    @Test
    fun `values outside the range are clamped, not wrapped`() {
        assertEquals(0f, volumeToGain(-20), 0f)
        assertEquals(1f, volumeToGain(500), 0f)
    }

    @Test
    fun `each step near the bottom is audible, which is where quiet listening lives`() {
        // Someone falling asleep works at the quiet end. Steps there must not be so
        // fine that the control does nothing, nor so coarse that it jumps.
        val step = db(volumeToGain(11)) - db(volumeToGain(10))
        assertTrue("a step at the quiet end was $step dB", step > 0.5 && step < 2.0)
    }
}

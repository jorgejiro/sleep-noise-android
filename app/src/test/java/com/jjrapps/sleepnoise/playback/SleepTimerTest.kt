package com.jjrapps.sleepnoise.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The countdown, driven by a clock the test moves by hand. Waiting ninety real
 * minutes to find out whether a timer expires is not a test, it is a nap.
 */
class SleepTimerTest {

    private var now = 1_000_000L
    private val timer = SleepTimer { now }

    private fun advance(minutes: Long) {
        now += minutes * 60_000L
    }

    @Test
    fun `a timer counts down and expires`() {
        timer.start(30)
        assertTrue(timer.isSet)
        assertEquals(30 * 60_000L, timer.remainingMillis)
        advance(29)
        assertFalse(timer.hasExpired)
        advance(1)
        assertTrue(timer.hasExpired)
        assertEquals(0L, timer.remainingMillis)
    }

    @Test
    fun `zero minutes means no timer`() {
        timer.start(0)
        assertFalse(timer.isSet)
        assertFalse(timer.hasExpired)
    }

    @Test
    fun `pausing freezes the countdown instead of letting it run out`() {
        // The case that matters: pause at bedtime, come back twenty minutes later,
        // and the timer should still have what was left — not have expired quietly
        // while nothing was playing.
        timer.start(30)
        advance(10)
        timer.freeze()
        val left = timer.remainingMillis
        advance(20)
        assertEquals("a frozen timer moved", left, timer.remainingMillis)
        assertFalse("a frozen timer expired", timer.hasExpired)
        timer.resume()
        assertEquals(left, timer.remainingMillis)
        advance(20)
        assertTrue(timer.hasExpired)
    }

    @Test
    fun `extending adds to what is left`() {
        timer.start(30)
        advance(25)
        timer.extend(15)
        assertEquals(20 * 60_000L, timer.remainingMillis)
        assertEquals(45, timer.totalMinutes)
    }

    @Test
    fun `extending a frozen timer keeps it frozen`() {
        timer.start(30)
        advance(10)
        timer.freeze()
        timer.extend(15)
        assertTrue(timer.isFrozen)
        assertEquals(35 * 60_000L, timer.remainingMillis)
    }

    @Test
    fun `extending when nothing is set does nothing`() {
        timer.extend(15)
        assertFalse(timer.isSet)
        assertEquals(0, timer.totalMinutes)
    }

    @Test
    fun `the closing fade starts one minute out and reaches silence at zero`() {
        timer.start(15)
        assertEquals("fading too early", 0f, timer.fadeProgress(), 0.001f)
        advance(14)
        assertEquals("fade should just be starting", 0f, timer.fadeProgress(), 0.02f)
        now += 30_000L
        assertEquals("halfway through the fade", 0.5f, timer.fadeProgress(), 0.02f)
        now += 30_000L
        assertEquals("silent at zero", 1f, timer.fadeProgress(), 0.001f)
    }

    @Test
    fun `a frozen timer does not fade`() {
        // Otherwise pausing during the last minute would leave the volume stuck down
        // where the fade had got to, and resuming would come back quieter.
        timer.start(15)
        advance(14)
        now += 30_000L
        timer.freeze()
        assertEquals(0f, timer.fadeProgress(), 0.001f)
    }

    @Test
    fun `cancelling clears everything`() {
        timer.start(30)
        advance(5)
        timer.cancel()
        assertFalse(timer.isSet)
        assertEquals(0, timer.totalMinutes)
        assertEquals(0L, timer.remainingMillis)
    }
}

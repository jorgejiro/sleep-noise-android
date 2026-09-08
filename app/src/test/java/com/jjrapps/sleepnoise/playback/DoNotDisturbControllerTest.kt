package com.jjrapps.sleepnoise.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What these tests are really about is the invariant: the app gives back exactly what
 * it took, and nothing else. Getting that wrong means either a phone left silent all
 * day or someone's own Do Not Disturb switched off behind their back.
 */
class DoNotDisturbControllerTest {

    private class FakeZen(override var isAccessGranted: Boolean = true) : ZenGateway {
        var silenced = false
            private set
        var silenceCalls = 0
            private set
        var restoreCalls = 0
            private set

        override fun silence() {
            silenced = true
            silenceCalls++
        }

        override fun restore() {
            silenced = false
            restoreCalls++
        }
    }

    @Test
    fun `acquires and releases the silence`() {
        val zen = FakeZen()
        val controller = DoNotDisturbController(zen)

        assertTrue(controller.acquire())
        assertTrue(controller.isHeld)
        assertTrue(zen.silenced)

        controller.release()
        assertFalse(controller.isHeld)
        assertFalse(zen.silenced)
    }

    @Test
    fun `does nothing without the access, and says so`() {
        val zen = FakeZen(isAccessGranted = false)
        val controller = DoNotDisturbController(zen)

        assertFalse(controller.acquire())
        assertFalse(controller.isHeld)
        assertEquals(0, zen.silenceCalls)
    }

    @Test
    fun `never gives back a silence it did not take`() {
        val zen = FakeZen()
        val controller = DoNotDisturbController(zen)

        // Somebody else's Do Not Disturb: pausing the noise must not touch it.
        controller.release()

        assertEquals(0, zen.restoreCalls)
    }

    @Test
    fun `asking twice silences once`() {
        val zen = FakeZen()
        val controller = DoNotDisturbController(zen)

        controller.acquire()
        controller.acquire()

        assertEquals(1, zen.silenceCalls)
    }

    @Test
    fun `releasing twice restores once`() {
        val zen = FakeZen()
        val controller = DoNotDisturbController(zen)
        controller.acquire()

        controller.release()
        controller.release()

        assertEquals(1, zen.restoreCalls)
    }

    @Test
    fun `still tries to give it back after losing the access mid-session`() {
        val zen = FakeZen()
        val controller = DoNotDisturbController(zen)
        controller.acquire()

        // Revoked from a screen this app does not control, while the noise plays.
        zen.isAccessGranted = false
        controller.release()

        // The attempt is made anyway: the gateway swallows a refusal, so trying costs
        // nothing, and not trying would throw away a release the system might allow.
        assertFalse(controller.isHeld)
        assertEquals(1, zen.restoreCalls)
    }

    @Test
    fun `hands back a silence left over by a killed session`() {
        val zen = FakeZen()
        val controller = DoNotDisturbController(zen)

        // A fresh controller in a fresh process: it holds nothing as far as it knows,
        // and the phone is silent all the same.
        controller.releaseStale()

        assertEquals(1, zen.restoreCalls)
        assertFalse(controller.isHeld)
    }
}

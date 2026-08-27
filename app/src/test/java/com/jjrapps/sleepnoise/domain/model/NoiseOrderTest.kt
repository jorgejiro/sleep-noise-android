package com.jjrapps.sleepnoise.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The order behind the arrows in the notification.
 *
 * Two lines of code, and worth pinning down anyway: the arrows exist so that no
 * button in the notification is ever dead, and an order that stopped wrapping
 * would quietly bring the dead button back at both ends. See ADR 007.
 */
class NoiseOrderTest {

    @Test
    fun `next follows the order the app shows`() {
        assertEquals(NoiseType.Pink, NoiseType.White.next())
        assertEquals(NoiseType.Brown, NoiseType.Pink.next())
        assertEquals(NoiseType.Masking, NoiseType.Brown.next())
    }

    @Test
    fun `previous walks it backwards`() {
        assertEquals(NoiseType.Brown, NoiseType.Masking.previous())
        assertEquals(NoiseType.Pink, NoiseType.Brown.previous())
        assertEquals(NoiseType.White, NoiseType.Pink.previous())
    }

    @Test
    fun `both ends wrap round, so neither arrow is ever dead`() {
        assertEquals(NoiseType.White, NoiseType.Masking.next())
        assertEquals(NoiseType.Masking, NoiseType.White.previous())
    }

    @Test
    fun `going forward through every sound comes back to where it started`() {
        var type = NoiseType.Default
        repeat(NoiseType.entries.size) { type = type.next() }
        assertEquals(NoiseType.Default, type)
    }
}

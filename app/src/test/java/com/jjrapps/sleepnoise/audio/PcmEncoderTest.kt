package com.jjrapps.sleepnoise.audio

import org.junit.Assert.assertEquals
import org.junit.Test

class PcmEncoderTest {

    private fun decode(bytes: ByteArray, index: Int): Int {
        val lo = bytes[index * 2].toInt() and 0xFF
        val hi = bytes[index * 2 + 1].toInt()
        return (hi shl 8) or lo      // hi keeps its sign, which is the point
    }

    @Test
    fun `silence encodes to zero`() {
        val out = ByteArray(4)
        PcmEncoder.encode(floatArrayOf(0f, 0f), 2, out, 0)
        assertEquals(0, decode(out, 0))
        assertEquals(0, decode(out, 1))
    }

    @Test
    fun `full scale stops one short of wrapping around`() {
        // Scaling by 32768 instead of 32767 sends +1.0 to -32768: the loudest
        // possible sample becomes the most negative one, which is a click.
        val out = ByteArray(4)
        PcmEncoder.encode(floatArrayOf(1f, -1f), 2, out, 0)
        assertEquals(32767, decode(out, 0))
        assertEquals(-32767, decode(out, 1))
    }

    @Test
    fun `samples past full scale are clamped, not wrapped`() {
        val out = ByteArray(4)
        PcmEncoder.encode(floatArrayOf(1.5f, -1.5f), 2, out, 0)
        assertEquals(32767, decode(out, 0))
        assertEquals(-32767, decode(out, 1))
    }

    @Test
    fun `bytes come out little endian, which is what WAV declares`() {
        val out = ByteArray(2)
        PcmEncoder.encode(floatArrayOf(256f / 32767f), 1, out, 0)
        assertEquals(0x00, out[0].toInt() and 0xFF)
        assertEquals(0x01, out[1].toInt() and 0xFF)
    }

    @Test
    fun `encoding writes at the offset it was given and reports the length`() {
        val out = ByteArray(10)
        val written = PcmEncoder.encode(floatArrayOf(1f, 1f), 2, out, 4)
        assertEquals(4, written)
        assertEquals(0, out[0].toInt())          // untouched before the offset
        assertEquals(32767, decode(out, 2))      // and written from it
    }

    @Test
    fun `the round trip keeps the level it was given`() {
        val samples = FloatArray(48_000)
        WhiteNoiseGenerator().generate(samples)
        val bytes = ByteArray(samples.size * 2)
        PcmEncoder.encode(samples, samples.size, bytes, 0)

        val decoded = FloatArray(samples.size) { decode(bytes, it) / 32767f }
        // Quantisation to 16 bits costs about 0.0001 dB at this level. If this ever
        // drifts, something in the encoder is scaling twice.
        assertEquals(db(rms(samples)), db(rms(decoded)), 0.01)
    }
}

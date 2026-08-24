package com.jjrapps.sleepnoise.audio

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The header is 44 bytes that Media3's extractor parses before it will play
 * anything, and a single field in the wrong place means silence with no error. So
 * every field gets read back.
 */
class WavHeaderTest {

    private val header = WavHeader.create()

    private fun ascii(at: Int, length: Int) =
        String(header, at, length, Charsets.US_ASCII)

    private fun le16(at: Int) =
        (header[at].toInt() and 0xFF) or ((header[at + 1].toInt() and 0xFF) shl 8)

    private fun le32(at: Int): Long {
        var value = 0L
        for (i in 3 downTo 0) value = (value shl 8) or (header[at + i].toLong() and 0xFF)
        return value
    }

    @Test
    fun `the chunk names are where a parser looks for them`() {
        assertEquals(WavHeader.SIZE, header.size)
        assertEquals("RIFF", ascii(0, 4))
        assertEquals("WAVE", ascii(8, 4))
        assertEquals("fmt ", ascii(12, 4))   // the trailing space is part of the name
        assertEquals("data", ascii(36, 4))
    }

    @Test
    fun `the format describes 48 kHz 16-bit stereo PCM`() {
        assertEquals(16L, le32(16))          // fmt chunk length
        assertEquals(1, le16(20))            // 1 = uncompressed PCM
        assertEquals(2, le16(22))            // channels
        assertEquals(SAMPLE_RATE.toLong(), le32(24))
        assertEquals(16, le16(34))           // bits per sample
    }

    @Test
    fun `the derived rates agree with the format, which is what a decoder trusts`() {
        val channels = le16(22)
        val bits = le16(34)
        assertEquals("byte rate", (SAMPLE_RATE * channels * bits / 8).toLong(), le32(28))
        assertEquals("block align", channels * bits / 8, le16(32))
    }

    @Test
    fun `the RIFF size covers everything after its own field`() {
        // Off-by-36 here is the classic WAV bug: some parsers read past the end and
        // others stop early, and neither says why.
        assertEquals(le32(40) + 36, le32(4))
    }

    @Test
    fun `the declared length is a whole number of frames`() {
        // A data chunk ending mid-frame would leave the last sample split across
        // channels, and every frame after a repeat shifted by one.
        assertEquals(0L, WavHeader.MAX_DATA_BYTES % 4L)
        assertEquals(WavHeader.MAX_DATA_BYTES, le32(40))
    }

    @Test
    fun `the declared length really is under seven hours, which is why playback repeats`() {
        val seconds = WavHeader.MAX_DATA_BYTES / (SAMPLE_RATE * 2 * 2)
        // Documenting the constraint that forces REPEAT_MODE_ONE: an unsigned 32-bit
        // byte count cannot describe a full night at this rate.
        assertEquals(6.2, seconds / 3600.0, 0.2)
    }
}

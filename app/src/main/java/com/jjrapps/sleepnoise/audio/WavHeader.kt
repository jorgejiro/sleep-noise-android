package com.jjrapps.sleepnoise.audio

/**
 * The 44-byte canonical WAV header, so Media3's own `WavExtractor` can read a
 * stream that no file ever produced.
 *
 * Writing a header instead of teaching ExoPlayer about a new format is the whole
 * trick of ADR 001: the synthesiser pretends to be a WAV file, and everything
 * downstream — extractor, renderer, audio sink — carries on as usual.
 */
object WavHeader {

    const val SIZE = 44

    /**
     * A WAV `data` chunk length is an unsigned 32-bit count of bytes, so the format
     * cannot describe a stream longer than about 6.2 hours of 48 kHz stereo — less
     * than one night.
     *
     * That is fine, because the declared length is a contract with the extractor
     * and not a limit on the generator. The player repeats the item, and the
     * generator keeps its state across the repeat, so what the listener hears is
     * one continuous stream: no seam, no restart, nothing to notice. This is why
     * [com.jjrapps.sleepnoise.playback.NoisePlayer] sets `REPEAT_MODE_ONE`.
     *
     * Not quite the full 32-bit range: the `RIFF` field earlier in the header holds
     * `dataBytes + 36`, so a data length at the ceiling overflows *that* field and
     * wraps to 32. The first version of this constant did exactly that and the
     * header was invalid — a test caught it, nothing else would have. So the room
     * for those 36 bytes comes off the top, and then it is rounded down to a whole
     * frame, because a data chunk ending mid-frame leaves every sample after a
     * repeat shifted by one channel.
     */
    const val MAX_DATA_BYTES: Long = (0xFFFFFFFFL - 36L) / 4L * 4L

    fun create(
        sampleRate: Int = SAMPLE_RATE,
        channels: Int = 2,
        bitsPerSample: Int = 16,
        dataBytes: Long = MAX_DATA_BYTES
    ): ByteArray {
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8
        val header = ByteArray(SIZE)
        var at = 0

        fun ascii(text: String) {
            for (c in text) header[at++] = c.code.toByte()
        }
        fun le32(value: Long) {
            header[at++] = (value and 0xFF).toByte()
            header[at++] = ((value shr 8) and 0xFF).toByte()
            header[at++] = ((value shr 16) and 0xFF).toByte()
            header[at++] = ((value shr 24) and 0xFF).toByte()
        }
        fun le16(value: Int) {
            header[at++] = (value and 0xFF).toByte()
            header[at++] = ((value shr 8) and 0xFF).toByte()
        }

        ascii("RIFF")
        le32(dataBytes + 36)          // everything after this field
        ascii("WAVE")
        ascii("fmt ")
        le32(16)                      // PCM fmt chunk size
        le16(1)                       // format 1 = uncompressed PCM
        le16(channels)
        le32(sampleRate.toLong())
        le32(byteRate.toLong())
        le16(blockAlign)
        le16(bitsPerSample)
        ascii("data")
        le32(dataBytes)
        return header
    }
}

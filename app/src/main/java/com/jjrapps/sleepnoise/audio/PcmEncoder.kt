package com.jjrapps.sleepnoise.audio

/**
 * Float samples to 16-bit little-endian PCM, which is what the audio pipeline
 * consumes.
 *
 * No dithering. Dither exists to break up the correlation between a quantisation
 * error and the signal that caused it, which matters for tonal material; here the
 * signal *is* noise, several thousand times louder than the last bit, so adding
 * more noise to hide noise would be a joke.
 */
object PcmEncoder {

    const val BYTES_PER_SAMPLE = 2

    /**
     * Writes `count` samples from [samples] into [out] starting at [outOffset].
     * Returns the number of bytes written.
     */
    fun encode(samples: FloatArray, count: Int, out: ByteArray, outOffset: Int): Int {
        var index = outOffset
        for (i in 0 until count) {
            // 32767 and not 32768: the positive side of a signed 16-bit range stops
            // one short, and scaling by 32768 would wrap the loudest sample around
            // to full negative — a click, not a clip.
            val value = (clampToUnit(samples[i]) * 32767f).toInt()
            out[index++] = (value and 0xFF).toByte()
            out[index++] = ((value shr 8) and 0xFF).toByte()
        }
        return count * BYTES_PER_SAMPLE
    }
}

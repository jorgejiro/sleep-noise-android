package com.jjrapps.sleepnoise.audio

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log2
import kotlin.math.sqrt

/**
 * Test-only spectrum analysis. Small enough to read in one sitting, which matters:
 * a test helper nobody understands is a test nobody trusts.
 *
 * Welch's method — average the periodograms of several overlapping windowed blocks
 * — because a single FFT of noise is itself noisy. One block would give a spectrum
 * scattered by several dB per bin and no slope could be fitted through it.
 */
object SpectrumAnalysis {

    /**
     * Power spectral density in dB per bin, averaged over blocks of [fftSize].
     * Returns bins `0 until fftSize / 2`.
     */
    fun powerSpectrumDb(samples: FloatArray, fftSize: Int = 4096): DoubleArray {
        require(Integer.bitCount(fftSize) == 1) { "fftSize must be a power of two" }
        val hop = fftSize / 2                       // 50% overlap
        val window = DoubleArray(fftSize) { 0.5 - 0.5 * cos(2.0 * PI * it / fftSize) }
        val accumulated = DoubleArray(fftSize / 2)
        var blocks = 0

        var start = 0
        while (start + fftSize <= samples.size) {
            val re = DoubleArray(fftSize)
            val im = DoubleArray(fftSize)
            for (i in 0 until fftSize) re[i] = samples[start + i] * window[i]
            fft(re, im)
            for (bin in 0 until fftSize / 2) {
                accumulated[bin] += re[bin] * re[bin] + im[bin] * im[bin]
            }
            blocks++
            start += hop
        }
        require(blocks > 0) { "not enough samples for one block of $fftSize" }

        return DoubleArray(fftSize / 2) { bin ->
            val power = accumulated[bin] / blocks
            // Floor keeps log of zero out of the result; -200 dB is far below
            // anything the tests look at.
            if (power <= 0.0) -200.0 else 10.0 * kotlin.math.log10(power)
        }
    }

    /**
     * Least-squares slope of the spectrum in **dB per octave**, fitted over
     * [fromHz]..[toHz].
     *
     * Fitted against log2 of frequency, so the result reads directly as the number
     * quoted in the specification: 0 for white noise, -6 for brown.
     */
    fun slopeDbPerOctave(
        spectrumDb: DoubleArray,
        sampleRate: Int,
        fftSize: Int,
        fromHz: Double,
        toHz: Double,
        minBins: Int = 8
    ): Double {
        val binHz = sampleRate.toDouble() / fftSize
        var n = 0
        var sumX = 0.0
        var sumY = 0.0
        var sumXy = 0.0
        var sumXx = 0.0
        for (bin in spectrumDb.indices) {
            val hz = bin * binHz
            if (hz < fromHz || hz > toHz) continue
            val x = log2(hz)
            val y = spectrumDb[bin]
            n++
            sumX += x
            sumY += y
            sumXy += x * y
            sumXx += x * x
        }
        require(n >= minBins) { "only $n bins between $fromHz and $toHz Hz" }
        return (n * sumXy - sumX * sumY) / (n * sumXx - sumX * sumX)
    }

    /** Iterative radix-2 Cooley-Tukey, in place. */
    private fun fft(re: DoubleArray, im: DoubleArray) {
        val n = re.size
        // Bit-reversal permutation.
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j or bit
            if (i < j) {
                re[i] = re[j].also { re[j] = re[i] }
                im[i] = im[j].also { im[j] = im[i] }
            }
        }
        var len = 2
        while (len <= n) {
            val angle = -2.0 * PI / len
            val wRe = cos(angle)
            val wIm = kotlin.math.sin(angle)
            var i = 0
            while (i < n) {
                var curRe = 1.0
                var curIm = 0.0
                for (k in 0 until len / 2) {
                    val aRe = re[i + k]
                    val aIm = im[i + k]
                    val bRe = re[i + k + len / 2] * curRe - im[i + k + len / 2] * curIm
                    val bIm = re[i + k + len / 2] * curIm + im[i + k + len / 2] * curRe
                    re[i + k] = aRe + bRe
                    im[i + k] = aIm + bIm
                    re[i + k + len / 2] = aRe - bRe
                    im[i + k + len / 2] = aIm - bIm
                    val nextRe = curRe * wRe - curIm * wIm
                    curIm = curRe * wIm + curIm * wRe
                    curRe = nextRe
                }
                i += len
            }
            len = len shl 1
        }
    }
}

/** Root mean square of a buffer. */
fun rms(samples: FloatArray): Double {
    var sum = 0.0
    for (s in samples) sum += s.toDouble() * s
    return sqrt(sum / samples.size)
}

/** Mean — the DC offset. Should sit at zero for any noise worth the name. */
fun mean(samples: FloatArray): Double {
    var sum = 0.0
    for (s in samples) sum += s
    return sum / samples.size
}

/** Peak absolute value. */
fun peak(samples: FloatArray): Float {
    var max = 0f
    for (s in samples) {
        val a = if (s < 0f) -s else s
        if (a > max) max = a
    }
    return max
}

/** Pearson correlation, for checking the two channels are genuinely independent. */
fun correlation(a: FloatArray, b: FloatArray): Double {
    require(a.size == b.size)
    val meanA = mean(a)
    val meanB = mean(b)
    var cov = 0.0
    var varA = 0.0
    var varB = 0.0
    for (i in a.indices) {
        val da = a[i] - meanA
        val db = b[i] - meanB
        cov += da * db
        varA += da * da
        varB += db * db
    }
    return cov / sqrt(varA * varB)
}

/** dB helper, guarding against log of zero. */
fun db(value: Double): Double = if (value <= 0.0) -200.0 else 20.0 * ln(value) / ln(10.0)

package com.jjrapps.sleepnoise.audio

import com.jjrapps.sleepnoise.domain.model.NoiseType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The spectral shape, which is what actually distinguishes the two sounds. White
 * noise is flat; brown noise falls 6 dB per octave. Everything else — level, DC,
 * peaks — could be right while these were wrong, and the app would be shipping two
 * sounds that are not what they claim.
 */
class NoiseSpectrumTest {

    private fun report(what: String, value: Any) = println("  [medida] %-34s %s".format(what, value))


    private val fftSize = 4096
    private val samples = 480_000

    private fun render(type: NoiseType): FloatArray {
        val buffer = FloatArray(samples)
        createGenerator(type).generate(buffer, samples)
        return buffer
    }

    /**
     * Writes the two measured spectra out as CSV.
     *
     * The assertions above prove the slopes are right; this is so a person can look
     * at the curves. A number in a green test tells you it passed, not what the
     * noise looks like — and the shape is the product here.
     */
    @Test
    fun `the measured spectra are written out for inspection`() {
        val out = java.io.File("build/reports/noise-spectrum.csv")
        out.parentFile.mkdirs()
        val binHz = SAMPLE_RATE.toDouble() / fftSize
        val white = SpectrumAnalysis.powerSpectrumDb(render(NoiseType.White), fftSize)
        val brown = SpectrumAnalysis.powerSpectrumDb(render(NoiseType.Brown), fftSize)
        out.printWriter().use { writer ->
            writer.println("hz,white_db,brown_db")
            for (bin in 1 until white.size) {
                val hz = bin * binHz
                if (hz < 15.0 || hz > 20_000.0) continue
                // Locale.ROOT o el separador decimal sale coma en un sistema en
                // español y el CSV queda inparseable.
                writer.println(
                    "%.2f,%.3f,%.3f".format(java.util.Locale.ROOT, hz, white[bin], brown[bin])
                )
            }
        }
        assertTrue("no spectrum was written", out.length() > 0)
    }

    @Test
    fun `white noise is flat across the audible band`() {
        val spectrum = SpectrumAnalysis.powerSpectrumDb(render(NoiseType.White), fftSize)
        val slope = SpectrumAnalysis.slopeDbPerOctave(
            spectrum, SAMPLE_RATE, fftSize, fromHz = 100.0, toHz = 10_000.0
        )
        report("blanco, pendiente 100 Hz - 10 kHz", "%.2f dB/octava".format(slope))
        assertEquals("white noise should be flat, measured $slope dB/octave", 0.0, slope, 0.4)
    }

    @Test
    fun `brown noise falls six decibels per octave`() {
        val spectrum = SpectrumAnalysis.powerSpectrumDb(render(NoiseType.Brown), fftSize)
        // Measured from 100 Hz up: below roughly ten times the 16 Hz corner the
        // leaky integrator is still flat, and including that region would drag the
        // fitted slope towards zero and hide a real defect.
        val slope = SpectrumAnalysis.slopeDbPerOctave(
            spectrum, SAMPLE_RATE, fftSize, fromHz = 100.0, toHz = 10_000.0
        )
        report("marrón, pendiente 100 Hz - 10 kHz", "%.2f dB/octava".format(slope))
        assertEquals("brown noise should fall 6 dB/octave, measured $slope", -6.0, slope, 0.6)
    }

    @Test
    fun `brown noise really is flat below its corner, which is why it stays bounded`() {
        // 65536-point FFT, not the 4096 the other tests use: at 48 kHz that is a
        // 0,73 Hz bin instead of 11,7, and looking under a 16 Hz corner with 11,7 Hz
        // bins gave a single bin to fit a line through. Resolution, not tolerance.
        val fineFft = 65_536
        val spectrum = SpectrumAnalysis.powerSpectrumDb(render(NoiseType.Brown), fineFft)
        val slope = SpectrumAnalysis.slopeDbPerOctave(
            spectrum, SAMPLE_RATE, fineFft, fromHz = 3.0, toHz = 12.0, minBins = 4
        )
        // Well above -6: the flattening under the corner is what stops the
        // integrator wandering off, and it sits below anything anyone hears.
        report("marrón, pendiente 3 - 12 Hz", "%.2f dB/octava".format(slope))
        assertTrue("slope under the corner was $slope dB/octave", slope > -4.0)
    }

    @Test
    fun `white channels are independent, measured tightly`() {
        val (left, right) = channels(NoiseType.White, frames = 240_000)
        val r = correlation(left, right)
        report("blanco, correlación entre canales", "%.4f".format(r))
        // White noise has no memory, so 240.000 samples really are 240.000
        // independent draws and the correlation of two unrelated streams should
        // land within a few thousandths of zero. Duplicated channels would
        // measure exactly 1.0 and collapse to a point in the middle of the head.
        assertEquals("channel correlation was $r", 0.0, r, 0.01)
        assertTrue("a channel is silent", rms(left) > 0.01 && rms(right) > 0.01)
    }

    @Test
    fun `brown channels are independent too, within what its memory allows`() {
        val (left, right) = channels(NoiseType.Brown, frames = 240_000)
        val r = correlation(left, right)
        report("marrón, correlación entre canales", "%.4f".format(r))
        // A looser bound, and not out of laziness. Brown noise is correlated with
        // itself over about 10 ms — that is what the 16 Hz corner means — so those
        // 240.000 samples are worth only some 500 independent ones, and the
        // estimator's own spread is around 0.045. The first version of this test
        // asked for 0.02 and failed at -0.039, which was well inside one standard
        // error: the test was wrong, not the generator.
        assertEquals("channel correlation was $r", 0.0, r, 0.15)
        assertTrue("a channel is silent", rms(left) > 0.01 && rms(right) > 0.01)
    }

    private fun channels(type: NoiseType, frames: Int): Pair<FloatArray, FloatArray> {
        val interleaved = FloatArray(frames * 2)
        StereoNoiseSource(type).generate(interleaved, frames)
        return FloatArray(frames) { interleaved[it * 2] } to
            FloatArray(frames) { interleaved[it * 2 + 1] }
    }

    @Test
    fun `both channels carry the same spectral shape despite being independent`() {
        val frames = 240_000
        val interleaved = FloatArray(frames * 2)
        StereoNoiseSource(NoiseType.Brown).generate(interleaved, frames)

        val slopes = listOf(0, 1).map { channel ->
            val samples = FloatArray(frames) { interleaved[it * 2 + channel] }
            SpectrumAnalysis.slopeDbPerOctave(
                SpectrumAnalysis.powerSpectrumDb(samples, fftSize),
                SAMPLE_RATE, fftSize, fromHz = 100.0, toHz = 10_000.0
            )
        }
        // Decorrelated is not the same as different: both ears must hear the same
        // colour of noise, or the image pulls to one side.
        assertEquals("left ${slopes[0]}, right ${slopes[1]}", slopes[0], slopes[1], 0.5)
    }
}

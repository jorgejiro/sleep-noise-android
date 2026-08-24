package com.jjrapps.sleepnoise.audio

import com.jjrapps.sleepnoise.domain.model.NoiseType
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Level, DC and clipping — the three ways a noise generator sounds wrong while
 * looking fine. None of these needs Android, so they run in milliseconds on the
 * JVM and there is no excuse for not having them.
 */
class NoiseLevelTest {

    /**
     * Every measurement gets printed, not just asserted.
     *
     * The specification asks for these numbers to be recorded (§16), and a green
     * test that only says "passed" records nothing: it hides whether brown noise
     * sits at -18,0 dBFS or at -18,29, and drift shows up in the second decimal
     * long before it trips a tolerance.
     */
    private fun report(what: String, value: Any) = println("  [medida] %-34s %s".format(what, value))


    private val samples = 480_000   // ten seconds at 48 kHz

    private fun render(type: NoiseType): FloatArray {
        val buffer = FloatArray(samples)
        // Deliberately not one big call: the generator has to sound the same across
        // buffer boundaries, because in the app it is called once per audio block.
        val generator = createGenerator(type)
        var written = 0
        val block = 1024
        val chunk = FloatArray(block)
        while (written < samples) {
            val n = minOf(block, samples - written)
            generator.generate(chunk, n)
            chunk.copyInto(buffer, written, 0, n)
            written += n
        }
        return buffer
    }

    @Test
    fun `white noise sits at the target level`() {
        val measured = rms(render(NoiseType.White))
        report("blanco, RMS", "%.2f dBFS".format(db(measured)))
        // Within 0.3 dB of -18 dBFS. Tight, because the level is computed from the
        // distribution rather than measured and corrected.
        assertEquals(db(TARGET_RMS.toDouble()), db(measured), 0.3)
    }

    @Test
    fun `brown noise sits at the same level as white`() {
        val white = rms(render(NoiseType.White))
        val brown = rms(render(NoiseType.Brown))
        report("marrón, RMS", "%.2f dBFS".format(db(brown)))
        report("diferencia marrón - blanco", "%.2f dB".format(db(brown) - db(white)))
        // Equal RMS is the contract the makeup gain is derived to satisfy. Matching
        // *loudness* is a separate matter and gets calibrated by ear in H3: the ear
        // is less sensitive down where brown noise lives.
        assertEquals(db(white), db(brown), 1.0)
    }

    @Test
    fun `neither generator drifts away from zero`() {
        for (type in NoiseType.entries) {
            val offset = abs(mean(render(type)))
            report("$type, desviación de continua", "%.2e".format(offset))
            // DC is inaudible as sound and audible as lost headroom. The brown
            // generator has a DC blocker precisely so this holds for it too.
            assertTrue("$type has a DC offset of $offset", offset < 0.001)
        }
    }

    @Test
    fun `nothing clips`() {
        for (type in NoiseType.entries) {
            val loudest = peak(render(type))
            report("$type, pico", "%.4f (%.2f dBFS)".format(loudest, db(loudest.toDouble())))
            assertTrue("$type peaks at $loudest", loudest < 1.0f)
        }
    }

    @Test
    fun `brown noise breathes and white noise does not`() {
        // This replaced a test that asserted brown noise has the higher crest
        // factor. It measured white 4.9 and brown 4.2, and the premise was simply
        // wrong: filtered Gaussian noise is still Gaussian, so the instantaneous
        // peak-to-RMS ratio is much the same for both.
        //
        // What genuinely differs is the *short-term* level. Brown noise has its
        // energy in the low frequencies, so its loudness drifts over tens of
        // milliseconds — it breathes — while white noise is steady. That drift is
        // why the DC blocker and the limiter exist, and it is measurable.
        report("blanco, fluctuación en 50 ms", "%.1f %%".format(envelopeVariation(NoiseType.White) * 100))
        report("marrón, fluctuación en 50 ms", "%.1f %%".format(envelopeVariation(NoiseType.Brown) * 100))
        assertTrue(
            "white varies by ${envelopeVariation(NoiseType.White)}, " +
                "brown by ${envelopeVariation(NoiseType.Brown)}",
            envelopeVariation(NoiseType.Brown) > envelopeVariation(NoiseType.White) * 3
        )
    }

    /**
     * Relative spread of the level measured in 50 ms windows: how much the loudness
     * wanders over time, independent of the overall level.
     */
    private fun envelopeVariation(type: NoiseType): Double {
        val signal = render(type)
        val window = SAMPLE_RATE / 20
        val levels = (0 until signal.size / window).map { block ->
            rms(signal.copyOfRange(block * window, (block + 1) * window))
        }
        val average = levels.average()
        val spread = kotlin.math.sqrt(levels.sumOf { (it - average) * (it - average) } / levels.size)
        return spread / average
    }

    @Test
    fun `the limiter barely works, which is the price of the extra six decibels`() {
        // Raising the level from -18 to -12 dBFS means Gaussian peaks now reach the
        // ceiling sometimes. The arithmetic says about thirty samples a second, and
        // thirty soft-limited peaks a second cannot be heard — but "cannot be heard"
        // is a claim worth measuring rather than repeating.
        for (type in NoiseType.entries) {
            val signal = render(type)
            val limited = signal.count { abs(it) > 0.85f }
            val perSecond = limited.toDouble() / (signal.size.toDouble() / SAMPLE_RATE)
            report("$type, muestras limitadas", "%.0f por segundo".format(perSecond))
            assertTrue(
                "$type limits $perSecond samples a second, which would be audible",
                perSecond < 200
            )
        }
    }

    @Test
    fun `a generator is reproducible after reset`() {
        val generator = createGenerator(NoiseType.Brown)
        val first = FloatArray(4096)
        val second = FloatArray(4096)
        generator.generate(first)
        generator.reset()
        generator.generate(second)
        assertTrue("reset did not restore the initial state", first.contentEquals(second))
    }

    @Test
    fun `the stream continues across buffer boundaries instead of restarting`() {
        // If a generator restarted per call, the app would emit the same block over
        // and over — a loop, which is exactly what synthesising the noise avoids.
        val generator = createGenerator(NoiseType.White)
        val first = FloatArray(2048)
        val second = FloatArray(2048)
        generator.generate(first)
        generator.generate(second)
        assertTrue("two consecutive buffers are identical", !first.contentEquals(second))
    }
}

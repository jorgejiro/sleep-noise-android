package com.jjrapps.sleepnoise.audio

import com.jjrapps.sleepnoise.domain.model.NoiseType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Changing sound is the one thing the user does while listening, so it is the one
 * transition that must not be audible as anything but the sounds themselves.
 */
class CrossfadeTest {

    private val frames = 256
    private val crossfadeMs = 800
    private val fadeFrames = crossfadeMs * SAMPLE_RATE / 1000

    private fun render(source: StereoNoiseSource, totalFrames: Int): FloatArray {
        val out = FloatArray(totalFrames * 2)
        val block = FloatArray(frames * 2)
        var done = 0
        while (done < totalFrames) {
            val n = minOf(frames, totalFrames - done)
            source.generate(block, n)
            block.copyInto(out, done * 2, 0, n * 2)
            done += n
        }
        return out
    }

    @Test
    fun `the level holds steady through the change instead of dipping`() {
        val source = StereoNoiseSource(NoiseType.Brown, crossfadeMillis = crossfadeMs)
        val before = rms(render(source, SAMPLE_RATE))
        source.crossfadeTo(NoiseType.White)
        val during = rms(render(source, fadeFrames))
        val after = rms(render(source, SAMPLE_RATE))

        // Compared against the level before and after, not as a spread between
        // windows: brown noise's own loudness wanders 24 % over 50 ms windows (see
        // NoiseLevelTest), so a window spread measures that, not the crossfade. The
        // first version of this test did exactly that and failed on the sound rather
        // than on the transition.
        //
        // What is worth asserting: linear weights would sag about 3 dB through the
        // middle of every change, because the two sounds are uncorrelated and it is
        // their powers that add, not their amplitudes. Equal-power weights hold it.
        assertEquals("level during the change", db(before), db(during), 1.0)
        assertEquals("level after the change", db(before), db(after), 1.0)
    }

    @Test
    fun `the change completes and the new sound is the one left playing`() {
        val source = StereoNoiseSource(NoiseType.Brown, crossfadeMillis = crossfadeMs)
        source.crossfadeTo(NoiseType.White)
        assertEquals(NoiseType.White, source.pendingType)
        render(source, fadeFrames + frames)
        assertEquals(NoiseType.White, source.type)
        assertNull("the fade never finished", source.pendingType)
    }

    @Test
    fun `after the change the spectrum is the new sound's, not a mixture`() {
        val source = StereoNoiseSource(NoiseType.Brown, crossfadeMillis = crossfadeMs)
        source.crossfadeTo(NoiseType.White)
        render(source, fadeFrames + frames)

        val settled = render(source, 240_000)
        val left = FloatArray(settled.size / 2) { settled[it * 2] }
        val slope = SpectrumAnalysis.slopeDbPerOctave(
            SpectrumAnalysis.powerSpectrumDb(left, 4096),
            SAMPLE_RATE, 4096, fromHz = 100.0, toHz = 10_000.0
        )
        // Flat: any brown left in the mix would pull this negative.
        assertEquals("slope after the change was $slope dB/octave", 0.0, slope, 0.5)
    }

    @Test
    fun `both channels fade at the same speed`() {
        // The fade counter is shared by the two channels, so advancing it inside the
        // per-channel loop would make the second channel fade at twice the speed and
        // pull the stereo image sideways through every change.
        //
        // Measured as spectral slope per channel, not level per channel. Comparing
        // the RMS of two independent brown-noise channels over a tenth of a second
        // measures their own wandering — the first version of this test did that and
        // failed at 3,7 dB apart, which was the sound and not the fade. The mix ratio
        // shows up in the *shape*: a channel further along the fade is measurably
        // flatter, and the slope is an average over thousands of bins, so it is
        // steady where an instantaneous level is not.
        val source = StereoNoiseSource(NoiseType.Brown, crossfadeMillis = crossfadeMs)
        render(source, SAMPLE_RATE)
        source.crossfadeTo(NoiseType.White)
        render(source, fadeFrames / 2)              // stop halfway through
        val half = render(source, 120_000)          // and look at the mix there

        val slopes = listOf(0, 1).map { channel ->
            val samples = FloatArray(half.size / 2) { half[it * 2 + channel] }
            SpectrumAnalysis.slopeDbPerOctave(
                SpectrumAnalysis.powerSpectrumDb(samples, 4096),
                SAMPLE_RATE, 4096, fromHz = 100.0, toHz = 10_000.0
            )
        }
        assertEquals("left ${slopes[0]}, right ${slopes[1]}", slopes[0], slopes[1], 0.6)
    }

    @Test
    fun `turning back mid-change does not jump`() {
        val source = StereoNoiseSource(NoiseType.Brown, crossfadeMillis = crossfadeMs)
        val steady = rms(render(source, SAMPLE_RATE))
        source.crossfadeTo(NoiseType.White)
        render(source, fadeFrames / 3)
        // Someone tapping back and forth: the mix has to keep moving from where it
        // is, not snap.
        source.crossfadeTo(NoiseType.Brown)
        val after = rms(render(source, fadeFrames))

        // Same reasoning as above: measured against the steady level, not as a
        // spread between windows.
        assertEquals("level after turning back", db(steady), db(after), 1.5)
        render(source, fadeFrames)
        assertEquals(NoiseType.Brown, source.type)
    }

    @Test
    fun `asking for the sound already playing changes nothing`() {
        val source = StereoNoiseSource(NoiseType.Brown, crossfadeMillis = crossfadeMs)
        source.crossfadeTo(NoiseType.Brown)
        assertNull(source.pendingType)
        assertEquals(NoiseType.Brown, source.type)
    }

    @Test
    fun `nothing clips while two sounds are mixed`() {
        val source = StereoNoiseSource(NoiseType.Brown, crossfadeMillis = crossfadeMs)
        source.crossfadeTo(NoiseType.White)
        val during = render(source, fadeFrames)
        // Two signals at -18 dBFS summing could reach -12 with the wrong weights.
        // Equal power keeps the total where it was, and this is the guard.
        assertTrue("peak reached ${peak(during)}", peak(during) < 1.0f)
        // No DC assertion here on purpose. The crossfade is 800 ms, and brown noise's
        // DC blocker sits at 5 Hz, so its mean over a window that short is legitimately
        // a few thousandths — it only averages to zero over seconds. The first version
        // of this test asserted 0,001 over 0,8 s and failed on arithmetic that was
        // correct. DC is checked where it can be checked, over ten seconds, in
        // NoiseLevelTest.
    }
}

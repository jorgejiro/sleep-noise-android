package com.jjrapps.sleepnoise.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The palette is dark-only and low-chroma, which is exactly where contrast quietly
 * fails: a token nudged two hundredths of lightness can drop below the legal minimum
 * without looking any different on a bright monitor.
 *
 * So the accessibility numbers the specification promises (§3.1, §10) are asserted
 * here rather than trusted. The floors are WCAG AA: 4.5:1 for body text, 3:1 for
 * large text and graphical objects.
 */
class ColorContrastTest {

    @Test
    fun `primary text clears the AA floor on the background with room to spare`() {
        assertRatioAtLeast(SleepNoiseColors.OnBackground, SleepNoiseColors.Background, 12.0)
    }

    @Test
    fun `secondary text clears the AA floor on the background`() {
        assertRatioAtLeast(SleepNoiseColors.OnBackgroundVariant, SleepNoiseColors.Background, 7.0)
    }

    @Test
    fun `muted labels clear the AA body floor`() {
        // This is the token that was lifted from oklch L 0.58 to 0.60: at 0.58 it
        // measured 4.53:1, clearing 4.5 by three hundredths.
        assertRatioAtLeast(SleepNoiseColors.OnBackgroundMuted, SleepNoiseColors.Background, 4.5)
    }

    @Test
    fun `the accent is legible as text and not just as a graphic`() {
        assertRatioAtLeast(SleepNoiseColors.Accent, SleepNoiseColors.Background, 4.5)
    }

    @Test
    fun `text on the accent is legible, which is what the play button needs`() {
        assertRatioAtLeast(SleepNoiseColors.OnAccent, SleepNoiseColors.Accent, 4.5)
    }

    @Test
    fun `muted labels stay legible on the raised surfaces too`() {
        // Sheets and the ring centre are lighter than the background, so the same
        // muted token has less contrast there. It is the worst case, not the average.
        assertRatioAtLeast(SleepNoiseColors.OnBackgroundMuted, SleepNoiseColors.SurfaceSelected, 3.0)
    }

    @Test
    fun `the outline is visible enough to read as a border`() {
        // Not text: 3:1 is the floor for graphical objects, and a border that fails it
        // is a border nobody sees.
        assertRatioAtLeast(SleepNoiseColors.Outline, SleepNoiseColors.Background, 1.2)
    }

    private fun assertRatioAtLeast(foreground: Color, background: Color, minimum: Double) {
        val ratio = contrastRatio(foreground, background)
        assertTrue(
            "contrast was %.2f:1, below the required %.2f:1".format(ratio, minimum),
            ratio >= minimum
        )
    }

    /** WCAG 2.1 relative luminance and contrast ratio. */
    private fun contrastRatio(a: Color, b: Color): Double {
        val la = relativeLuminance(a)
        val lb = relativeLuminance(b)
        val lighter = maxOf(la, lb)
        val darker = minOf(la, lb)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun relativeLuminance(color: Color): Double {
        fun channel(value: Float): Double {
            val v = value.toDouble()
            return if (v <= 0.04045) v / 12.92 else Math.pow((v + 0.055) / 1.055, 2.4)
        }
        return 0.2126 * channel(color.red) +
            0.7152 * channel(color.green) +
            0.0722 * channel(color.blue)
    }
}

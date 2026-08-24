package com.jjrapps.sleepnoise.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The palette of the "Noche profunda" direction: one dark warm ground and one
 * saturated accent, nothing else.
 *
 * The specification defines these in oklch (`docs/especificacion-release-1.0.md`
 * §3.1); the hex values below are that conversion, computed rather than eyeballed.
 * Each comment keeps the oklch source so a future tweak starts from the intent and
 * not from the result.
 */
object SleepNoiseColors {
    /** oklch(0.155 0.014 62) — every screen sits on this. */
    val Background = Color(0xFF110B06)

    /** oklch(0.185 0.012 62) — cards and controls at rest. */
    val Surface = Color(0xFF17120D)

    /** oklch(0.215 0.018 62) — the centre of the ring, modal sheets. */
    val SurfaceRaised = Color(0xFF201811)

    /** oklch(0.235 0.022 62) — the selected control. */
    val SurfaceSelected = Color(0xFF261C13)

    /** oklch(0.26 0.012 62) — borders and dividers. */
    val Outline = Color(0xFF28231E)

    /** The single saturated colour in the app: volume ring, active icons, values. */
    val Accent = Color(0xFFE8A860)

    /** Dark enough to read on top of [Accent]. */
    val OnAccent = Color(0xFF20140A)

    /** oklch(0.94 0.008 62) — primary text. 16.4:1 on [Background]. */
    val OnBackground = Color(0xFFEFEAE6)

    /** oklch(0.80 0.012 62) — secondary text. 10.4:1 on [Background]. */
    val OnBackgroundVariant = Color(0xFFC4BCB6)

    /**
     * oklch(0.60 0.018 62) — labels and units. 4.9:1 on [Background].
     *
     * Lifted from the 0.58 first drafted: that landed at 4.53:1, which clears
     * WCAG AA by three hundredths and would break on the next nudge.
     */
    val OnBackgroundMuted = Color(0xFF887E76)
}

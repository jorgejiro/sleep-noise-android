package com.jjrapps.sleepnoise.audio

import kotlin.math.abs
import kotlin.math.tanh

/**
 * A smooth ceiling.
 *
 * Below [THRESHOLD] the signal passes untouched, so ordinary material is not
 * compressed at all; above it, `tanh` bends the curve towards 1 with a continuous
 * derivative, which is what keeps the limiter inaudible where hard clipping would
 * buzz.
 *
 * It matters more since the level went up: Gaussian peaks have no upper bound, and
 * at -12 dBFS RMS a sample reaches full scale roughly thirty times a second. Thirty
 * hard clips a second is a texture you can hear; thirty soft ones are not.
 */
internal fun softLimit(value: Float): Float {
    val magnitude = abs(value)
    if (magnitude <= THRESHOLD) return value
    val excess = (magnitude - THRESHOLD) / (1f - THRESHOLD)
    val limited = THRESHOLD + (1f - THRESHOLD) * tanh(excess)
    return if (value < 0f) -limited else limited
}

private const val THRESHOLD = 0.85f

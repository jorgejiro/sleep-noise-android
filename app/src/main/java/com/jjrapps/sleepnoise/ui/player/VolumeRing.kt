package com.jjrapps.sleepnoise.ui.player

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.jjrapps.sleepnoise.ui.theme.SleepNoiseColors
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * The volume ring: the one dominant element of the screen, and a control.
 *
 * Dragging anywhere on the ring sets the volume from the angle, starting at the top
 * and going clockwise. It is a shortcut, not the only way in — the plain slider
 * underneath is the guaranteed path, because a control you have to discover cannot
 * be the only way to do something (ADR 002).
 *
 * To TalkBack it is a progress bar with adjust actions, not a drawing: the gesture
 * is unusable with a screen reader, so the semantics carry the value and the
 * increment instead.
 */
@Composable
fun VolumeRing(
    volume: Int,
    onVolumeChange: (Int) -> Unit,
    accessibilityLabel: String,
    diameter: Dp = 264.dp,
    strokeWidth: Dp = 5.6.dp,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val stroke = with(density) { strokeWidth.toPx() }
    val touchTolerance = with(density) { 56.dp.toPx() }

    fun volumeFromOffset(offset: Offset, size: Size): Int? {
        val centre = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f - stroke / 2f
        val distance = hypot(offset.x - centre.x, offset.y - centre.y)
        // Only the ring itself, not the button in the middle: the centre is play.
        if (distance < radius - touchTolerance) return null
        // atan2 measured from twelve o'clock, clockwise, so the value grows the way
        // the ring fills.
        val angle = atan2(offset.x - centre.x, centre.y - offset.y)
        val turns = (angle / (2 * Math.PI)).toFloat()
        val fraction = if (turns < 0f) turns + 1f else turns
        return (fraction * 100f).roundToInt().coerceIn(0, 100)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(diameter)
            .semantics {
                contentDescription = accessibilityLabel
                progressBarRangeInfo = ProgressBarRangeInfo(volume.toFloat(), 0f..100f, 100)
                setProgress { target ->
                    onVolumeChange(target.roundToInt().coerceIn(0, 100))
                    true
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    volumeFromOffset(change.position, size.toSize())?.let(onVolumeChange)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { position ->
                    volumeFromOffset(position, size.toSize())?.let(onVolumeChange)
                }
            }
            .drawBehind {
                val radius = size.minDimension / 2f - stroke / 2f
                val topLeft = Offset(
                    (size.width - radius * 2f) / 2f,
                    (size.height - radius * 2f) / 2f
                )
                val arcSize = Size(radius * 2f, radius * 2f)

                // The breathing halo, drawn as a radial gradient behind everything.
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SleepNoiseColors.Accent.copy(alpha = 0.16f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.minDimension * 0.78f
                    ),
                    radius = size.minDimension * 0.78f
                )
                drawArc(
                    color = SleepNoiseColors.Outline,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke)
                )
                if (volume > 0) {
                    drawArc(
                        color = SleepNoiseColors.Accent,
                        startAngle = -90f,
                        sweepAngle = 360f * volume / 100f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
            }
    ) {
        content()
    }
}

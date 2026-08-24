package com.jjrapps.sleepnoise.ui.common

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.jjrapps.sleepnoise.R
import com.jjrapps.sleepnoise.domain.model.NoiseType

/**
 * How each sound is named and drawn.
 *
 * In one place rather than in every screen: with two sounds an inline `if` was
 * tolerable, with four it becomes four chances to forget one. `when` over an enum
 * also means the compiler catches the fifth sound the day it appears.
 */
@StringRes
fun NoiseType.nameRes(): Int = when (this) {
    NoiseType.White -> R.string.sound_white_name
    NoiseType.Pink -> R.string.sound_pink_name
    NoiseType.Brown -> R.string.sound_brown_name
    NoiseType.Masking -> R.string.sound_masking_name
}

@StringRes
fun NoiseType.shortNameRes(): Int = when (this) {
    NoiseType.White -> R.string.sound_white_short
    NoiseType.Pink -> R.string.sound_pink_short
    NoiseType.Brown -> R.string.sound_brown_short
    NoiseType.Masking -> R.string.sound_masking_short
}

/** Each icon draws the shape of its own spectrum: flat, sloping, or a plateau. */
@DrawableRes
fun NoiseType.iconRes(): Int = when (this) {
    NoiseType.White -> R.drawable.ic_noise_white
    NoiseType.Pink -> R.drawable.ic_noise_pink
    NoiseType.Brown -> R.drawable.ic_noise_brown
    NoiseType.Masking -> R.drawable.ic_noise_masking
}

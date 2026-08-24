package com.jjrapps.sleepnoise.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jjrapps.sleepnoise.R

/**
 * Sora, bundled rather than downloaded: the app declares no `INTERNET` permission,
 * so a downloadable font would simply never arrive.
 *
 * One variable font file covers every weight — `minSdk` 31 is well past the API 26
 * where font variation settings start working, so four static files would only add
 * bytes to the APK.
 */
private fun soraWeight(weight: Int) = Font(
    resId = R.font.sora,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight))
)

val SoraFontFamily = FontFamily(
    soraWeight(200),
    soraWeight(300),
    soraWeight(400),
    soraWeight(600)
)

/**
 * Only the roles the app actually uses. Sizes in sp so they scale with the system
 * setting up to 200%: the screens are built to grow, not to clip.
 */
val SleepNoiseTypography = Typography(
    // The sound name, the one piece of display type in the app.
    displaySmall = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.W200,
        fontSize = 34.sp,
        letterSpacing = (-0.5).sp
    ),
    // Screen headers.
    titleLarge = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.W400,
        fontSize = 22.sp
    ),
    // Settings rows, control labels.
    bodyLarge = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.W300,
        fontSize = 15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp
    ),
    // Units and uppercase labels; the tracking is what makes them read as labels.
    labelMedium = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.W300,
        fontSize = 12.sp,
        letterSpacing = 1.2.sp
    ),
    // The wordmark.
    labelSmall = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.W400,
        fontSize = 11.sp,
        letterSpacing = 2.4.sp
    )
)

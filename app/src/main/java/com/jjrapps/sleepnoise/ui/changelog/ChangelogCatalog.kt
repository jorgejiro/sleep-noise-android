package com.jjrapps.sleepnoise.ui.changelog

import androidx.annotation.ArrayRes
import com.jjrapps.sleepnoise.R
import java.time.LocalDate

/**
 * A released version. The highlights live in a localised string array, so the
 * changelog is translated like any other visible text.
 */
data class ChangelogRelease(
    val versionName: String,
    val versionCode: Int,
    val releaseDate: LocalDate,
    @param:ArrayRes val highlightsRes: Int
)

/**
 * Every release, newest first.
 *
 * When the version in `build.gradle.kts` goes up, three things change together: an
 * entry here, the matching `string-array` in both languages, and `CHANGELOG.md`.
 * They say the same thing to three different readers — this one is for someone who
 * already has the app.
 */
object ChangelogCatalog {
    val releases: List<ChangelogRelease> = listOf(
        ChangelogRelease(
            versionName = "1.0.1",
            versionCode = 2,
            releaseDate = LocalDate.of(2026, 8, 27),
            highlightsRes = R.array.changelog_1_0_1
        ),
        ChangelogRelease(
            versionName = "1.0",
            versionCode = 1,
            releaseDate = LocalDate.of(2026, 8, 24),
            highlightsRes = R.array.changelog_1_0
        )
    )
}

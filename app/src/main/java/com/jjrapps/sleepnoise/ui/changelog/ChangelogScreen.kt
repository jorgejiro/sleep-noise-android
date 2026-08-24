package com.jjrapps.sleepnoise.ui.changelog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jjrapps.sleepnoise.R
import com.jjrapps.sleepnoise.ui.common.ScreenHeader
import com.jjrapps.sleepnoise.ui.theme.SleepNoiseColors
import com.jjrapps.sleepnoise.ui.theme.SleepNoiseTheme
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun ChangelogScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        ScreenHeader(
            title = stringResource(R.string.changelog_title),
            onBack = onBack,
            modifier = Modifier.padding(end = 24.dp, top = 8.dp)
        )
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(26.dp)
        ) {
            items(ChangelogCatalog.releases, key = { it.versionCode }) { release ->
                ReleaseCard(release)
            }
        }
    }
}

@Composable
private fun ReleaseCard(release: ChangelogRelease) {
    // The locale comes from the configuration, so a language change in Settings
    // reformats the date too rather than leaving it in the previous language.
    val locale = LocalConfiguration.current.locales[0]
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale)

    Column {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text(
                text = release.versionName,
                style = MaterialTheme.typography.titleLarge,
                color = SleepNoiseColors.OnBackground
            )
            Spacer(Modifier.size(10.dp))
            Text(
                text = release.releaseDate.format(formatter),
                style = MaterialTheme.typography.labelMedium,
                color = SleepNoiseColors.OnBackgroundMuted
            )
        }
        Spacer(Modifier.height(12.dp))
        stringArrayResource(release.highlightsRes).forEach { highlight ->
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                Spacer(
                    Modifier
                        .padding(top = 8.dp, end = 12.dp)
                        .size(5.dp)
                        .background(SleepNoiseColors.Accent, CircleShape)
                )
                Text(
                    text = highlight,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SleepNoiseColors.OnBackgroundVariant
                )
            }
        }
    }
}

@Preview
@Composable
private fun ChangelogPreview() {
    SleepNoiseTheme { ChangelogScreen(onBack = {}) }
}

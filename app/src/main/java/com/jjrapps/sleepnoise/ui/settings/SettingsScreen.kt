package com.jjrapps.sleepnoise.ui.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jjrapps.sleepnoise.BuildConfig
import com.jjrapps.sleepnoise.R
import com.jjrapps.sleepnoise.domain.model.PlaybackPreferences
import com.jjrapps.sleepnoise.ui.common.ScreenHeader
import com.jjrapps.sleepnoise.ui.theme.SleepNoiseColors
import com.jjrapps.sleepnoise.ui.theme.SleepNoiseTheme
import timber.log.Timber

/**
 * Where the feedback goes. A constant rather than a string resource: it is the
 * author's address, the same in every language, and nothing about it is
 * translatable. The row does not print it either — whoever taps it is about to read
 * it in the To: field of their own mail app, and keeping it off the screen keeps it
 * out of screenshots.
 */
private const val FEEDBACK_EMAIL = "jjrmobileapps@gmail.com"

private val LANGUAGES = listOf(
    PlaybackPreferences.LANGUAGE_AUTO to R.string.settings_language_auto,
    "en" to R.string.settings_language_en,
    "es" to R.string.settings_language_es
)

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenChangelog: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        ScreenHeader(
            title = stringResource(R.string.settings_title),
            onBack = onBack,
            modifier = Modifier.padding(end = 24.dp, top = 8.dp)
        )
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Section(stringResource(R.string.settings_section_playback)) {
                    SwitchRow(
                        label = stringResource(R.string.settings_autoplay),
                        subtitle = stringResource(R.string.settings_autoplay_subtitle),
                        checked = preferences.autoplayOnOpen,
                        onCheckedChange = viewModel::setAutoplay
                    )
                }
            }
            item {
                Section(stringResource(R.string.settings_section_about)) {
                    ActionRow(
                        label = stringResource(R.string.settings_language),
                        value = stringResource(
                            LANGUAGES.first { it.first == preferences.language }.second
                        ),
                        onClick = { showLanguageDialog = true }
                    )
                    RowDivider()
                    InfoRow(
                        label = stringResource(R.string.settings_version),
                        value = stringResource(
                            R.string.settings_version_format,
                            BuildConfig.VERSION_NAME,
                            BuildConfig.VERSION_CODE
                        )
                    )
                    RowDivider()
                    ActionRow(
                        label = stringResource(R.string.changelog_title),
                        value = "",
                        onClick = onOpenChangelog
                    )
                    RowDivider()
                    // Resolved with stringResource and not context.getString: only
                    // the first reacts to a configuration change, and with a language
                    // switch inside the app that is the difference between a subject
                    // line in the chosen language and one in the previous language.
                    val feedbackSubject = stringResource(
                        R.string.feedback_subject,
                        stringResource(R.string.app_name),
                        BuildConfig.VERSION_NAME,
                        BuildConfig.VERSION_CODE
                    )
                    ActionRow(
                        label = stringResource(R.string.settings_feedback),
                        value = "",
                        onClick = {
                            val subject = feedbackSubject
                            // ACTION_SENDTO with a mailto: URI so only mail apps
                            // answer, not the whole share sheet. The subject goes in
                            // the URI *and* in EXTRA_SUBJECT: Gmail reads the URI and
                            // ignores the extra, other clients do the opposite, and
                            // sending both is what makes it land everywhere.
                            val mailto = "mailto:$FEEDBACK_EMAIL?subject=" +
                                android.net.Uri.encode(subject)
                            val intent = Intent(Intent.ACTION_SENDTO, mailto.toUri())
                                .putExtra(Intent.EXTRA_SUBJECT, subject)
                            runCatching { context.startActivity(intent) }
                                .onFailure { Timber.w(it, "no mail app to send feedback with") }
                        }
                    )
                }
            }
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            containerColor = SleepNoiseColors.SurfaceRaised,
            title = {
                Text(
                    stringResource(R.string.settings_language),
                    style = MaterialTheme.typography.titleLarge,
                    color = SleepNoiseColors.OnBackground
                )
            },
            text = {
                Column {
                    LANGUAGES.forEach { (code, labelRes) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clickable {
                                    viewModel.setLanguage(code)
                                    showLanguageDialog = false
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(labelRes),
                                style = MaterialTheme.typography.bodyLarge,
                                color = SleepNoiseColors.OnBackground
                            )
                            if (preferences.language == code) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check),
                                    contentDescription = null,
                                    tint = SleepNoiseColors.Accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(
                        stringResource(R.string.action_close),
                        color = SleepNoiseColors.Accent
                    )
                }
            }
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = SleepNoiseColors.OnBackgroundMuted,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SleepNoiseColors.Surface, MaterialTheme.shapes.medium)
        ) { content() }
    }
}

@Composable
private fun RowDivider() =
    HorizontalDivider(thickness = 0.5.dp, color = SleepNoiseColors.Outline)

@Composable
private fun ActionRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = SleepNoiseColors.OnBackground
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SleepNoiseColors.OnBackgroundMuted
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = SleepNoiseColors.OnBackgroundMuted,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(16.dp)
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = SleepNoiseColors.OnBackground
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = SleepNoiseColors.OnBackgroundMuted
        )
    }
}

@Composable
private fun SwitchRow(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.padding(end = 16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = SleepNoiseColors.OnBackground
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = SleepNoiseColors.OnBackgroundMuted
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SleepNoiseColors.OnAccent,
                checkedTrackColor = SleepNoiseColors.Accent,
                uncheckedTrackColor = SleepNoiseColors.Surface,
                uncheckedBorderColor = SleepNoiseColors.Outline
            )
        )
    }
}

@Preview
@Composable
private fun SettingsPreview() {
    SleepNoiseTheme { SettingsScreen(onBack = {}, onOpenChangelog = {}) }
}

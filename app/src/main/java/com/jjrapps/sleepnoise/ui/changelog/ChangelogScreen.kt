package com.jjrapps.sleepnoise.ui.changelog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jjrapps.sleepnoise.R
import com.jjrapps.sleepnoise.ui.common.ScreenHeader
import com.jjrapps.sleepnoise.ui.theme.SleepNoiseTheme

/**
 * Release notes. The catalogue and its localised entries arrive in H7; the screen is
 * here now so the route out of Settings exists.
 */
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
    }
}

@Preview
@Composable
private fun ChangelogScreenPreview() {
    SleepNoiseTheme {
        ChangelogScreen(onBack = {})
    }
}

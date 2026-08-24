package com.jjrapps.sleepnoise

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.jjrapps.sleepnoise.domain.model.PlaybackPreferences
import com.jjrapps.sleepnoise.domain.repository.PlaybackPreferencesRepository
import com.jjrapps.sleepnoise.ui.navigation.SleepNoiseNavGraph
import com.jjrapps.sleepnoise.ui.theme.SleepNoiseTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * [AppCompatActivity] and not ComponentActivity: the in-app language switch goes
 * through `AppCompatDelegate.setApplicationLocales`, which needs AppCompat to work
 * below API 33. See `CLAUDE.md` §5.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var preferences: PlaybackPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        // Before setContent: on targetSdk 36 the window is edge to edge whether we
        // ask or not, so we may as well be the ones deciding.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        applyStoredLanguage()
        setContent {
            SleepNoiseTheme {
                SleepNoiseNavGraph()
            }
        }
    }

    /**
     * Re-applies the language chosen in Settings.
     *
     * On API 33 and up the system remembers it and this is a no-op. Below that,
     * AppCompat's copy does not survive a process death, so it has to be restored
     * from DataStore. Applied only when it differs from what is already in force:
     * `setApplicationLocales` recreates the activity, so applying it unconditionally
     * would loop.
     */
    private fun applyStoredLanguage() {
        lifecycleScope.launch {
            val stored = preferences.preferences.first().language
            val wanted = if (stored == PlaybackPreferences.LANGUAGE_AUTO) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(stored)
            }
            if (AppCompatDelegate.getApplicationLocales() != wanted) {
                AppCompatDelegate.setApplicationLocales(wanted)
            }
        }
    }
}

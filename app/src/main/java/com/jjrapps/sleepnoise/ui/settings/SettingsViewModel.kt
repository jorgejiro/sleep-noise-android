package com.jjrapps.sleepnoise.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jjrapps.sleepnoise.domain.model.PlaybackPreferences
import com.jjrapps.sleepnoise.domain.repository.PlaybackPreferencesRepository
import com.jjrapps.sleepnoise.playback.PlaybackConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: PlaybackPreferencesRepository,
    private val playback: PlaybackConnection
) : ViewModel() {

    val preferences: StateFlow<PlaybackPreferences> = repository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlaybackPreferences())

    fun setAutoplay(enabled: Boolean) = viewModelScope.launch {
        repository.setAutoplayOnOpen(enabled)
    }

    /**
     * Applies the language immediately and remembers it.
     *
     * `setApplicationLocales` is the platform's own per-app language on API 33 and
     * up, where the system stores the choice itself. Below that AppCompat emulates
     * it, and the copy in DataStore is what survives a restart — which is why the
     * preference is written as well as applied.
     */
    fun setLanguage(language: String) = viewModelScope.launch {
        repository.setLanguage(language)
        AppCompatDelegate.setApplicationLocales(
            if (language == PlaybackPreferences.LANGUAGE_AUTO) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(language)
            }
        )
        // El servicio no se recrea con el cambio de idioma, así que hay que decírselo:
        // si no, su notificación se queda en el idioma anterior.
        playback.refreshLabels()
    }
}

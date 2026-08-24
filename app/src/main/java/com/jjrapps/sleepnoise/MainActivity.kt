package com.jjrapps.sleepnoise

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.jjrapps.sleepnoise.ui.navigation.SleepNoiseNavGraph
import com.jjrapps.sleepnoise.ui.theme.SleepNoiseTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * [AppCompatActivity] and not ComponentActivity: the in-app language switch goes
 * through `AppCompatDelegate.setApplicationLocales`, which needs AppCompat to work
 * below API 33. See `CLAUDE.md` §5.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Before setContent: on targetSdk 36 the window is edge to edge whether we
        // ask or not, so we may as well be the ones deciding.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            SleepNoiseTheme {
                SleepNoiseNavGraph()
            }
        }
    }
}

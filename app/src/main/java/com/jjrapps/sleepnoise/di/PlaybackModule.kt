package com.jjrapps.sleepnoise.di

import android.app.NotificationManager
import android.content.Context
import com.jjrapps.sleepnoise.data.datastore.PlaybackPreferencesDataSource
import com.jjrapps.sleepnoise.data.repository.PlaybackPreferencesRepositoryImpl
import com.jjrapps.sleepnoise.domain.repository.PlaybackPreferencesRepository
import com.jjrapps.sleepnoise.playback.DoNotDisturbController
import com.jjrapps.sleepnoise.playback.PlaybackConnection
import com.jjrapps.sleepnoise.playback.SystemZenGateway
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlaybackModule {

    /**
     * One connection for the whole app. Scoped to the application and not to a
     * screen: connecting to a media session is asynchronous, and a per-screen
     * controller would make every navigation wait for a handshake.
     */
    @Provides
    @Singleton
    fun providePlaybackConnection(@ApplicationContext context: Context): PlaybackConnection =
        PlaybackConnection(context)

    /**
     * One holder of Do Not Disturb for the whole app, because there is one phone to
     * silence. Scoped to the application and not to the service so that a service
     * restarted mid-session cannot end up believing nobody was holding it.
     */
    @Provides
    @Singleton
    fun provideDoNotDisturbController(
        @ApplicationContext context: Context
    ): DoNotDisturbController = DoNotDisturbController(
        SystemZenGateway(context.getSystemService(NotificationManager::class.java))
    )

    @Provides
    @Singleton
    fun providePreferencesRepository(
        @ApplicationContext context: Context
    ): PlaybackPreferencesRepository =
        PlaybackPreferencesRepositoryImpl(PlaybackPreferencesDataSource(context))
}

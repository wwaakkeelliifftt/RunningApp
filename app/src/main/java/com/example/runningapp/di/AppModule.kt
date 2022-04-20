package com.example.runningapp.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.runningapp.data.local.RunDao
import com.example.runningapp.data.local.RunningDatabase
import com.example.runningapp.data.repository.MainRepository
import com.example.runningapp.data.repository.MainRepositoryImpl
import com.example.runningapp.util.Constants
import com.example.runningapp.util.Constants.RUNNING_DB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Provides
    @Singleton
    fun provideRunningDatabase(@ApplicationContext app: Context): RunningDatabase {
        return Room.databaseBuilder(
            app,
            RunningDatabase::class.java,
            RUNNING_DB
        ).build()
    }

    @Provides
    @Singleton
    fun provideRunDao(db: RunningDatabase): RunDao = db.dao

    @Provides
    @Singleton
    fun provideMainRepository(dao: RunDao): MainRepository {
        return MainRepositoryImpl(dao = dao)
    }

    @Singleton
    @Provides
    fun provideSharedPreference(@ApplicationContext app: Context): SharedPreferences {
        return app.getSharedPreferences(
            Constants.SHARED_PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
    }

    @Singleton
    @Provides
    fun provideName(sharedPref: SharedPreferences): String =
        sharedPref.getString(Constants.KEY_NAME, "_") ?: "%username"

    @Singleton
    @Provides
    fun provideWeight(sharedPref: SharedPreferences): Float =
        sharedPref.getFloat(Constants.KEY_WEIGHT, 6.66f)

    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPref: SharedPreferences): Boolean =
        sharedPref.getBoolean(Constants.KEY_FIRST_TIME_TOGGLE, true)


}


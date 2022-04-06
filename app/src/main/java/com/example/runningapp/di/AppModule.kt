package com.example.runningapp.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.runningapp.data.local.RunDao
import com.example.runningapp.data.local.RunningDatabase
import com.example.runningapp.data.repository.MainRepository
import com.example.runningapp.data.repository.MainRepositoryImpl
import com.example.runningapp.util.Constants.RUNNING_DB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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



}
package com.example.runningapp.data.repository

import androidx.lifecycle.LiveData
import com.example.runningapp.data.local.entity.Run

interface MainRepository {

    suspend fun insertRun(run: Run)

    suspend fun deleteRun(run: Run)

    suspend fun getAllRunsSortedBy(column: String): List<Run>

    fun getTotalTimeInMills(): LiveData<Long>

    fun getTotalAverageSpeed(): LiveData<Float>

    fun getTotalDistance(): LiveData<Int>

    fun getTotalCalories(): LiveData<Int>

}
package com.example.runningapp.data.repository

import androidx.lifecycle.LiveData
import com.example.runningapp.data.local.RunDao
import com.example.runningapp.data.local.entity.Run
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(
    private val dao: RunDao
) : MainRepository {

    override suspend fun insertRun(run: Run): Unit = dao.insertRun(run = run)
    override suspend fun deleteRun(run: Run): Unit = dao.deleteRun(run = run)

    override fun getTotalDistance(): LiveData<Int> = dao.getTotalDistance()
    override fun getTotalCalories(): LiveData<Int> = dao.getTotalCalories()
    override fun getTotalTimeInMills(): LiveData<Long> = dao.getTotalTimeInMills()
    override fun getTotalAverageSpeed(): LiveData<Float> = dao.getTotalAverageSpeed()

    override suspend fun getAllRunsSortedBy(column: String): List<Run> {
        return dao.getAllRunsSortedBy(column = column)
    }

}


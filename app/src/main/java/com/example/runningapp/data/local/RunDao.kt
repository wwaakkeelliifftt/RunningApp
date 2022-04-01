package com.example.runningapp.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.runningapp.data.local.entity.Run
import kotlinx.coroutines.Deferred

@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query(
        """
            SELECT * FROM running_table
            ORDER BY
            CASE WHEN :column = 'timestamp' THEN timestamp END DESC,
            CASE WHEN :column = 'speed' THEN averageSpeedInKmH END DESC,
            CASE WHEN :column = 'calories' THEN caloriesBurned END DESC,
            CASE WHEN :column = 'mills' THEN timeInMills END DESC,
            CASE WHEN :column = 'distance' THEN distanceInMeter END DESC
            """
    )
    fun getAllRunsSortedBy(column: String): LiveData<List<Run>>

    @Query("SELECT SUM(timeInMills) FROM running_table")
    fun getTotalTimeInMills(): LiveData<Long>

    @Query("SELECT AVG(averageSpeedInKmH) FROM running_table")
    fun getTotalAverageSpeed(): LiveData<Float>

    @Query("SELECT SUM(distanceInMeter) FROM running_table")
    fun getTotalDistance(): LiveData<Int>

    @Query("SELECT SUM(caloriesBurned) FROM running_table")
    fun getTotalCalories(): LiveData<Int>

}
package com.example.runningapp.ui.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningapp.data.local.entity.Run
import com.example.runningapp.data.repository.MainRepository
import com.example.runningapp.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {

    val totalTimeRun = repository.getTotalTimeInMills()
    val totalDistance = repository.getTotalDistance()
    val totalAvgSpeed = repository.getTotalAverageSpeed()
    val totalCaloriesBurned = repository.getTotalCalories()
    val runsSortedByDate = MutableLiveData<List<Run>>()

    private fun getRuns() {
        viewModelScope.launch {
            val result = repository.getAllRunsSortedBy(Constants.SORTED_BY_DATE)
            runsSortedByDate.postValue(result)
        }
    }

    init {
        getRuns()
    }



}
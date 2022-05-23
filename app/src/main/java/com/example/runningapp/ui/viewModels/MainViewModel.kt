package com.example.runningapp.ui.viewModels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.example.runningapp.R
import com.example.runningapp.data.local.entity.Run
import com.example.runningapp.data.repository.MainRepository
import com.example.runningapp.util.Constants
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.FieldPosition
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.N)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository
): ViewModel() {

    private val _sortType: MutableLiveData<SortType> = MutableLiveData()
    val sortType: LiveData<SortType> get() = _sortType

    private val _queryRuns = MutableLiveData<List<Run>>()
    val queryRuns: LiveData<List<Run>> get() = _queryRuns

    init {
        changeSortType(SortType.Date)
    }

    fun changeSortType(sortType: SortType) {
        this._sortType.value = sortType
        viewModelScope.launch {
            val result = when (sortType) {
                SortType.Date -> repository.getAllRunsSortedBy(Constants.SORTED_BY_DATE)
                SortType.RunningTime -> repository.getAllRunsSortedBy(Constants.SORTED_BY_TIME_IN_MILLIS)
                SortType.Distance -> repository.getAllRunsSortedBy(Constants.SORTED_BY_DISTANCE)
                SortType.AvgSpeed -> repository.getAllRunsSortedBy(Constants.SORTED_BY_SPEED)
                SortType.CaloriesBurned -> repository.getAllRunsSortedBy(Constants.SORTED_BY_CALORIES)
            }
            _queryRuns.postValue(result)
        }
    }

    fun insertRun(run: Run) {
        viewModelScope.launch {
            repository.insertRun(run)
        }
    }

    fun deleteRun(position: Int) {
        viewModelScope.launch {
            _queryRuns.value?.get(position)?.let { run ->
                repository.deleteRun(run)
                Timber.d("Run №${run.id} -- ${run.distanceInMeter}km --> was clear from db")
            }
        }
        changeSortType(sortType.value!!)
    }

    fun getTotalDistance(): LiveData<Int> = repository.getTotalDistance()
    fun getTotalCalories(): LiveData<Int> = repository.getTotalCalories()
    fun getTotalTimeInMills(): LiveData<Long> = repository.getTotalTimeInMills()
    fun getTotalAverageSpeed(): LiveData<Float> = repository.getTotalAverageSpeed()

}

sealed class SortType {
    object Date: SortType()
    object RunningTime: SortType()
    object AvgSpeed: SortType()
    object Distance: SortType()
    object CaloriesBurned: SortType()
}


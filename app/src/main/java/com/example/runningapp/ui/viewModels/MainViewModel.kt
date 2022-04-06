package com.example.runningapp.ui.viewModels

import androidx.lifecycle.ViewModel
import com.example.runningapp.data.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository
): ViewModel() {
    

}
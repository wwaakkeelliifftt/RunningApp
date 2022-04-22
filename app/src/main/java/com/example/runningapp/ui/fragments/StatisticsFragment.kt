package com.example.runningapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.runningapp.R
import com.example.runningapp.databinding.FragmentStatisticsBinding
import com.example.runningapp.ui.other.CustomMarkerView
import com.example.runningapp.ui.viewModels.StatisticsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragment: Fragment() {

    private val viewModel: StatisticsViewModel by viewModels()

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStatisticsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToObservers()
        setupBarChart()
    }

    private fun setupBarChart() {
        binding.barChart.apply {
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawLabels(false)
                axisLineColor = Color.WHITE
                textColor = Color.WHITE
                setDrawGridLines(false)
            }
            axisLeft.apply {
                axisLineColor = Color.WHITE
                textColor = Color.WHITE
                setDrawGridLines(false)
            }
            axisRight.apply {
                axisLineColor = Color.WHITE
                textColor = Color.WHITE
                setDrawGridLines(false)
            }

            description.text = "Average Speed Over Time"
            legend.isEnabled = false
        }
    }

    private fun subscribeToObservers() {
        viewModel.apply {
            totalTimeRun.observe(viewLifecycleOwner) {
                it?.let {
                    val calendar = Calendar.getInstance().apply { timeInMillis = it }
                    val dateFormat = SimpleDateFormat("HH:mm:ss")
                    val totalTimeString = dateFormat.format(calendar.time)
                    binding.tvTotalTime.text = totalTimeString
                }
            }
            totalDistance.observe(viewLifecycleOwner) {
                it?.let {
                    val km = it / 1000
                    val totalDistance = round(km * 10f) / 10f
                    val totalDistanceString = "$totalDistance km"
                    binding.tvTotalDistance.text = totalDistanceString
                }
            }
            totalAvgSpeed.observe(viewLifecycleOwner) {
                it?.let {
                    val avgSpeed = round(it * 10f) / 10f
                    val avgSpeedString = "$avgSpeed km/h"
                    binding.tvAverageSpeed.text = avgSpeedString
                }
            }
            totalCaloriesBurned.observe(viewLifecycleOwner) {
                it?.let {
                    val totalCalories = "$it kcal"
                    binding.tvTotalCalories.text = totalCalories
                }
            }
            runsSortedByDate.observe(viewLifecycleOwner) {
                it?.let {
                    val allAvgSpeed = it.indices.map { i ->
                        BarEntry(i.toFloat(), it[i].averageSpeedInKmH)
                    }
                    val barDataSet = BarDataSet(allAvgSpeed, "Avg Speed Over Time").apply {
                        valueTextColor = Color.WHITE
                        color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
                    }
                    binding.barChart.data = BarData(barDataSet)
                    /** bind Custom Marker View */
                    binding.barChart.marker = CustomMarkerView(
                        it.reversed(),
                        R.layout.marker_view,
                        requireContext()
                    )
                    binding.barChart.invalidate()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
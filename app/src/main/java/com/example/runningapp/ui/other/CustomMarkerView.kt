package com.example.runningapp.ui.other

import android.content.Context
import android.view.LayoutInflater
import com.example.runningapp.data.local.entity.Run
import com.example.runningapp.databinding.MarkerViewBinding
import com.example.runningapp.util.TrackingUtility
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView(
    val runs: List<Run>,
    layoutId: Int,
    context: Context
) : MarkerView(context, layoutId) {

    private val layoutInflater = LayoutInflater.from(context)
    private var binding = MarkerViewBinding.inflate(layoutInflater, this, false)

    init {
        addView(binding.root)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
//        this.binding = MarkerViewBinding.bind(this)
    }

    /** specified position where we show our custom view */
    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())

    }

    override fun refreshContent(barEntry: Entry?, highlight: Highlight?) {
        super.refreshContent(barEntry, highlight)

        if (barEntry == null) {
            return
        }
        val currentRunId = barEntry.x.toInt()
        val run = runs[currentRunId]

        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timestamp
        }
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        binding.tvDate.text = dateFormat.format(calendar.time)

        val avgSpeed = "${run.averageSpeedInKmH}km/h"
        binding.tvAvgSpeed.text = avgSpeed

        val distanceInKm = "${run.distanceInMeter / 1000f}km"
        binding.tvDistance.text = distanceInKm

        binding.tvDuration.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMills)

        val caloriesBurned = "${run.caloriesBurned}kcal"
        binding.tvCaloriesBurned.text = caloriesBurned
    }

}

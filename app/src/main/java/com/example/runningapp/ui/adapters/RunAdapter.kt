package com.example.runningapp.ui.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runningapp.R
import com.example.runningapp.data.local.entity.Run
import com.example.runningapp.databinding.ItemRunBinding
import com.example.runningapp.util.TrackingUtility
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    inner class RunViewHolder(val itemBinding: ItemRunBinding): RecyclerView.ViewHolder(itemBinding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }
    private val listDiffer = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Run>) = listDiffer.submitList(list).also {
        Timber.d("TRY to update adapter with $list")
    }

    override fun getItemCount(): Int = listDiffer.currentList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(
            ItemRunBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = listDiffer.currentList[position]

        with(holder) {
            itemView.apply {
                Glide.with(this)
                    .load(run.img)
                    .centerCrop()
                    .into(holder.itemBinding.ivRunImage)

            }
            itemBinding.apply {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = run.timestamp
                }
                val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
                tvDate.text = dateFormat.format(calendar.time)

                val avgSpeed = "${run.averageSpeedInKmH}km/h"
                tvAvgSpeed.text = avgSpeed

                val distanceInKm = "${run.distanceInMeter / 1000f}km"
                tvDistance.text = distanceInKm

                tvTime.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMills)

                val caloriesBurned = "${run.caloriesBurned}kcal"
                tvCalories.text = caloriesBurned
            }
        }
    }

}

/** example from stackoverflow */
//class ViewBindingVH constructor(val binding: ViewBinding) :
//    RecyclerView.ViewHolder(binding.root) {
//
//    companion object {
//        inline fun create(
//            parent: ViewGroup,
//            crossinline block: (inflater: LayoutInflater, container: ViewGroup, attach: Boolean) -> ViewBinding
//        ) = ViewBindingVH(block(LayoutInflater.from(parent.context), parent, false))
//    }
//}
//
//
//class CardAdapter : RecyclerView.Adapter<ViewBindingVH>() {
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewBindingVH {
//        return ViewBindingVH.create(parent, CardBinding::inflate)
//    }
//
//    override fun onBindViewHolder(holder: ViewBindingVH, position: Int) {
//        (holder.binding as CardBinding).apply {
//            //bind model to view
//            title.text = "some text"
//            descripiton.text = "some text"
//        }
//    }
//
//}
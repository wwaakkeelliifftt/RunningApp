package com.example.runningapp.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.example.runningapp.R
import com.example.runningapp.data.local.entity.Run
import com.example.runningapp.databinding.FragmentTrackingBinding
import com.example.runningapp.services.Polyline
import com.example.runningapp.services.TrackingService
import com.example.runningapp.ui.viewModels.MainViewModel
import com.example.runningapp.util.Constants
import com.example.runningapp.util.TrackingUtility
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment: Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private var map: GoogleMap? = null

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()

    private var currentTimeInMillis = 0L
    private var timeWithMillis = false
    /** default value for, later we fix it with actual user input parameters */
    private var weight = 66f

    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!

    private var menu: Menu? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        _binding = FragmentTrackingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.apply {
            onCreate(savedInstanceState)
            getMapAsync {
                map = it

                /** called once for case with rotate device and recreate fragment */
                addAllPolylines()
            }
        }
        timeWithMillis = savedInstanceState?.getBoolean(Constants.SHOW_MILLIS_STATE) ?: true

        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }
        binding.btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDatabase()
        }
        binding.chbMillis.setOnClickListener {
            timeWithMillis = !timeWithMillis
        }

        /** observe livedata state from service in fragment */
        subscribeToObservers()
    }

    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(Constants.POLYLINE_COLOR)
                .width(Constants.POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)

            map?.addPolyline(polylineOptions)
        }
    }

    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(Constants.POLYLINE_COLOR)
                .width(Constants.POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    Constants.MAP_ZOOM
                )
            )
        }
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints) {
            for (position in polyline) {
                bounds.include(position)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()
            )
        )
    }

    @SuppressLint("NewApi")
    private fun endRunAndSaveToDatabase() {
        map?.snapshot { btm ->
            var distanceInMeters = 0
            for (polyline in pathPoints) {
                distanceInMeters = TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            val averageSpeed = round(
                (distanceInMeters / 1000f) / (currentTimeInMillis / 1000f / 60 / 60)
                        * 10) / 10f
            val dateStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()

            val run = Run(
                img = btm,
                timestamp = dateStamp,
                averageSpeedInKmH = averageSpeed,
                distanceInMeter = distanceInMeters,
                timeInMills = currentTimeInMillis,
                caloriesBurned = caloriesBurned
            )
            Timber.d("RUN SAVED. id = ${run.id}, avgSpeed = ${run.averageSpeedInKmH}")
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run saved successfully",
                Snackbar.LENGTH_LONG
            ).show()

            stopRun()
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking) {
            binding.btnToggleRun.text = "Start"
            binding.btnFinishRun.visibility = View.VISIBLE
        } else {
            binding.btnToggleRun.text = "Stop"
            binding.btnFinishRun.visibility = View.GONE
            menu?.get(0)?.isVisible = true
        }
    }

    private fun toggleRun() {
        if (isTracking) {
            menu?.get(0)?.isVisible = true
            sendCommandToService(Constants.ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(Constants.ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })
        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            currentTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(currentTimeInMillis, timeWithMillis)
            binding.tvTimer.text = formattedTime
        })
    }


    private fun sendCommandToService(action: String): Intent {
        return Intent(requireContext(), TrackingService::class.java).also { service ->
            service.action = action
            requireContext().startService(service)
        }
    }


    /** save state of visibility timer "00:00:00" or "00:00:00:00" */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
        outState.putBoolean(Constants.SHOW_MILLIS_STATE, timeWithMillis)
    }

    /** below -> option menu setup  */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (currentTimeInMillis > 0L) {
            this.menu?.get(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miCancelTracking -> showCancelTrackingDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancelTrackingDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel the run?")
            .setMessage("Are you sure to cancel the current run and delete all it's data?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes") { _, _ ->
                stopRun()
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
        dialog.show()
    }

    private fun stopRun() {
        sendCommandToService(Constants.ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }


    /** below -> lifecycle bindings for "MapView" */
    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    /** probably @mapView already destroy at this moment and need null-check action ?? */
    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
        _binding = null
        map = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

}

package com.example.runningapp.ui.fragments

import android.Manifest
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runningapp.R
import com.example.runningapp.databinding.FragmentRunBinding
import com.example.runningapp.ui.adapters.RunAdapter
import com.example.runningapp.ui.adapters.onItemClick
import com.example.runningapp.ui.adapters.onLongItemClick
import com.example.runningapp.ui.adapters.setOnItemClickListener
import com.example.runningapp.ui.viewModels.MainViewModel
import com.example.runningapp.ui.viewModels.SortType
import com.example.runningapp.util.Constants
import com.example.runningapp.util.TrackingUtility
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

@AndroidEntryPoint
class RunFragment: Fragment(), EasyPermissions.PermissionCallbacks {

    private val viewModel: MainViewModel by viewModels()

    private var _binding: FragmentRunBinding? = null
    private val binding get() = _binding!!
    private lateinit var runAdapter: RunAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRunBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("NewApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissions()
        setupRecyclerView()
        setupFab()
        setupSpinner()

        viewModel.queryRuns.observe(viewLifecycleOwner) {
            runAdapter.submitList(it)
            binding.rvRuns.scheduleLayoutAnimation()
        }

    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }
    }

    private fun setupRecyclerView() = binding.rvRuns.apply {
        runAdapter = RunAdapter()
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())

        /** hide FAB when scroll down */
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy < 0 || dy > 0  && binding.fab.isShown) {
                    binding.fab.hide()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    binding.fab.show()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })

        onLongItemClick {
            animateLongCLick(binding.root)
        }

    }

    private fun animateLongCLick(view: View) {
        ValueAnimator.ofObject(
            ArgbEvaluator(),
            resources.getColor(R.color.colorAccent),
            resources.getColor(R.color.md_blue_700)
        ).apply {
            duration = 1000L
            addUpdateListener { valueAnimator ->
                view.setBackgroundColor(valueAnimator.animatedValue as Int)
            }
            start()
        }
    }

    @SuppressLint("NewApi")
    private fun setupSpinner() {
        // setSelection from string-array "filter_options" at R.values.arrays.xml
        Timber.d("initial Spinner Sort.Type is -> ${viewModel.sortType.value.toString()}")
        when (viewModel.sortType.value) {
            SortType.Date -> binding.spFilter.setSelection(0)
            SortType.RunningTime -> binding.spFilter.setSelection(1)
            SortType.Distance -> binding.spFilter.setSelection(2)
            SortType.AvgSpeed -> binding.spFilter.setSelection(3)
            SortType.CaloriesBurned -> binding.spFilter.setSelection(4)
            else -> binding.spFilter.setSelection(2)
        }

        binding.spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> viewModel.changeSortType(SortType.Date)
                    1 -> viewModel.changeSortType(SortType.RunningTime)
                    2 -> viewModel.changeSortType(SortType.Distance)
                    3 -> viewModel.changeSortType(SortType.AvgSpeed)
                    4 -> viewModel.changeSortType(SortType.CaloriesBurned)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }


    private fun requestPermissions() {
        if (TrackingUtility.hasLocationPermission(requireContext())) {
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need accept location permission to use this app.",
                Constants.REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need accept location permission to use this app.",
                Constants.REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) { }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this)
                .build()
                .show()
        } else {
            requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            this
        )
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


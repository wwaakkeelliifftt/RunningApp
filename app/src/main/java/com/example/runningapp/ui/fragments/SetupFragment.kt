package com.example.runningapp.ui.fragments

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.runningapp.R
import com.example.runningapp.databinding.FragmentSetupBinding
import com.example.runningapp.ui.viewModels.MainViewModel
import com.example.runningapp.util.Constants
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class SetupFragment: Fragment() {

    @Inject lateinit var sharedPref: SharedPreferences
//    @set:Inject var isFirstAppOpen = true
    var isFirstAppOpen: Boolean = true

    private var _binding: FragmentSetupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSetupBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isFirstAppOpen = sharedPref.getBoolean(Constants.KEY_FIRST_TIME_TOGGLE, true)
        if (!isFirstAppOpen) {
            // remove SetupFragment from back-stack
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()

            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment,
                savedInstanceState,
                navOptions = navOptions
            )
        }

//        Navigation.findNavController(requireView())
//            .popBackStack(R.id.settingsFragment, true)

        binding.tvContinue.setOnClickListener {
            val success = writePersonalDataToSharedPref()
            binding.apply {
                if (success) {
                    findNavController().navigate(R.id.action_setupFragment_to_runFragment)
                } else {
                    /** axyenna! */
                    animateErrorInput(root)

                    Snackbar.make(
                        requireView(), //.findViewById(R.id.rootView),
                        "Please enter all the fields",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun writePersonalDataToSharedPref(): Boolean {
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()
        if (name.isEmpty() || weight.isEmpty()) {
            return false
        }
        sharedPref
            .edit()
            .putString(Constants.KEY_NAME, name)
            .putFloat(Constants.KEY_WEIGHT, weight.toFloat())
            .putBoolean(Constants.KEY_FIRST_TIME_TOGGLE, false)
            // .apply() - async / .commit() - sync
            .apply()

        val toolbarText = "Let's go $name!"
        requireActivity().findViewById<TextView>(R.id.tvToolbarTitle).text = toolbarText
        return true
    }

    /** red-color splash if input false */
    private fun animateErrorInput(view: View) {
        val colorAnimation = ValueAnimator.ofObject(
            ArgbEvaluator(),
            resources.getColor(R.color.errorColor),
            resources.getColor(R.color.md_blue_700)
        ).apply {
            duration = 1000L
            addUpdateListener { valueAnimator ->
                view.setBackgroundColor(valueAnimator.animatedValue as Int)
            }
            start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
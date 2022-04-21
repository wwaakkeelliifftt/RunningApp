package com.example.runningapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.runningapp.R
import com.example.runningapp.databinding.FragmentSettingsBinding
import com.example.runningapp.util.Constants
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class SettingsFragment: Fragment() {

    @Inject lateinit var sharedPref: SharedPreferences
    private var name = "default"
    private var weight = 19.37f

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        name = sharedPref.getString(Constants.KEY_NAME, "def_from_sharedPref") ?: "kosyak"
        weight = sharedPref.getFloat(Constants.KEY_WEIGHT, 18.2f)

        binding.apply {
            tilName.hint = name
            tilWeight.hint = weight.toString()

            btnResetChanges.setOnClickListener {
                 resetChangesAndGoToSetupScreen(savedInstanceState)
            }

            btnApplyChanges.setOnClickListener {
                val success = applyChangesToSharedPreference()
                if (success) {
                    Snackbar.make(requireView(), "New changes saved success!", Snackbar.LENGTH_LONG).show()
                } else {
                    Snackbar.make(requireView(), "Fill out all fields to apply changes", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun resetChangesAndGoToSetupScreen(savedInstanceState: Bundle?) {
        sharedPref.edit()
            .remove(Constants.KEY_NAME)
            .remove(Constants.KEY_WEIGHT)
            .remove(Constants.KEY_FIRST_TIME_TOGGLE)
            .putBoolean(Constants.KEY_FIRST_TIME_TOGGLE, true)
            .apply()

        requireActivity().findViewById<TextView>(R.id.tvToolbarTitle).text = "Reset & Go"
        findNavController().navigate(R.id.action_settingsFragment_to_setupFragment)
    }

    private fun applyChangesToSharedPreference(): Boolean {
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()
        return if (name.isEmpty() || weight.isEmpty()) {
            false
        } else {
            sharedPref.edit()
                .putString(Constants.KEY_NAME, name)
                .putFloat(Constants.KEY_WEIGHT, weight.toFloat())
                .putBoolean(Constants.KEY_FIRST_TIME_TOGGLE, false)
                // .apply() - async / .commit() - sync
                .apply()
            this.name = name
            this.weight = weight.toFloat()
            requireActivity().findViewById<TextView>(R.id.tvToolbarTitle).text = "Running App, go $name!"
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
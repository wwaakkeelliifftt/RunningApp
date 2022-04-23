package com.example.runningapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.runningapp.R
import com.example.runningapp.databinding.ActivityMainBinding
import com.example.runningapp.util.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        /** in case when activity was destroyed. if nof -> override onNewIntent() */
        navigateToTrackingFragmentByPendingIntent(intent = intent)

        val navController = getNavControllerToNavContainer()
        binding.bottomNavigationView.setupWithNavController(navController)

        /** No-Op. Use for old version of androidx.navigation. Already deprecated and works fine without this */
//        binding.bottomNavigationView.setOnNavigationItemReselectedListener { /* No-Op */ }

        navController.addOnDestinationChangedListener { _controller, destination, _args ->
                when (destination.id) {
                    R.id.settingsFragment,
                    R.id.runFragment,
                    R.id.statisticsFragment -> {
                        binding.bottomNavigationView.visibility = View.VISIBLE
                    }
                    else -> binding.bottomNavigationView.visibility = View.GONE
                }
            }

    }

    /**     this method needed, because we have bug with invocation on
     *      "binding.nav_host_fragment_container.findNavController()"
     *      and app will crash at runtime
     **/
    private fun getNavControllerToNavContainer(): NavController {
        return (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container)
                as NavHostFragment)
            .findNavController()
    }

    /** launch tracking fragment by click on notification banner */
    private fun navigateToTrackingFragmentByPendingIntent(intent: Intent?) {
        if (intent?.action == Constants.ACTION_SHOW_TRACKING_FRAGMENT) {
            getNavControllerToNavContainer()
                .navigate(R.id.action_global_tracking_fragment)
        }
    }

    /** case when activity wasn't destroy */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentByPendingIntent(intent = intent)
    }

}

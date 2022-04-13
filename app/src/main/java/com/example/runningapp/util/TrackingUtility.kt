package com.example.runningapp.util

import android.Manifest
import android.content.Context
import android.os.Build
import pub.devrel.easypermissions.EasyPermissions
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.concurrent.TimeUnit

object TrackingUtility {

    fun hasLocationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false): String {
        var milliseconds = ms
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)

        return if (!includeMillis) {
            val f: NumberFormat = DecimalFormat("00")
            "${f.format(hours)}:${f.format(minutes)}:${f.format(seconds)}"
        } else {
            milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
            milliseconds /= 10
            "${if (hours < 10) "0" else ""}$hours:" +
                    "${if (minutes < 10) "0" else ""}$minutes:" +
                    "${if (seconds < 10) "0" else ""}$seconds:" +
                    "${if (milliseconds < 10) "0" else ""}$milliseconds"
        }
    }

}
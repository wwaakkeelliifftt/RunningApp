package com.example.runningapp.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.runningapp.util.Constants
import com.example.runningapp.util.TrackingUtility
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import com.example.runningapp.R

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    @Inject lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    @Inject lateinit var baseNotificationBuilder: NotificationCompat.Builder
    private lateinit var currentNotificationBuilder: NotificationCompat.Builder
    var isFirstRun = true


    private val timeRunInSeconds = MutableLiveData<Long>()                          // <-- private
    private var isTimeEnabled = false
    private var lapTime = 0L                // each onStart/onPause
    private var timeStarted = 0L            // timestamp when we start Run
    private var totalTimeRun = 0L
    private var lastSecondTimestamp = 0L    // increment counter for update @timeRunInSeconds

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
        val timeRunInMillis = MutableLiveData<Long>()                               // <-- public
    }

    private fun initialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()

        initialValues()
        currentNotificationBuilder = baseNotificationBuilder
        isTracking.observe(this, Observer {
            updateLocationTracking(isTracking = it)
            updateNotificationTrackingState(isTracking = it)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                Constants.ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                        Timber.d("Starting service")
                    } else {
                        /** first invoke startTimer() going on inside a startForegroundService() */
                        startTimer()
                        Timber.d("Resuming service..")
                    }
                }
                Constants.ACTION_PAUSE_SERVICE -> {
                    pauseService()
                    Timber.d("Paused service")
                }
                Constants.ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startTimer() {
        addEmptyPolyline()

        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimeEnabled = true

        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                // time difference between now and timeStarted
                lapTime = System.currentTimeMillis() - timeStarted
                // post the new lapTime. equal total time
                timeRunInMillis.postValue(totalTimeRun + lapTime)

                if (timeRunInMillis.value!! >= lastSecondTimestamp + 1000L ) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(Constants.TIMER_UPDATE_INTERVAL)
            }
            totalTimeRun += lapTime
        }
    }


    @SuppressLint("MissingPermission")  // all fine, we check with TrackingUtility func
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermission(this)) {
                val request = LocationRequest.create().apply {
                    interval = Constants.LOCATION_UPDATE_INTERVAL
                    fastestInterval = Constants.FASTEST_LOCATION_INTERVAL
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result.locations.let { listOfLocation ->
                    for (location in listOfLocation) {
                        addPathPoint(location = location)
                        Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(it.latitude, it.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    // strange method result -> Any.. think need to refactor
    private fun addEmptyPolyline(): Any = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun startForegroundService() {
//        addEmptyPolyline() we can remove func from here after implement startTimer() and invoke there
        startTimer()
        isTracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        startForeground(Constants.NOTIFICATION_ID, baseNotificationBuilder.build())
        timeRunInSeconds.observe(this, Observer {
            val notification = currentNotificationBuilder
                .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000))
            notificationManager.notify(Constants.NOTIFICATION_ID, notification.build())
        })
    }

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = PendingIntent.getService(
            this,
            if (isTracking) 1 else 2,
            if (isTracking) {
                Intent(this, TrackingService::class.java).apply {
                    action = Constants.ACTION_PAUSE_SERVICE
                }
            } else {
                Intent(this, TrackingService::class.java).apply {
                    action = Constants.ACTION_START_OR_RESUME_SERVICE
                }
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        /**
         * strange way, but anyway it's work:
         * we need remove previous "action" (in List inside obj) of notification
         * before update notification with new action
         * */
        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        currentNotificationBuilder = baseNotificationBuilder
            .addAction(R.drawable.ic_pause_circle, notificationActionText, pendingIntent)

        notificationManager.notify(Constants.NOTIFICATION_ID, currentNotificationBuilder.build())
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimeEnabled = false
    }

    /**
     *  we use NotificationManager.IMPORTANCE_LOW to avoid notification with sound in our case.
     *  app will send a lot of notification update to the channel and higher level of channel
     *  will trigger with "ring"
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            Constants.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}
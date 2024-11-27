package com.sensordatalabeler.data

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getBroadcast
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.sensordatalabeler.data.db.MyLocationEntity
import com.sensordatalabeler.sensor.LocationUpdateBroadcastReceiver
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Manager class for location.
 *
 * Handles request and data.
 */
class LocationManager private constructor(private val context: Context) {

    private val _receivingLocationUpdates: MutableLiveData<Boolean> = MutableLiveData(false)


    val receivingLocationUpdates: LiveData<Boolean>
        get() = _receivingLocationUpdates

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest: LocationRequest =
        LocationRequest.Builder(TimeUnit.SECONDS.toMillis(intervalMills))
            .setIntervalMillis(TimeUnit.SECONDS.toMillis(intervalMills))
            .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(updateMills))
            .setMaxUpdateDelayMillis(TimeUnit.SECONDS.toMillis(2))
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

    private val locationUpdatePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, LocationUpdateBroadcastReceiver::class.java)
        intent.action = LocationUpdateBroadcastReceiver.ACTION_PROCESS_UPDATES
        getBroadcast(context, 0, intent, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)
    }

    /**
     * Start the location request service.
     *
     * @throws SecurityException If location permission has revoked.
     */
    fun startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates()")

        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            Log.d(TAG, "Permission denied")
            return
        }
        try {
            _receivingLocationUpdates.value = true
            fusedLocationClient.requestLocationUpdates(locationRequest, locationUpdatePendingIntent)
        } catch (permissionRevoked: SecurityException) {
            _receivingLocationUpdates.value = false
            Log.d(TAG, "Location permission revoked; details: $permissionRevoked")
            throw permissionRevoked
        }
        Log.d(TAG, "Location Update running")
    }

    /**
     * Stop the location update service.
     */
    fun stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates()")
        _receivingLocationUpdates.value = false
        fusedLocationClient.removeLocationUpdates(locationUpdatePendingIntent)
    }

    /**
     * Getter for location entity data.
     */
    fun getLocationEntity(): MyLocationEntity {
        Log.d(TAG, "getLocationEntity")
        return locationEntity
    }

    companion object {
        private const val TAG = "LocationManager"
        private const val intervalMills = 60L
        private const val updateMills = 30L
        var locationEntity =  MyLocationEntity(0.0,0.0, Date(0))

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: LocationManager? = null

        fun getInstance(context: Context): LocationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocationManager(context).also { INSTANCE = it}
            }

        }
    }
}

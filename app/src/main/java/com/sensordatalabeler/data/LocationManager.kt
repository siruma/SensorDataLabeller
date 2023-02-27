package com.sensordatalabeler.data

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
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
import java.util.*
import java.util.concurrent.TimeUnit


@SuppressLint("UnspecifiedImmutableFlag")
class LocationManager private constructor(private val context: Context) {

    private val _receivingLocationUpdates: MutableLiveData<Boolean> = MutableLiveData(false)


    val receivingLocationUpdates: LiveData<Boolean>
        get() = _receivingLocationUpdates

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest: LocationRequest =
        LocationRequest.Builder(TimeUnit.SECONDS.toMillis(60))
            .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(30))
            .setMaxUpdateDelayMillis(TimeUnit.SECONDS.toMillis(2))
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

    private val locationUpdatePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, LocationUpdateBroadcastReceiver::class.java)
        intent.action = LocationUpdateBroadcastReceiver.ACTION_PROCESS_UPDATES
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates()")

        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)
            return

        try {
            _receivingLocationUpdates.value = true
            fusedLocationClient.requestLocationUpdates(locationRequest, locationUpdatePendingIntent)
        } catch (permissionRevoked: SecurityException) {
            _receivingLocationUpdates.value = false
            Log.d(TAG, "Location permission revoked; details: $permissionRevoked")
            throw permissionRevoked
        }
    }

    fun stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates()")
        _receivingLocationUpdates.value = false
        fusedLocationClient.removeLocationUpdates(locationUpdatePendingIntent)
    }

    fun getLocationEntity(): MyLocationEntity {
        return locationEntity
    }

    companion object {
        private const val TAG = "LocationManager"
        var locationEntity =  MyLocationEntity(0.0,0.0,Date(0))

        private var INSTANCE: LocationManager? = null

        fun getInstance(context: Context): LocationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocationManager(context).also { INSTANCE = it}
            }

        }
    }
}
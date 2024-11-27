package com.sensordatalabeler.sensor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationResult
import com.sensordatalabeler.data.LocationManager
import com.sensordatalabeler.data.db.MyLocationEntity
import java.util.Date

/**
 * Location Update Broadcast Receiver.
 *
 * Handles updates for location.
 */
class LocationUpdateBroadcastReceiver : BroadcastReceiver() {

    /**
     * onReceive
     *
     * @param context
     * @param intent Used for extract the location data
     */
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive() context:$context, intent:$intent")

        if (intent.action == ACTION_PROCESS_UPDATES) {

            // Checks for location availability changes.

            LocationAvailability.extractLocationAvailability(intent)?.let { locationAvailability ->
                if (!locationAvailability.isLocationAvailable) {
                    Log.d(TAG, "Location services are no longer available!")
                }
            }

            LocationResult.extractResult(intent)?.let { locationResult ->
                val locations = locationResult.locations.map { location ->
                    Log.d(TAG, "Location: $location")
                    MyLocationEntity(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        date = Date(location.time)
                    )
                }
                if (locations.isNotEmpty()) {
                    //TODO LocationRepository
                    Log.d(TAG, "Location: ${locations.last().getString()}")

                    LocationManager.locationEntity = locations.last()
                }
            }
        }
    }

    companion object {
        private const val TAG = "LUBroadcastReceiver"
        private const val PACKAGE_NAME = "com.sensordatalabeler.sensor"
        const val ACTION_PROCESS_UPDATES = "$PACKAGE_NAME.action.PROCESS_UPDATES"
    }
}

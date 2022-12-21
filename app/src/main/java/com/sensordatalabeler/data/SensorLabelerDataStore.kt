package com.sensordatalabeler.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class SensorLabelerDataStore(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = SENSOR_LABELER_DATASTORE_NAME
    )

    val activeSensorLabelerFlow: Flow<Boolean> = context.dataStore.data.map {
        it[ACTIVE_SENSOR_LABELER_KEY] ?: false
    }

    suspend fun setActiveSensorLabeler(activeSensorLabeler: Boolean) {
        context.dataStore.edit {
            it[ACTIVE_SENSOR_LABELER_KEY] = activeSensorLabeler
        }
    }
    val timeStampSensorFlow:Flow<Int> = context.dataStore.data.map {
        it[TIME_STAMP_POINT_KEY] ?:0
    }

    val heartRateSensorFlow: Flow<Int> = context.dataStore.data.map {
        it[HEARD_BEAT_POINTS_KEY] ?: 0
    }

    suspend fun setTimeStampSensor(timeStamp: Int) {
        context.dataStore.edit {
            it[TIME_STAMP_POINT_KEY] = timeStamp
        }
    }

    suspend fun setHeartRateSensor(measurement: Int){
        context.dataStore.edit {
            it[HEARD_BEAT_POINTS_KEY] = measurement
        }
    }

    companion object {
        private const val SENSOR_LABELER_DATASTORE_NAME = "sensor_labeler_datastore"

        private val TIME_STAMP_POINT_KEY = intPreferencesKey("time_stamp_point")
        private val HEARD_BEAT_POINTS_KEY = intPreferencesKey("heard_beat_points")
        private val ACTIVE_SENSOR_LABELER_KEY = booleanPreferencesKey("active_sensor_labeler")
    }
}
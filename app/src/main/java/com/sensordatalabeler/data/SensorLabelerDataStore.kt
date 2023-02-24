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
        it[HEART_BEAT_POINTS_KEY] ?: 0
    }
    val gyroRateSensorFlow: Flow<Int> = context.dataStore.data.map {
        it[GYRO_RATE_POINTS_KEY] ?: 0
    }
    val accelerationRateSensorFlow: Flow<Int> = context.dataStore.data.map {
        it[ACCELERATION_RATE_POINTS_KEY] ?: 0
    }

    suspend fun setTimeStampSensor(timeStamp: Int) {
        context.dataStore.edit {
            it[TIME_STAMP_POINT_KEY] = timeStamp
        }
    }

    suspend fun setHeartRateSensor(measurement: Int){
        context.dataStore.edit {
            it[HEART_BEAT_POINTS_KEY] = measurement
        }
    }
    suspend fun setGyroRateSensor(measurement: Int){
        context.dataStore.edit {
            it[GYRO_RATE_POINTS_KEY] = measurement
        }
    }

    suspend fun setAccelerationRateSensor(measurementRate: Int) {
        context.dataStore.edit {
            it[ACCELERATION_RATE_POINTS_KEY] = measurementRate
        }

    }

    companion object {
        private const val SENSOR_LABELER_DATASTORE_NAME = "sensor_labeler_datastore"

        private val TIME_STAMP_POINT_KEY = intPreferencesKey("time_stamp_point")
        private val HEART_BEAT_POINTS_KEY = intPreferencesKey("heart_beat_points")
        private val ACTIVE_SENSOR_LABELER_KEY = booleanPreferencesKey("active_sensor_labeler")
        private val GYRO_RATE_POINTS_KEY = intPreferencesKey("gyro_rate_points")
        private val ACCELERATION_RATE_POINTS_KEY = intPreferencesKey("acceleration_rate_points")
    }
}
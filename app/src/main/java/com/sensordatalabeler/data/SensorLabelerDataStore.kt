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

    // TIME STAMP
    val timeStampSensorFlow:Flow<Int> = context.dataStore.data.map {
        it[TIME_STAMP_POINT_KEY] ?:0
    }

    // HEART RATE
    val heartRateSensorFlow: Flow<Int> = context.dataStore.data.map {
        it[HEART_BEAT_POINTS_KEY] ?: 0
    }

    // GYRO RATE
    val gyroXRateSensorFlow: Flow<Int> = context.dataStore.data.map {
        it[GYRO_X_RATE_POINTS_KEY] ?: 0
    }
    val gyroYRateSensorFlow: Flow<Int> = context.dataStore.data.map {
        it[GYRO_Y_RATE_POINTS_KEY] ?: 0
    }
    val gyroZRateSensorFlow: Flow<Int> = context.dataStore.data.map {
        it[GYRO_Z_RATE_POINTS_KEY] ?: 0
    }

    // ACCELERATION RATE
    val accelerationXRateSensorFlow: Flow<Int> = context.dataStore.data.map {
        it[ACCELERATION_X_RATE_POINTS_KEY] ?: 0
    }
    val accelerationYRateSensorFlow: Flow<Int> = context.dataStore.data.map {
        it[ACCELERATION_Y_RATE_POINTS_KEY] ?: 0
    }
    val accelerationZRateSensorFlow: Flow<Int> = context.dataStore.data.map {
        it[ACCELERATION_Z_RATE_POINTS_KEY] ?: 0
    }

    // SET FUNCTIONS

    // ACTIVE SENSOR
    suspend fun setActiveSensorLabeler(activeSensorLabeler: Boolean) {
        context.dataStore.edit {
            it[ACTIVE_SENSOR_LABELER_KEY] = activeSensorLabeler
        }
    }

    // TIME STAMP
    suspend fun setTimeStampSensor(timeStamp: Int) {
        context.dataStore.edit {
            it[TIME_STAMP_POINT_KEY] = timeStamp
        }
    }

    // HEART RATE
    suspend fun setHeartRateSensor(measurement: Int){
        context.dataStore.edit {
            it[HEART_BEAT_POINTS_KEY] = measurement
        }
    }

    // GYRO RATE
    suspend fun setGyroXRateSensor(measurement: Int){
        context.dataStore.edit {
            it[GYRO_X_RATE_POINTS_KEY] = measurement
        }
    }
    suspend fun setGyroYRateSensor(measurement: Int){
        context.dataStore.edit {
            it[GYRO_Y_RATE_POINTS_KEY] = measurement
        }
    }
    suspend fun setGyroZRateSensor(measurement: Int){
        context.dataStore.edit {
            it[GYRO_Z_RATE_POINTS_KEY] = measurement
        }
    }

    // ACCELERATION RATE
    suspend fun setAccelerationXRateSensor(measurementRate: Int) {
        context.dataStore.edit {
            it[ACCELERATION_X_RATE_POINTS_KEY] = measurementRate
        }
    }
    suspend fun setAccelerationYRateSensor(measurementRate: Int) {
        context.dataStore.edit {
            it[ACCELERATION_Y_RATE_POINTS_KEY] = measurementRate
        }
    }
    suspend fun setAccelerationZRateSensor(measurementRate: Int) {
        context.dataStore.edit {
            it[ACCELERATION_Z_RATE_POINTS_KEY] = measurementRate
        }
    }

    companion object {
        private const val SENSOR_LABELER_DATASTORE_NAME = "sensor_labeler_datastore"

        private val TIME_STAMP_POINT_KEY = intPreferencesKey("time_stamp_point")
        private val HEART_BEAT_POINTS_KEY = intPreferencesKey("heart_beat_points")
        private val ACTIVE_SENSOR_LABELER_KEY = booleanPreferencesKey("active_sensor_labeler")

        // GYRO RATE
        private val GYRO_X_RATE_POINTS_KEY = intPreferencesKey("gyro_x_rate_points")
        private val GYRO_Y_RATE_POINTS_KEY = intPreferencesKey("gyro_y_rate_points")
        private val GYRO_Z_RATE_POINTS_KEY = intPreferencesKey("gyro_z_rate_points")

        // ACCELERATION RATE
        private val ACCELERATION_X_RATE_POINTS_KEY = intPreferencesKey("acceleration_x_rate_points")
        private val ACCELERATION_Y_RATE_POINTS_KEY = intPreferencesKey("acceleration_y_rate_points")
        private val ACCELERATION_Z_RATE_POINTS_KEY = intPreferencesKey("acceleration_z_rate_points")
    }
}
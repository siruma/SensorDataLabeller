package com.sensordatalabeler.data

import android.content.Context
import com.sensordatalabeler.data.db.MyLocationEntity
import kotlinx.coroutines.flow.Flow

class SensorLabelerRepository private constructor(
    private val sensorLabelerDataStore: SensorLabelerDataStore
) {
    val activeSensorLabelerFlow: Flow<Boolean> = sensorLabelerDataStore.activeSensorLabelerFlow

    suspend fun setActiveSensorLabeler(activeLabeler: Boolean) =
        sensorLabelerDataStore.setActiveSensorLabeler(activeLabeler)

    // HEART RATE
    val heartRateSensorFlow: Flow<Int> = sensorLabelerDataStore.heartRateSensorFlow

    suspend fun setHeartRateSensor(measurement: Int) =
        sensorLabelerDataStore.setHeartRateSensor(measurement)

    // TIME STAMP
    val timeStampSensorFlow: Flow<Int> = sensorLabelerDataStore.timeStampSensorFlow

    suspend fun setTimeStampSensor(timeStamp: Int) =
        sensorLabelerDataStore.setTimeStampSensor(timeStamp)

    // GYRO RATE
    val gyroXRateSensorFlow: Flow<Int> = sensorLabelerDataStore.gyroXRateSensorFlow
    val gyroYRateSensorFlow: Flow<Int> = sensorLabelerDataStore.gyroYRateSensorFlow
    val gyroZRateSensorFlow: Flow<Int> = sensorLabelerDataStore.gyroZRateSensorFlow

    suspend fun setGyroXRateSensor(gyroRate: Int) =
        sensorLabelerDataStore.setGyroXRateSensor(gyroRate)

    suspend fun setGyroYRateSensor(gyroRate: Int) =
        sensorLabelerDataStore.setGyroYRateSensor(gyroRate)

    suspend fun setGyroZRateSensor(gyroRate: Int) =
        sensorLabelerDataStore.setGyroZRateSensor(gyroRate)


    // ACCELERATION RATE
    val accelerationXRateSensorFlow: Flow<Int> = sensorLabelerDataStore.accelerationXRateSensorFlow
    val accelerationYRateSensorFlow: Flow<Int> = sensorLabelerDataStore.accelerationYRateSensorFlow
    val accelerationZRateSensorFlow: Flow<Int> = sensorLabelerDataStore.accelerationZRateSensorFlow
    suspend fun setAccelerationXRateSensor(measurementRate: Int) =
        sensorLabelerDataStore.setAccelerationXRateSensor(measurementRate)

    suspend fun setAccelerationYRateSensor(measurementRate: Int) =
        sensorLabelerDataStore.setAccelerationYRateSensor(measurementRate)

    suspend fun setAccelerationZRateSensor(measurementRate: Int) =
        sensorLabelerDataStore.setAccelerationZRateSensor(measurementRate)

    // STEP COUNTER
    val stepCounterSensorFlow: Flow<Int> = sensorLabelerDataStore.stepCounterSensorFlow

    suspend fun setStepCounterSensor(steps: Int) =
        sensorLabelerDataStore.setStepCounterSensor(steps)

    // LOCATION
    val longitudeSensorFlow: Flow<Double> = sensorLabelerDataStore.longitudeSensorFlow
    val latitudeSensorFlow: Flow<Double> = sensorLabelerDataStore.latitudeSensorFlow
    val dateSensorFlow: Flow<Long> = sensorLabelerDataStore.dateSensorFlow
     suspend fun setLocationsSensor(locationEntity: MyLocationEntity) =
        sensorLabelerDataStore.setLocationSensor(locationEntity)

    companion object {
        @Volatile
        private var INSTANCE: SensorLabelerRepository? = null

        fun getInstance(context: Context): SensorLabelerRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SensorLabelerRepository(
                    SensorLabelerDataStore(context)
                )
                    .also { INSTANCE = it }
            }
        }
    }
}
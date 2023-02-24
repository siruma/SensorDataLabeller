package com.sensordatalabeler.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class SensorLabelerRepository private constructor(
    private val sensorLabelerDataStore: SensorLabelerDataStore
) {
    val activeSensorLabelerFlow: Flow<Boolean> = sensorLabelerDataStore.activeSensorLabelerFlow

    suspend fun setActiveSensorLabeler(activeLabeler: Boolean) =
        sensorLabelerDataStore.setActiveSensorLabeler(activeLabeler)

    val heartRateSensorFlow: Flow<Int> = sensorLabelerDataStore.heartRateSensorFlow

    suspend fun setHeartRateSensor(measurement: Int) =
        sensorLabelerDataStore.setHeartRateSensor(measurement)

    val timeStampSensorFlow: Flow<Int> = sensorLabelerDataStore.timeStampSensorFlow

    suspend fun setTimeStampSensor(timeStamp: Int) =
        sensorLabelerDataStore.setTimeStampSensor(timeStamp)

    val gyroRateSensorFlow: Flow<Int> = sensorLabelerDataStore.gyroRateSensorFlow
     suspend fun setGyroRateSensor(gyroRate: Int) =
         sensorLabelerDataStore.setGyroRateSensor(gyroRate)

    val accelerationRateSensorFlow : Flow<Int> = sensorLabelerDataStore.accelerationRateSensorFlow
    suspend fun setAccelerationRateSensor(measurementRate: Int) =
        sensorLabelerDataStore.setAccelerationRateSensor(measurementRate)

    companion object {
        @Volatile private var INSTANCE : SensorLabelerRepository ? = null

        fun getInstance(context: Context): SensorLabelerRepository {
            return INSTANCE ?: synchronized(this){
                INSTANCE ?: SensorLabelerRepository(
                    SensorLabelerDataStore(context))
                    .also { INSTANCE = it }
            }
        }
    }
}
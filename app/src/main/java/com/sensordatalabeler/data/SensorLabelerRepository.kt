package com.sensordatalabeler.data

import android.content.Context
import com.sensordatalabeler.data.db.MyLocationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Sensor labeler repository
 *
 * @param sensorLabelerDataStore
 */
class SensorLabelerRepository private constructor(
    private val sensorLabelerDataStore: SensorLabelerDataStore
) {
    val activeSensorLabelerFlow: Flow<Boolean> = sensorLabelerDataStore.activeSensorLabelerFlow

    // HEART RATE
    val heartRateSensorFlow: Flow<Int> = sensorLabelerDataStore.heartRateSensorFlow

    // TIME STAMP
    val timeStampSensorFlow: Flow<String> = sensorLabelerDataStore.timeStampSensorFlow

    // GYRO RATE
    val gyroXRateSensorFlow: Flow<Int> = sensorLabelerDataStore.gyroXRateSensorFlow
    val gyroYRateSensorFlow: Flow<Int> = sensorLabelerDataStore.gyroYRateSensorFlow
    val gyroZRateSensorFlow: Flow<Int> = sensorLabelerDataStore.gyroZRateSensorFlow

    // ACCELERATION RATE
    val accelerationXRateSensorFlow: Flow<Int> = sensorLabelerDataStore.accelerationXRateSensorFlow
    val accelerationYRateSensorFlow: Flow<Int> = sensorLabelerDataStore.accelerationYRateSensorFlow
    val accelerationZRateSensorFlow: Flow<Int> = sensorLabelerDataStore.accelerationZRateSensorFlow

    // STEP COUNTER
    val stepCounterSensorFlow: Flow<Int> = sensorLabelerDataStore.stepCounterSensorFlow

    // LOCATION
    val longitudeSensorFlow: Flow<Double> = sensorLabelerDataStore.longitudeSensorFlow
    val latitudeSensorFlow: Flow<Double> = sensorLabelerDataStore.latitudeSensorFlow
    val dateSensorFlow: Flow<Long> = sensorLabelerDataStore.dateSensorFlow

    /**
     * Setter for Activity of Sensors.
     *
     * @param activeLabeler True if sensor is active.
     */
    suspend fun setActiveSensorLabeler(activeLabeler: Boolean) =
        sensorLabelerDataStore.setActiveSensorLabeler(activeLabeler)

    /**
     * Setter for Heart Rate sensor.
     *
     * @param measurement value of heart rate.
     */
    suspend fun setHeartRateSensor(measurement: Int) =
        sensorLabelerDataStore.setHeartRateSensor(measurement)

    /**
     * Setter for Time Stamp sensor.
     *
     * @param timeStamp measurement timestamp
     */
    suspend fun setTimeStampSensor(timeStamp: String) =
        sensorLabelerDataStore.setTimeStampSensor(timeStamp)

    /**
     * Setter for Gyro X Rate Sensor.
     *
     * @param gyroRate x axis
     */
    suspend fun setGyroXRateSensor(gyroRate: Int) =
        sensorLabelerDataStore.setGyroXRateSensor(gyroRate)

    /**
     * Setter for Gyro Y Rate Sensor.
     *
     * @param gyroRate y axis
     */
    suspend fun setGyroYRateSensor(gyroRate: Int) =
        sensorLabelerDataStore.setGyroYRateSensor(gyroRate)

    /**
     * Setter for Gyro Z Rate Sensor.
     *
     * @param gyroRate z axis
     */
    suspend fun setGyroZRateSensor(gyroRate: Int) =
        sensorLabelerDataStore.setGyroZRateSensor(gyroRate)

    /**
     * Setter for Acceleration X Rate Sensor.
     *
     * @param measurementRate x axis
     */
    suspend fun setAccelerationXRateSensor(measurementRate: Int) =
        sensorLabelerDataStore.setAccelerationXRateSensor(measurementRate)

    /**
     * Setter for Acceleration Y Rate Sensor.
     *
     * @param measurementRate y axis
     */
    suspend fun setAccelerationYRateSensor(measurementRate: Int) =
        sensorLabelerDataStore.setAccelerationYRateSensor(measurementRate)

    /**
     * Setter for Acceleration Z Rate Sensor.
     *
     * @param measurementRate z axis
     */
    suspend fun setAccelerationZRateSensor(measurementRate: Int) =
        sensorLabelerDataStore.setAccelerationZRateSensor(measurementRate)

    /**
     * Setter for Step Counter Sensor
     *
     * @param steps number of steps.
     */
    suspend fun setStepCounterSensor(steps: Int) =
        sensorLabelerDataStore.setStepCounterSensor(steps)

    /**
     * Setter for Locations Sensor.
     *
     * @param locationEntity Current location
     */
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
package com.sensordatalabeler

import android.util.Log
import com.sensordatalabeler.file.WriteFile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Measurement Values Class.
 * This Class will contain at least 10 measurement.
 */
class MeasurementValues {

    private var activeMeasurement = false
    private var heartRate = 0
    private var time = ""
    private var gyro: IntArray = IntArray(length)
    private var acceleration: IntArray = IntArray(length)
    private var steps = 0
    private var location: DoubleArray = DoubleArray(2)
    private var date = ""
    private var nameOfActivity = "NO NAME"
    private var valuesArray = Array(numberOfDatapoint) { "" }
    private var mNumber = 0

    /**
     * Add measurement
     *
     * @param type type of measurement
     * @param value value of measurement
     */
    fun addMeasurement(type: String, value: Any) {
        if (this.mNumber >= numberOfDatapoint) {
            this.mNumber = 0
            WriteFile.saveData(this)
        }
        when (type) {
            "heartRate" -> this.heartRate = value as Int
            "steps" -> this.steps = value as Int
            "gyro" -> this.gyro = value as IntArray
            "acceleration" -> this.acceleration = value as IntArray
            "location" -> this.location = value as DoubleArray
            else -> Log.d(TAG, "Type not supported.")
        }
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        this.date = current.format(formatter)
        this.valuesArray[mNumber] =
            "${this.nameOfActivity},${this.date},${this.acceleration.toList()}," +
                    "${this.heartRate},${this.gyro.toList()},${this.steps},${this.location.toList()};"
        this.mNumber += 1
    }

    /**
     * Setter for status of measurement.
     */
    fun setActiveMeasurement(active: Boolean) {
        activeMeasurement = active
    }

    /**
     * Setter for time.
     */
    fun setTime(timeString: String) {
        time = timeString
    }

    /**
     * Setter for Activity name.
     */
    fun setNameOfActivity(name: String) {
        nameOfActivity = name
    }

    /**
     * Getter for Activity name.
     */
    fun getNameOfAcvity(): String {
        return nameOfActivity
    }

    /**
     * Getter for data.
     */
    fun getData(): Array<String> {
        return valuesArray
    }

    /**
     * Getter for Status of measurement.
     */
    fun getActiveMeasurement(): Boolean {
        return activeMeasurement
    }

    /**
     * Getter for Time.
     */
    fun getTime(): String {
        return time
    }

    /**
     * Getter for heart rate.
     */
    fun getHeartRate(): Int {
        return heartRate
    }

    /**
     * Reset the data.
     */
    fun resetData() {
        Log.d(TAG, "Data reset")
        this.valuesArray = Array(numberOfDatapoint) { "" }
        this.mNumber = 0
    }

    companion object {
        private const val TAG = "MeasurementValues"
        private const val length = 3
        private const val numberOfDatapoint = 30
    }
}

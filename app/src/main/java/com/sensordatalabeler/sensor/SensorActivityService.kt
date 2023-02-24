package com.sensordatalabeler.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.roundToInt


class SensorActivityService: SensorEventListener {

    private lateinit var mName: String

    // Sensor Managers
    private lateinit var mSensorManager : SensorManager
    private var mSensor : Sensor ?= null

    private var mMeasurement = 0



    fun onCreate(sensorManager: SensorManager, type : Int, name: String): SensorActivityService {
        Log.d(TAG, "onCreate()")
        mName = name
        mSensorManager = sensorManager
        try {
            mSensor = mSensorManager.getDefaultSensor(type)
        } catch (e : java.lang.NullPointerException) {
            Log.d(TAG, "$mName sensor not found")
            return this
        }
        Log.d(TAG, "$mName sensor found")
        return this
    }

    fun startMeasurement() {
        val sensorRegistered = mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST)
        Log.d(TAG, "$mName Sensor registered: " + (if (sensorRegistered) "yes" else "no"))
    }

    fun stopMeasure() {
        mSensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        //val mHeartRateFloat = event.values[0]
        //mHeartRate = mHeartRateFloat.roundToInt()
        val mMeasurementFloat = event.values[0]
        mMeasurement = mMeasurementFloat.roundToInt()
    }


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // Do something here if sensor accuracy changes.
    }

    fun onPause() {
        mSensorManager.unregisterListener(this)
    }

    fun getMeasurementRate(): Int {
        return mMeasurement
    }


    companion object {

        private const val TAG = "Sensor Service:"
    }
}
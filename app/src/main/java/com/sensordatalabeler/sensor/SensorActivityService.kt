package com.sensordatalabeler.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import java.io.Serializable
import kotlin.math.roundToInt


class SensorActivityService: SensorEventListener {

    private lateinit var mName: String

    // Sensor Managers
    private lateinit var mSensorManager : SensorManager
    private var mSensor : Sensor ?= null

    private val mMeasurement: IntArray = IntArray(3)



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

    fun startMeasurement( delay : Int) {
        val sensorRegistered = mSensorManager.registerListener(this, mSensor, delay)
        Log.d(TAG, "$mName Sensor registered: " + (if (sensorRegistered) "yes" else "no"))
    }

    fun stopMeasure() {
        mSensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if(mName == "HEART RATE" || mName == "STEP_COUNTER"){
            val mMeasurementFloat = event.values[0]
            mMeasurement[0] = mMeasurementFloat.roundToInt()
        }else {
            val mMeasurementFloatX = event.values[0]
            val mMeasurementFloatY = event.values[1]
            val mMeasurementFloatZ = event.values[2]
            mMeasurement[0] = mMeasurementFloatX.roundToInt()
            mMeasurement[1] = mMeasurementFloatY.roundToInt()
            mMeasurement[2] = mMeasurementFloatZ.roundToInt()
        }

    }


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // Do something here if sensor accuracy changes.
    }

    fun onPause() {
        mSensorManager.unregisterListener(this)
    }

    fun getMeasurementRate(): IntArray {
            return mMeasurement
    }


    companion object {

        private const val TAG = "Sensor Service:"
    }
}
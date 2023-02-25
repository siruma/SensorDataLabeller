package com.sensordatalabeler

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.sensordatalabeler.databinding.ActivityMainBinding

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as MainApp).repository)
    }

    private var activeSensorLabeler = false
        set(newActiveStatus) {
            if (field != newActiveStatus) {
                field = newActiveStatus
                if (newActiveStatus) {
                    binding.startStopWorkoutButton.text =
                        getString(R.string.stop_sensor_button_text)
                    updateOutput(heartRate)
                } else {
                    binding.startStopWorkoutButton.text =
                        getString(R.string.start_sensor_button_text)
                }

            }
        }

    //Measurement values
    private var heartRate = 0
    private var time = 0
    private val gyro : IntArray = IntArray(3)
    private var acceleration : IntArray = IntArray(3)
    private var gpsValue = 0

    private var foregroundOnlyServiceBound = false

    private var foregroundOnlySensorLabelerService: ForegroundOnlySensorLabelerService? = null

    //Connection monitor
    private var foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder?) {
            val binder = service as ForegroundOnlySensorLabelerService.LocalBinder
            foregroundOnlySensorLabelerService = binder.sensorLabelerService
            foregroundOnlyServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundOnlySensorLabelerService = null
            foregroundOnlyServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel.heartRateFlow.observe(this) { measurement ->
            heartRate = measurement
            //updateHeartRate(heartRate)
        }

        mainViewModel.gyroXRateFlow.observe(this) {measurement ->
            gyro[0] = measurement
            updateGyro(gyro)
        }
        mainViewModel.gyroYRateFlow.observe(this) {measurement ->
            gyro[1] = measurement
            updateGyro(gyro)
        }
        mainViewModel.gyroZRateFlow.observe(this) {measurement ->
            gyro[2] = measurement
            updateGyro(gyro)
        }

        mainViewModel.accelerationXFlow.observe(this) {measurement ->
            acceleration[0] = measurement
            //updateAcceleration(accelerationX)
        }
        mainViewModel.accelerationYFlow.observe(this) {measurement ->
            acceleration[1] = measurement
            //updateAcceleration(accelerationX)
        }
        mainViewModel.accelerationZFlow.observe(this) {measurement ->
            acceleration[2] = measurement
            //updateAcceleration(accelerationX)
        }

        mainViewModel.timeStampFlow.observe(this) { measurement ->
            time = measurement
            //updateTimeStamp(time)
        }

        mainViewModel.activeSensorLabelerFlow.observe(this) { active ->
            Log.d(TAG, "Sensor Status change: $activeSensorLabeler")
            activeSensorLabeler = active
        }
    }

    override fun onStart() {
        super.onStart()

        val serviceIntent = Intent(this, ForegroundOnlySensorLabelerService::class.java)
        bindService(serviceIntent, foregroundOnlyServiceConnection, BIND_AUTO_CREATE)
    }

    override fun onStop() {
        if (foregroundOnlyServiceBound) {
            unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyServiceBound = false
        }
        super.onStop()
    }

    fun onClickSensorLabeler(view: View) {
        Log.d(TAG, "onClickSensorLabeler()")
        if (activeSensorLabeler) {
            foregroundOnlySensorLabelerService?.stopSensorLabeler()
            binding.saveSensorDataButton.visibility = View.VISIBLE
            binding.nameMeasurement.visibility = View.GONE
            binding.outputTextView.text = getString(R.string.default_greeting_message)
        } else {
            foregroundOnlySensorLabelerService?.startSensorLabeler()
            binding.saveSensorDataButton.visibility = View.GONE
            binding.nameMeasurement.visibility = View.VISIBLE
        }
    }

    fun onClickSensorSave(view: View) {
        Log.d(TAG, "onClickSensorSave()")
        if (activeSensorLabeler) {
            Log.d(TAG, "Sensor measurement ongoing")
        } else {
            Log.d(TAG, "Sensor data saved")
        }
    }

    fun onClickSensorName(view: View) {
        Log.d(TAG, "onClickSensorName()")
        if (activeSensorLabeler) {
            nameSensorData()
        }
    }

    fun onClickChoice(view: View) {
        Log.d(TAG, "onClickChoice()")
        saveChoice()
    }

    fun onClickNext(view: View) {
        Log.d(TAG, "onClickNext()")
        if(binding.nameMenu1.isVisible) {
           binding.nameMenu1.visibility = View.GONE
            binding.nameMenu2.visibility = View.VISIBLE
        }else if (binding.nameMenu2.isVisible){
            binding.nameMenu2.visibility = View.GONE
            binding.nameMenu3.visibility = View.VISIBLE
        }else if (binding.nameMenu3.isVisible){
            binding.nameMenu3.visibility = View.GONE
            binding.nameMenu1.visibility = View.VISIBLE
            //binding.nameMenu4.visibility = View.VISIBLE
        }else if(binding.nameMenu4.isVisible){
            binding.nameMenu4.visibility = View.GONE
            binding.nameMenu1.visibility = View.VISIBLE
        }
    }

    fun onClickElse(view: View){
        Log.d(TAG, "onClickElse()")
        binding.nameMenu4.visibility = View.GONE
        binding.customName.visibility = View.VISIBLE

    }


    fun onClickSave(view: View) {
        //TODO save custom name and fix the problem with onscreen keyboard
        binding.customName.visibility = View.GONE
        binding.mainMenu.visibility = View.VISIBLE
    }

    private fun saveChoice() {
        // TODO Save the name of the measurement
        binding.mainMenu.visibility = View.VISIBLE
        if (binding.nameMenu1.isVisible) {
            binding.nameMenu1.visibility = View.GONE
        }else if (binding.nameMenu2.isVisible){
            binding.nameMenu2.visibility = View.GONE
        }else if (binding.nameMenu3.isVisible){
            binding.nameMenu3.visibility = View.GONE
        }
    }

    private fun nameSensorData() {
        binding.mainMenu.visibility = View.GONE
        binding.nameMenu1.visibility = View.VISIBLE
    }

    private fun updateHeartRate(measurement: Int) {
        Log.d(TAG, "updateHeartRate()")
        Log.d(TAG, "Heart Rate: $measurement")
        val output = getString(R.string.heart_rate_text, measurement)
        binding.outputTextView.text = output
    }

    private fun updateTimeStamp(timeStamp: Int) {
        Log.d(TAG, "updateTimeStamp()")
        val output = getString(R.string.time_stamp_text, timeStamp)
    }

    private fun updateOutput(measurement: Int) {
        Log.d(TAG, "updateOutput()")
        val output = getString(R.string.heart_rate_text, measurement)
        binding.outputTextView.text = output
    }
    private fun updateAcceleration(measurement: IntArray) {
        Log.d(TAG, "updateAcceleration()")
        Log.d(TAG, "Acceleration: ${measurement[0]}, ${measurement[1]},${measurement[2]}")
        val output = getString(R.string.acceleration_rate_text, measurement[0], measurement[1], measurement[2])
        binding.outputTextView.text = output
    }

    private fun updateGyro(measurement: IntArray) {
        Log.d(TAG, "updateGyro()")
        Log.d(TAG, "Gyro rate: ${measurement[0]}, ${measurement[1]},${measurement[2]}")
        val output = getString(R.string.gyro_rate_text, measurement[0], measurement[1], measurement[2])
        binding.outputTextView.text = output
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
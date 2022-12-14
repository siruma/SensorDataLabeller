package com.sensordatalabeler

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
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
                } else {
                    binding.startStopWorkoutButton.text =
                        getString(R.string.start_sensor_button_text)
                }
                updateOutput(heartBeat)
            }
        }

    private var heartBeat = 0

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

        mainViewModel.heardBeatPointsFlow.observe(this) { heartRate ->
            heartBeat = heartRate
            updateOutput(heartBeat)
        }

        mainViewModel.activeSensorLabelerFlow.observe(this) { active ->
            Log.d(TAG, "Sensor Status change: $activeSensorLabeler")
            activeSensorLabeler = active
        }
    }

    override fun onStart() {
        super.onStart()

        val serviceIntent = Intent(this, ForegroundOnlySensorLabelerService::class.java)
        bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)
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
        } else {
            foregroundOnlySensorLabelerService?.startSensorLabeler()
        }
    }

    private fun updateOutput(measurement:Int) {
        Log.d(TAG,"updateOutput()")
        val output = getString(R.string.heart_rate_text, measurement)
        binding.outputTextView.text = output
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
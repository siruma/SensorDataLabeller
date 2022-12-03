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

class MainActivity:ComponentActivity() {
    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as MainApp).repository)
    }

    private var activeSensorLabeler = false
        set(newActiveStatus) {
            if (field != newActiveStatus)
                field = newActiveStatus
                if (newActiveStatus){
                    binding.startStopWalkingWorkoutButton.text =
                        getString(R.string.stop_sensor_button_text)
                } else {
                    binding.startStopWalkingWorkoutButton.text =
                        getString(R.string.start_sensor_button_text)
                }
            updateOutput(heardBeat)
        }

    private var heardBeat = 0

    private var foregroundOnlyServiceBound = false

    private var foregroundOnlySensorLabelerService: ForegroundOnlySensorLabelerService? = null

    private var foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            TODO("Not yet implemented")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            TODO("Not yet implemented")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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

    fun onClickWalkingWorkout(view: View) {
        Log.d(TAG, "onClickWalkingWorkout()")
        if (activeSensorLabeler) {
            foregroundOnlySensorLabelerService?.stopSensorLabeler()
        } else {
            foregroundOnlySensorLabelerService?.startSensorlabeler()
        }
    }

    private fun updateOutput(measurement:Int) {
        Log.d(TAG,"updateOutput()")
        val output = "{measurement} Heard beat per minute"
        binding.outputTextView.text = output
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
package com.sensordatalabeler

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.sensordatalabeler.databinding.ActivityMainBinding
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.*

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

            }
        }

    //Measurement values
    private var heartRate = 0
    private var time = ""
    private val gyro: IntArray = IntArray(3)
    private val acceleration: IntArray = IntArray(3)
    private var steps = 0
    private var longitude = 0.0
    private var latitude = 0.0
    private var date = Date()
    private var nameOfActivity = "NO NAME"
    private var fileName = "data"
    private var fileType = ".csv"

    private var bufferedWriter: BufferedWriter? = null

    private var activeMeasurement = false

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
            if (activeMeasurement) {
                updateHeartRate(heartRate)
            }
        }

        mainViewModel.gyroXRateFlow.observe(this) { measurement ->
            gyro[0] = measurement
        }
        mainViewModel.gyroYRateFlow.observe(this) { measurement ->
            gyro[1] = measurement
        }
        mainViewModel.gyroZRateFlow.observe(this) { measurement ->
            gyro[2] = measurement
        }

        mainViewModel.accelerationXFlow.observe(this) { measurement ->
            acceleration[0] = measurement
        }
        mainViewModel.accelerationYFlow.observe(this) { measurement ->
            acceleration[1] = measurement
        }
        mainViewModel.accelerationZFlow.observe(this) { measurement ->
            acceleration[2] = measurement
        }

        mainViewModel.stepCounterFlow.observe(this) { measurement ->
            steps = measurement
        }

        mainViewModel.timeStampFlow.observe(this) { measurement ->
            time = measurement.toString()
        }

        mainViewModel.activeSensorLabelerFlow.observe(this) { active ->
            activeSensorLabeler = active
        }

        mainViewModel.latitudeFlow.observe(this) { latitudeData ->
            latitude = latitudeData
        }
        mainViewModel.longitudeFlow.observe(this) { longitudeData ->
            longitude = longitudeData
        }
        mainViewModel.dateFlow.observe(this) { dateSensor ->
            date = Date(dateSensor)
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

    /*
    Start and stop the measurement
     */
    fun onClickSensorLabeler(view: View) {
        Log.d(TAG, "onClickSensorLabeler()")
        if (activeSensorLabeler) {
            foregroundOnlySensorLabelerService?.stopSensorLabeler()
            activeMeasurement = false
            binding.exportSensorDataButton.visibility = View.VISIBLE
            binding.nameMeasurement.visibility = View.GONE
            binding.outputTextView.text = getString(R.string.default_greeting_message)
            closeFile()
        } else {
            foregroundOnlySensorLabelerService?.startSensorLabeler()
            activeMeasurement = true
            binding.exportSensorDataButton.visibility = View.GONE
            binding.nameMeasurement.visibility = View.VISIBLE
            openFile()
        }
    }

    /*
    Closed the file after measurement
     */
    private fun closeFile() {
        Log.d(TAG, "File Closed")
        bufferedWriter?.close()
    }


    /*
    Opens the file when measurement start
     */
    private fun openFile() {
        Log.d(TAG, "File opened")
        var os = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val sdCard = Environment.getExternalStorageDirectory()
            val dir = File(sdCard.absolutePath + "/Android/data/com.sensordatalabeler/files/Data")
            if (!dir.exists())
                dir.mkdirs()
            val file = File(dir, fileName + time + fileType)
            if (!file.exists())
                file.createNewFile()
            Log.d(TAG, "Write in sdcard")
            FileOutputStream(file)
        } else {
            openFileOutput(fileName, MODE_PRIVATE)
        }
        bufferedWriter = BufferedWriter(OutputStreamWriter(os))
    }

    /*
    Export files
     */
    fun onClickSensorExport(view: View) {
        Log.d(TAG, "onClickSensorExport()")
        if (activeSensorLabeler) {
            Log.d(TAG, "Sensor measurement ongoing")
        } else {
            Log.d(TAG, "Sensor data Exported")
            val output = "Not yet implemented"
            // TODO Export data nicely by one file or packed
            binding.outputTextView.text = output
        }
    }

    /*
    write data in the file
     */
    private fun saveData() {

        val data =
            "$nameOfActivity,$date,${acceleration.toList()},$heartRate,${gyro.toList()},$steps,($latitude,$longitude);"
        bufferedWriter?.write(data)
        bufferedWriter?.newLine()
        Log.d(TAG, "DATA: $data")
    }

    fun onClickSensorName(view: View) {
        Log.d(TAG, "onClickSensorName()")
        if (activeSensorLabeler) {
            nameSensorData()
        }
    }

    /*
    Activate save choice
     */
    fun onClickChoice(view: View) {
        Log.d(TAG, "onClickChoice()")
        saveChoice(view)
    }

    /*
      Change the page of name menu
     */
    fun onClickNext(view: View) {
        Log.d(TAG, "onClickNext()")
        if (binding.nameMenu1.isVisible) {
            binding.nameMenu1.visibility = View.GONE
            binding.nameMenu2.visibility = View.VISIBLE
        } else if (binding.nameMenu2.isVisible) {
            binding.nameMenu2.visibility = View.GONE
            binding.nameMenu3.visibility = View.VISIBLE
        } else if (binding.nameMenu3.isVisible) {
            binding.nameMenu3.visibility = View.GONE
            binding.nameMenu1.visibility = View.VISIBLE
            //binding.nameMenu4.visibility = View.VISIBLE
        } else if (binding.nameMenu4.isVisible) {
            binding.nameMenu4.visibility = View.GONE
            binding.nameMenu1.visibility = View.VISIBLE
        }
    }

    /*
    Opens custom name menu
     */
    fun onClickElse(view: View) {
        Log.d(TAG, "onClickElse()")
        binding.nameMenu4.visibility = View.GONE
        binding.customName.visibility = View.VISIBLE

    }

    /*
    Saves Custom name
     */
    fun onClickSave(view: View) {
        //TODO save custom name and fix the problem with onscreen keyboard
        binding.customName.visibility = View.GONE
        binding.mainMenu.visibility = View.VISIBLE
    }

    /*
    Save the default name and closes the name menu
     */
    private fun saveChoice(view: View) {
        binding.mainMenu.visibility = View.VISIBLE
        if (binding.nameMenu1.isVisible) {
            binding.nameMenu1.visibility = View.GONE
        } else if (binding.nameMenu2.isVisible) {
            binding.nameMenu2.visibility = View.GONE
        } else if (binding.nameMenu3.isVisible) {
            binding.nameMenu3.visibility = View.GONE
        }
        val b = view as Button
        nameOfActivity = b.text.toString()
        Log.d(TAG, "NAME: $nameOfActivity")
        saveData()

    }

    /*
    Opens the name manu
     */
    private fun nameSensorData() {
        binding.mainMenu.visibility = View.GONE
        binding.nameMenu1.visibility = View.VISIBLE
    }

    /*
    Update the heart rate to screen
     */
    private fun updateHeartRate(measurement: Int) {
        Log.d(TAG, "updateHeartRate()")
        Log.d(TAG, "Heart Rate: $measurement")
        val output = getString(R.string.heart_rate_text, measurement)
        binding.outputTextView.text = output
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
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
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope

import androidx.wear.ambient.AmbientModeSupport
import com.sensordatalabeler.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Date

/**
 * Main activity.
 */
class MainActivity : FragmentActivity(), AmbientModeSupport.AmbientCallbackProvider {
    private lateinit var binding: ActivityMainBinding
    private lateinit var ambientController: AmbientModeSupport.AmbientController

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

    // File
    private var fileName = "data"
    private var fileType = ".csv"

    //Measurement values
    private val measurementValues = MeasurementValues()

    private var bufferedWriter: BufferedWriter? = null

    private var activeMeasurement = false

    private var foregroundOnlyServiceBound = false

    private var foregroundOnlySensorLabelerService: ForegroundOnlySensorLabelerService? = null

    /**
     * Connection to the monitor
     */
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

    /**
     * Create the observers for sensor data.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ambientController = AmbientModeSupport.attach(this)
        Log.d(TAG, "Is Ambient: ${ambientController.isAmbient}")

        mainViewModel.heartRateFlow.observe(this) { measurement ->
            measurementValues.addMeasurement("heartRate", measurement)
            if (measurementValues.getActiveMeasurement()) {
                updateHeartRate(measurementValues.getHeartRate())
            }
        }

        combine(
            mainViewModel.gyroXRateFlow.asFlow(),
            mainViewModel.gyroYRateFlow.asFlow(),
            mainViewModel.gyroZRateFlow.asFlow()
        ) { x: Int, y: Int, z: Int ->
            intArrayOf(x, y, z)
        }.onEach { gyroData: IntArray ->
            measurementValues.addMeasurement("gyro", gyroData)
        }.launchIn(lifecycleScope)

        combine(
            mainViewModel.accelerationXFlow.asFlow(),
            mainViewModel.accelerationYFlow.asFlow(),
            mainViewModel.accelerationZFlow.asFlow()
        ) { x: Int, y: Int, z: Int ->
            intArrayOf(x, y, z)
        }.onEach { accelerationData: IntArray ->
            measurementValues.addMeasurement("acceleration", accelerationData)
        }.launchIn(lifecycleScope)

        mainViewModel.stepCounterFlow.observe(this) { measurement ->
            measurementValues.addMeasurement("steps", measurement)
        }

        mainViewModel.timeStampFlow.observe(this) { measurement ->
            measurementValues.setTime(measurement.toString())
        }

        mainViewModel.activeSensorLabelerFlow.observe(this) { active ->
            activeSensorLabeler = active
        }

        combine(
            mainViewModel.latitudeFlow.asFlow(),
            mainViewModel.longitudeFlow.asFlow()
        ) { latitude: Double, longitude: Double ->
            doubleArrayOf(latitude, longitude)
        }.onEach { locationData: DoubleArray ->
            measurementValues.addMeasurement("location", locationData)
        }.launchIn(lifecycleScope)

        mainViewModel.dateFlow.observe(this) { dateSensor ->
            measurementValues.setDate(Date(dateSensor))
        }
    }

    /**
     * Bind the foreground
     */
    override fun onStart() {
        super.onStart()

        val serviceIntent = Intent(this, ForegroundOnlySensorLabelerService::class.java)
        bindService(serviceIntent, foregroundOnlyServiceConnection, BIND_AUTO_CREATE)
    }

    /**
     * Unbinds the foreground
     */
    override fun onStop() {
        if (foregroundOnlyServiceBound) {
            unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyServiceBound = false
        }
        super.onStop()
    }

    /**
    * Start and stop the measurement
     */
    fun onClickSensorLabeler(view: View) {
        Log.d(TAG, "onClickSensorLabeler()")
        if (activeSensorLabeler) {
            foregroundOnlySensorLabelerService?.stopSensorLabeler()
            measurementValues.setActiveMeasurement(false)
            nameSensorData()
        } else {
            foregroundOnlySensorLabelerService?.startSensorLabeler()
            measurementValues.setActiveMeasurement(true)
            binding.exportSensorDataButton.visibility = View.GONE
            binding.nameMeasurement.visibility = View.VISIBLE
            openFile()
        }
    }

    /**
     * Closed the file
     */
    private fun closeFile() {
        Log.d(TAG, "File Closed")
        bufferedWriter?.close()
    }


    /**
     * Opens the file to write.
     */
    private fun openFile() {
        Log.d(TAG, "File opened")
        val time = measurementValues.getTime()
        val os = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val dir = File(applicationContext.getExternalFilesDir(null), "/Data")
            Log.d(TAG, "Path: ${dir.path}")
            Log.d(TAG, "Dir exists: ${dir.exists()}")
            if (!dir.exists())
                Files.createDirectories(Paths.get(dir.path))
            Log.d(TAG, "Dir exists: ${dir.exists()}")
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

    /**
     * Export files to zip-file.
     */
    fun onClickSensorExport(view: View) {
        Log.d(TAG, "onClickSensorExport()")
        if (activeSensorLabeler) {
            Log.d(TAG, "Sensor measurement ongoing")
        } else {
            Log.d(TAG, "Sensor data Exported")
            val output = "Data Exported"
            writeMetaData()
            try {
                zip(File(PATH_TO_DATA, "Data.zip"), File(PATH_TO_DATA))
                cleanUp(File(PATH_TO_DATA))
            } catch (e: IllegalStateException) {
                zip(File(filesDir, "Data.zip"), filesDir)
                cleanUp(filesDir)
            }
            binding.outputTextView.text = output
        }
    }

    /**
     * Write META data.
     */
    private fun writeMetaData() {
        val metadata =
            "[NAME, DATA, ACCELERATION(x,y,z), HEART RATE, GYRO(x,y,z), STEPS, (LATITUDE,LONGITUDE)]"
        val metaFileName = "metadata.txt"
        val os = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val dir = File(PATH_TO_DATA)
            if (!dir.exists())
                dir.mkdirs()
            val file = File(dir, metaFileName)
            if (!file.exists())
                file.createNewFile()
            FileOutputStream(file)
        } else {
            openFileOutput(metaFileName, MODE_PRIVATE)
        }

        val bw = BufferedWriter(OutputStreamWriter(os))
        bw.write(metadata)
        bw.close()

    }

    /**
     * Write data in the file
     */
    private fun saveData() {
        val nonEmptyData = measurementValues.getData().filter { it.isNotEmpty() }

        if (bufferedWriter == null) {
            Log.e(TAG, "BufferedWriter is null, data cannot be saved.")
            return
        }

        for (data in nonEmptyData) {
            bufferedWriter!!.write(data)
            bufferedWriter!!.newLine()
            Log.d(TAG, "DATA: $data")
        }
        measurementValues.resetData()
        if (!measurementValues.getActiveMeasurement())
            closeFile()
    }

    /**
     * Button function for name sensor
     */
    fun onClickSensorName(view: View) {
        Log.d(TAG, "onClickSensorName()")
        if (activeSensorLabeler) {
            nameSensorData()
        }
    }

    /**
     * Activate save choice
     */
    fun onClickChoice(view: View) {
        Log.d(TAG, "onClickChoice()")
        saveChoice(view)
    }

    /**
     * Change the page of name menu
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

    /**
     * Opens custom name menu
     */
    fun onClickElse(view: View) {
        Log.d(TAG, "onClickElse()")
        binding.nameMenu4.visibility = View.GONE
        binding.customName.visibility = View.VISIBLE

    }

    /**
     * Saves Custom name
     */
    fun onClickSave(view: View) {
        //TODO save custom name and fix the problem with onscreen keyboard
        binding.customName.visibility = View.GONE
        binding.mainMenu.visibility = View.VISIBLE
    }

    /**
     * Save the default name and closes the name menu
     */
    private fun saveChoice(view: View) {
        binding.mainMenu.visibility = View.VISIBLE
        if (!activeMeasurement) {
            binding.nameMeasurement.visibility = View.GONE
            binding.exportSensorDataButton.visibility = View.VISIBLE
            binding.outputTextView.text = getString(R.string.default_greeting_message)
        }
        if (binding.nameMenu1.isVisible) {
            binding.nameMenu1.visibility = View.GONE
        } else if (binding.nameMenu2.isVisible) {
            binding.nameMenu2.visibility = View.GONE
        } else if (binding.nameMenu3.isVisible) {
            binding.nameMenu3.visibility = View.GONE
        }
        val b = view as Button
        measurementValues.setNameOfActivity(b.text.toString())
        Log.d(TAG, "NAME: $measurementValues.nameOfActivity")
        saveData()

    }

    /**
     * Opens the name manu
     */
    private fun nameSensorData() {
        binding.mainMenu.visibility = View.GONE
        binding.nameMenu1.visibility = View.VISIBLE
    }

    /**
     * Update the heart rate to screen
     */
    private fun updateHeartRate(measurement: Int) {
        Log.d(TAG, "updateHeartRate()")
        Log.d(TAG, "Heart Rate: $measurement")
        val output = getString(R.string.heart_rate_text, measurement)
        binding.outputTextView.text = output
    }

    companion object {
        private const val TAG = "MainActivity"
        private val PATH_TO_DATA =
            "${Environment.getExternalStorageDirectory()}/Android/data/com.sensordatalabeler/files/Data"
    }

    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback = MyAmbientCallBack()
    private class MyAmbientCallBack : AmbientModeSupport.AmbientCallback() {
        override fun onEnterAmbient(ambientDetails: Bundle?) {
            super.onEnterAmbient(ambientDetails)
        }

        override fun onExitAmbient() {
            super.onExitAmbient()
        }

        override fun onUpdateAmbient() {
            super.onUpdateAmbient()
        }
    }
}
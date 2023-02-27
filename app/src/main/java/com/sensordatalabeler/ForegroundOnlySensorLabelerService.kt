package com.sensordatalabeler


import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.CATEGORY_WORKOUT
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.sensordatalabeler.data.LocationManager
import com.sensordatalabeler.data.SensorLabelerRepository
import com.sensordatalabeler.sensor.LocationUpdateBroadcastReceiver
import com.sensordatalabeler.sensor.SensorActivityService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ForegroundOnlySensorLabelerService : LifecycleService() {

    private val sensorLabelerRepository: SensorLabelerRepository by lazy {
        (application as MainApp).repository
    }

    // LOCATION
    private lateinit var locationReceiver: LocationManager

    private lateinit var notificationManager: NotificationManager

    // BODY Sensors
    private lateinit var heartRateSensor: SensorActivityService
    private lateinit var gyroRateSensor: SensorActivityService
    private lateinit var accelerationRateSensor: SensorActivityService
    private lateinit var stepCounterSensor: SensorActivityService

    private var confChange = false

    private var serviceRunForeground = false

    private val localBinder = LocalBinder()

    private var sensorLabelerActive = false

    private var dataFromSensorJob: Job? = null

    private fun setActiveSensorLabeler(active: Boolean) = lifecycleScope.launch {
        sensorLabelerRepository.setActiveSensorLabeler(active)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate()")

        sensorLabelerRepository.activeSensorLabelerFlow.asLiveData().observe(this) { active ->
            if (sensorLabelerActive != active) {
                sensorLabelerActive = active
            }
        }
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        locationReceiver = LocationManager.getInstance(this)

        heartRateSensor = SensorActivityService().onCreate(
            getSystemService(SENSOR_SERVICE) as SensorManager,
            Sensor.TYPE_HEART_RATE,
            "HEART RATE"
        )
        gyroRateSensor = SensorActivityService().onCreate(
            getSystemService(SENSOR_SERVICE) as SensorManager,
            Sensor.TYPE_GYROSCOPE,
            "GYROSCOPE"
        )
        accelerationRateSensor = SensorActivityService().onCreate(
            getSystemService(SENSOR_SERVICE) as SensorManager,
            Sensor.TYPE_ACCELEROMETER,
            "ACCELEROMETER"
        )

        stepCounterSensor = SensorActivityService().onCreate(
            getSystemService(SENSOR_SERVICE) as SensorManager,
            Sensor.TYPE_STEP_COUNTER,
            "STEP_COUNTER"
        )

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "onStartCommand()")

        val cancelLabelerFromNotification =
            intent?.getBooleanExtra(EXTRA_CANCEL_LABELER_FROM_NOTIFICATION, false)
                ?: false

        if (cancelLabelerFromNotification) {
            stopSensorLabelerWithServiceShutdownOption(true)
        }
        //Stop the system start a new service after it's been killed
        return Service.START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        Log.d(TAG, "onBind()")
        notForegroundService()
        return localBinder
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        Log.d(TAG, "onRebind()")
        notForegroundService()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        super.onUnbind(intent)
        Log.d(TAG, "onUnbind()")

        if (!confChange && sensorLabelerActive) {
            Log.d(TAG, "Start foreground service")
            val notification =
                generateNotification(getString(R.string.labeler_notification_started_text))
            startForeground(NOTIFICATION_ID, notification)
            serviceRunForeground = true
        }
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        confChange = true
    }

    private fun notForegroundService() {
        //stopForeground(true)
        serviceRunForeground = false
        confChange = false
    }

    fun startSensorLabeler() {
        Log.d(TAG, "startSensorLabeler()")

        setActiveSensorLabeler(true)
        heartRateSensor.startMeasurement(1000000)
        gyroRateSensor.startMeasurement(0)
        accelerationRateSensor.startMeasurement(0)
        stepCounterSensor.startMeasurement(0)
        locationReceiver.startLocationUpdates()
        startService(Intent(applicationContext, ForegroundOnlySensorLabelerService::class.java))
        //subscribe to location and sensor callbacks here
        dataFromSensorJob = lifecycleScope.launch {
            readSensorData()
        }
    }

    private suspend fun readSensorData() {
        while (true) {
            heartRateSensor.let { sensorLabelerRepository.setHeartRateSensor(it.getMeasurementRate()[0]) }
            gyroRateSensor.let {
                val intArray = it.getMeasurementRate()
                sensorLabelerRepository.setGyroXRateSensor(intArray[0])
                sensorLabelerRepository.setGyroYRateSensor(intArray[1])
                sensorLabelerRepository.setGyroZRateSensor(intArray[2])}
            accelerationRateSensor.let {
                val intArray = it.getMeasurementRate()
                sensorLabelerRepository.setAccelerationXRateSensor(intArray[0])
                sensorLabelerRepository.setAccelerationYRateSensor(intArray[1])
                sensorLabelerRepository.setAccelerationZRateSensor(intArray[2])}
            stepCounterSensor.let { sensorLabelerRepository.setStepCounterSensor(it.getMeasurementRate()[0]) }
            locationReceiver.let {
                sensorLabelerRepository.setLocationsSensor(it.getLocationEntity())
            }
            delay(THREE_SECONDS_MILLISECONDS)
        }
    }

    fun stopSensorLabeler() {
        Log.d(TAG, "stopSensorLabeler")
        stopSensorLabelerWithServiceShutdownOption(false)
    }

    private fun stopSensorLabelerWithServiceShutdownOption(stopService: Boolean) {
        Log.d(TAG, "stopSensorLabelerWithServiceShutdownOption()")
        heartRateSensor.stopMeasure()
        gyroRateSensor.stopMeasure()
        accelerationRateSensor.stopMeasure()
        dataFromSensorJob?.cancel()
        locationReceiver.stopLocationUpdates()

        lifecycleScope.launch {
            val job: Job = setActiveSensorLabeler(false)
            if (stopService) {
                //wait until DataStore data is saved
                job.join()
                stopSelf()
            }
        }
    }


    @SuppressLint("UnspecifiedImmutableFlag")
    private fun generateNotification(mainText: String): Notification {
        Log.d(TAG, "generateNotification()")
        //Create notification channel
        val titleText = getString(R.string.notification_title)
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID, titleText, NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(notificationChannel)

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(mainText)
            .setBigContentTitle(titleText)
        val launchActivityIntent = Intent(this, MainActivity::class.java)
        val cancelIntent = Intent(this, ForegroundOnlySensorLabelerService::class.java)
        cancelIntent.putExtra(EXTRA_CANCEL_LABELER_FROM_NOTIFICATION, true)

        val servicePendingIntent =
            PendingIntent.getService(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val activityPendingIntent = PendingIntent.getActivity(this, 0, launchActivityIntent, 0)

        val notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
        val notificationBuilder = notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
            .setContentText(mainText)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            .setCategory(CATEGORY_WORKOUT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                R.drawable.ic_cancel,
                getString(R.string.stop_sensor_notification_text),
                servicePendingIntent
            )

        val ongoingActivityStatus = Status.Builder()
            .addTemplate(mainText)
            .build()

        val ongoingActivity = OngoingActivity.Builder(
            applicationContext, NOTIFICATION_ID, notificationBuilder
        )
            .setTouchIntent(activityPendingIntent)
            .setStatus(ongoingActivityStatus)
            .build()

        ongoingActivity.apply(applicationContext)

        return notificationBuilder.build()
    }

    inner class LocalBinder : Binder() {
        internal val sensorLabelerService: ForegroundOnlySensorLabelerService
            get() = this@ForegroundOnlySensorLabelerService
    }

    companion object {
        private const val TAG = "ForegroundOnlyService"

        private const val THREE_SECONDS_MILLISECONDS = 3000L

        private const val PACKAGE_NAME = "com.sensordatalabeler"

        private const val EXTRA_CANCEL_LABELER_FROM_NOTIFICATION =
            "$PACKAGE_NAME.extra.CANCEL_SUBSCRIPTION_FROM_NOTIFICATION"

        private const val NOTIFICATION_ID = 68772672

        private const val NOTIFICATION_CHANNEL_ID = "sensor_labeler_channel_01"
    }
}
package com.sensordatalabeler

import android.app.Application
import com.sensordatalabeler.data.SensorLabelerRepository

/**
 * Main class of the App.
 */
class MainApp: Application() {
    val repository by lazy {
        SensorLabelerRepository.getInstance(applicationContext)
    }
}

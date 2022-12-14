package com.sensordatalabeler

import android.app.Application
import com.sensordatalabeler.data.SensorLabelerRepository

class MainApp: Application() {
    val repository by lazy {
        SensorLabelerRepository.getInstance(applicationContext)
    }
}
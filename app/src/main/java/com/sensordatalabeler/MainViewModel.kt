package com.sensordatalabeler

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.sensordatalabeler.data.SensorLabelerRepository

class MainViewModel(sensorLabelerRepository: SensorLabelerRepository) : ViewModel() {
    val activeSensorLabelerFlow = sensorLabelerRepository.activeSensorLabelerFlow.asLiveData()
    val heartRateFlow = sensorLabelerRepository.heartRateSensorFlow.asLiveData()
    val timeStampFlow = sensorLabelerRepository.timeStampSensorFlow.asLiveData()
    val gyroRateFlow = sensorLabelerRepository.gyroRateSensorFlow.asLiveData()
    val accelerationFlow = sensorLabelerRepository.accelerationRateSensorFlow.asLiveData()
}

class MainViewModelFactory(
    private val sensorLabelerRepository: SensorLabelerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(sensorLabelerRepository) as T
        }
        throw java.lang.IllegalArgumentException("Unknown ViewModel class")
    }
}
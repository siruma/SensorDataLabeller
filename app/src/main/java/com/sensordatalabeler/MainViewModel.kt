package com.sensordatalabeler

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.sensordatalabeler.data.SensorLabelerRepository

class MainViewModel(sensorLabelerRepository: SensorLabelerRepository) : ViewModel() {
    val activeSensorLabelerFlow = sensorLabelerRepository.activeSensorLabelerFlow.asLiveData()
    val heartRateFlow = sensorLabelerRepository.heartRateSensorFlow.asLiveData()
    val timeStampFlow = sensorLabelerRepository.timeStampSensorFlow.asLiveData()
    val stepCounterFlow = sensorLabelerRepository.stepCounterSensorFlow.asLiveData()
    // GYRO RATE
    val gyroXRateFlow = sensorLabelerRepository.gyroXRateSensorFlow.asLiveData()
    val gyroYRateFlow = sensorLabelerRepository.gyroYRateSensorFlow.asLiveData()
    val gyroZRateFlow = sensorLabelerRepository.gyroZRateSensorFlow.asLiveData()

    // ACCELERATION
    val accelerationXFlow = sensorLabelerRepository.accelerationXRateSensorFlow.asLiveData()
    val accelerationYFlow = sensorLabelerRepository.accelerationYRateSensorFlow.asLiveData()
    val accelerationZFlow = sensorLabelerRepository.accelerationZRateSensorFlow.asLiveData()
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
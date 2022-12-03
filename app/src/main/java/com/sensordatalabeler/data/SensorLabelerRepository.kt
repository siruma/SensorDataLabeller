package com.sensordatalabeler.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class SensorLabelerRepository private constructor(
    private val sensorLabelerDataStore: SensorLabelerDataStore
) {
    val activeSensorLabelerFlow: Flow<Boolean> = sensorLabelerDataStore.activeSensorLabelerFlow

    suspend fun setActiveSensorLabeler(activeLabeler: Boolean) =
        sensorLabelerDataStore.setActiveSensorLabeler(activeLabeler)

    val heardBeatSensorPointsFlow: Flow<Int> = sensorLabelerDataStore.heardBeatSensorPointsFlow

    suspend fun setHeardBeatSensorPoints(points: Int) =
        sensorLabelerDataStore.setHeardBeatSensorPoints(points)

    companion object {
        @Volatile private var INSTANCE : SensorLabelerRepository ? = null

        fun getInstance(context: Context): SensorLabelerRepository {
            return INSTANCE ?: synchronized(this){
                INSTANCE ?: SensorLabelerRepository(
                    SensorLabelerDataStore(context))
                    .also { INSTANCE = it }
            }
        }
    }
}
package com.sensordatalabeler.data.db

import java.util.Date

/**
 * Data class for location.
 */
class MyLocationEntity(latitude: Double, longitude: Double, date: Date) {

    private val myLatitude = latitude
    private val myLongitude = longitude
    private val myDate = date

    fun getLatitude() : Double {
        return myLatitude
    }
    fun getLongitude() : Double {
        return myLongitude
    }
    fun getDate() : Long {
        return myDate.time
    }

    fun getString() : String {
        return "($myLatitude, $myLongitude, $myDate)"
    }
}

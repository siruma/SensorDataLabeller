package com.sensordatalabeler


import android.view.InputDevice
import android.view.MotionEvent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LabelerUnitTest {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    // First test
    @Test
    fun startMenu(){
        onView(withId(R.id.start_stop_workout_button))
            .check(matches(isDisplayed()))
        onView(withId(R.id.save_sensor_data_button))
            .check(matches(isDisplayed()))
    }
    @Test
    fun startMeasurement() {
        onView(withId(R.id.start_stop_workout_button))
            .check(matches(isDisplayed()))
        onView(withId(R.id.start_stop_workout_button))
            .check(matches( isClickable()))
            .perform(click(InputDevice.SOURCE_ANY,MotionEvent.BUTTON_PRIMARY))
        onView(withId(R.id.start_stop_workout_button))
            .perform(click(InputDevice.SOURCE_ANY,MotionEvent.BUTTON_PRIMARY))
        onView(withText(STRING_START)).check(matches(isDisplayed()))
    }

    companion object {
        const val STRING_START = "Start Sensor"
        const val STRING_NAME = "Name Data"
    }

}
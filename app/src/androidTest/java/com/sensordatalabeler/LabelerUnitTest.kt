package com.sensordatalabeler


import androidx.test.core.app.ActivityScenario
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
    val rule =  ActivityScenarioRule(MainActivity::class.java)

    // First test
    @Test
    fun startMenu(){
        Thread.sleep(1000) // Temporary delay for debugging (remove in production tests)
        onView(withId(R.id.start_stop_workout_button))
            .check(matches(isDisplayed()))
        onView(withId(R.id.export_sensor_data_button))
            .check(matches(isDisplayed()))
    }
    @Test
    fun startMeasurement() {
        Thread.sleep(1000) // Temporary delay for debugging (remove in production tests)
        // Check that the button is displayed
        onView(withId(R.id.start_stop_workout_button))
            .check(matches(isDisplayed()))

        // Check that the button is clickable and perform a click action
        onView(withId(R.id.start_stop_workout_button))
            .check(matches(isClickable()))
            .perform(click())

        // Perform another click action
        onView(withId(R.id.start_stop_workout_button))
            .perform(click())

        // Assert that the text "Start Sensor" is displayed
        onView(withText(STRING_START)).check(matches(isDisplayed()))
    }

    companion object {
        const val STRING_START = "Start Sensor"
        const val STRING_NAME = "Name Data"
    }

}
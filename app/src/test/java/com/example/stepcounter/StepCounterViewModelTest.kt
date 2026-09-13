package com.example.stepcounter

import org.junit.Assert.assertEquals
import org.junit.Test

class StepCounterViewModelTest {

    private fun freshViewModel(): StepCounterViewModel = StepCounterViewModel()

    @Test
    fun increment_pressesAddUp() {
        val stepCounterViewModel = freshViewModel()
        assertEquals(0, stepCounterViewModel.steps.value)

        stepCounterViewModel.increment()
        stepCounterViewModel.increment()
        stepCounterViewModel.increment()

        assertEquals(3, stepCounterViewModel.steps.value)
    }

    @Test
    fun increment_isRepeatableAcrossInstances() {
        val firstInstance = freshViewModel()
        repeat(3) { firstInstance.increment() }

        val secondInstance = freshViewModel()
        repeat(3) { secondInstance.increment() }

        assertEquals(firstInstance.steps.value, secondInstance.steps.value)
        assertEquals(3, secondInstance.steps.value)
    }

    @Test
    fun reset_returnsToZeroAndNextIncrementStartsFresh() {
        val stepCounterViewModel = freshViewModel()
        repeat(5) { stepCounterViewModel.increment() }
        assertEquals(5, stepCounterViewModel.steps.value)

        stepCounterViewModel.reset()
        assertEquals(0, stepCounterViewModel.steps.value)

        stepCounterViewModel.increment()
        assertEquals(1, stepCounterViewModel.steps.value)
    }

    @Test
    fun sensorBaseline_deltaSinceResetIsShown() {
        val stepCounterViewModel = freshViewModel()

        // Sensor reports a cumulative total since boot; the first reading is the baseline.
        stepCounterViewModel.onSensorEvent(100f)
        assertEquals(0, stepCounterViewModel.steps.value)

        // Device walked 5 steps while the app was in the foreground.
        stepCounterViewModel.onSensorEvent(105f)
        assertEquals(5, stepCounterViewModel.steps.value)

        // Reset re-baselines: count goes back to 0 even though the raw total was 105.
        stepCounterViewModel.reset()
        assertEquals(0, stepCounterViewModel.steps.value)

        stepCounterViewModel.onSensorEvent(110f)
        assertEquals(5, stepCounterViewModel.steps.value)
    }
}
package com.example.stepcounter

import org.junit.Assert.assertEquals
import org.junit.Test

class StepCounterViewModelTest {

    private fun freshViewModel(): StepCounterViewModel = StepCounterViewModel()

    @Test
    fun increment_pressesAddUp() {
        val vm = freshViewModel()
        assertEquals(0, vm.steps.value)

        vm.increment()
        vm.increment()
        vm.increment()

        assertEquals(3, vm.steps.value)
    }

    @Test
    fun increment_isRepeatableAcrossInstances() {
        val first = freshViewModel()
        repeat(3) { first.increment() }

        val second = freshViewModel()
        repeat(3) { second.increment() }

        assertEquals(first.steps.value, second.steps.value)
        assertEquals(3, second.steps.value)
    }

    @Test
    fun reset_returnsToZeroAndNextIncrementStartsFresh() {
        val vm = freshViewModel()
        repeat(5) { vm.increment() }
        assertEquals(5, vm.steps.value)

        vm.reset()
        assertEquals(0, vm.steps.value)

        vm.increment()
        assertEquals(1, vm.steps.value)
    }

    @Test
    fun sensorBaseline_deltaSinceResetIsShown() {
        val vm = freshViewModel()

        // Sensor reports a cumulative total since boot; the first reading is the baseline.
        vm.onSensorEvent(100f)
        assertEquals(0, vm.steps.value)

        // Device walked 5 steps while the app was in the foreground.
        vm.onSensorEvent(105f)
        assertEquals(5, vm.steps.value)

        // Reset re-baselines: count goes back to 0 even though the raw total was 105.
        vm.reset()
        assertEquals(0, vm.steps.value)

        vm.onSensorEvent(110f)
        assertEquals(5, vm.steps.value)
    }
}
package com.example.stepcounter

import org.junit.Assert.assertEquals
import org.junit.Test

class StepCounterLogicTest {

    @Test
    fun computeDisplayCount_addsSensorAndManualSteps() {
        assertEquals(0, StepCounterLogic.computeDisplayCount(0, 0))
        assertEquals(3, StepCounterLogic.computeDisplayCount(0, 3))
        assertEquals(9, StepCounterLogic.computeDisplayCount(7, 2))
        assertEquals(50, StepCounterLogic.computeDisplayCount(50, 0))
    }

    @Test
    fun computeDisplayCount_neverNegative() {
        // Sensor delta may dip below zero for an instant while a new baseline is
        // being established; the displayed count must never go negative.
        assertEquals(0, StepCounterLogic.computeDisplayCount(-5, 0))
        assertEquals(0, StepCounterLogic.computeDisplayCount(-5, -2))
    }

    @Test
    fun computeDisplayCount_isDeterministic() {
        repeat(2) {
            for (sensor in -3..25) {
                for (manual in -3..25) {
                    assertEquals(
                        (sensor + manual).coerceAtLeast(0),
                        StepCounterLogic.computeDisplayCount(sensor, manual),
                    )
                }
            }
        }
    }

    @Test
    fun reachedMilestones_noneBelowFirstMilestone() {
        assertEquals(emptyList<Int>(), StepCounterLogic.reachedMilestones(0))
        assertEquals(emptyList<Int>(), StepCounterLogic.reachedMilestones(9))
        assertEquals(emptyList<Int>(), StepCounterLogic.reachedMilestones(-1))
    }

    @Test
    fun reachedMilestones_boundaries() {
        assertEquals(listOf(10), StepCounterLogic.reachedMilestones(10))
        assertEquals(listOf(10, 20), StepCounterLogic.reachedMilestones(20))
        assertEquals(listOf(10, 20, 50), StepCounterLogic.reachedMilestones(50))
        assertEquals(listOf(10, 20, 50), StepCounterLogic.reachedMilestones(100))
    }

    @Test
    fun reachedMilestones_isDeterministic() {
        repeat(2) {
            for (steps in 0..150) {
                assertEquals(
                    StepCounterLogic.MILESTONES.filter { steps >= it },
                    StepCounterLogic.reachedMilestones(steps),
                )
            }
        }
    }
}
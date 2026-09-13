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
        val milestones = listOf(10, 20, 50)
        assertEquals(emptyList<Int>(), StepCounterLogic.reachedMilestones(0, milestones))
        assertEquals(emptyList<Int>(), StepCounterLogic.reachedMilestones(9, milestones))
        assertEquals(emptyList<Int>(), StepCounterLogic.reachedMilestones(-1, milestones))
    }

    @Test
    fun reachedMilestones_boundaries() {
        val milestones = listOf(10, 20, 50)
        assertEquals(listOf(10), StepCounterLogic.reachedMilestones(10, milestones))
        assertEquals(listOf(10, 20), StepCounterLogic.reachedMilestones(20, milestones))
        assertEquals(listOf(10, 20, 50), StepCounterLogic.reachedMilestones(50, milestones))
        assertEquals(listOf(10, 20, 50), StepCounterLogic.reachedMilestones(100, milestones))
    }

    @Test
    fun reachedMilestones_isDeterministic() {
        val milestones = listOf(10, 20, 50)
        repeat(2) {
            for (steps in 0..150) {
                assertEquals(
                    milestones.filter { steps >= it },
                    StepCounterLogic.reachedMilestones(steps, milestones),
                )
            }
        }
    }

    @Test
    fun roundedSensorSteps_roundsUpHalfwayValues() {
        assertEquals(100, StepCounterLogic.roundedSensorSteps(100.4f))
        assertEquals(100, StepCounterLogic.roundedSensorSteps(99.6f))
    }

    @Test
    fun sensorDeltaSinceBaseline_isZeroUntilBaselineExists() {
        assertEquals(0, StepCounterLogic.sensorDeltaSinceBaseline(105, null))
        assertEquals(5, StepCounterLogic.sensorDeltaSinceBaseline(105, 100))
    }

    @Test
    fun progressFraction_isClampedBetweenZeroAndOne() {
        assertEquals(0f, StepCounterLogic.progressFraction(-10, 100))
        assertEquals(0.5f, StepCounterLogic.progressFraction(50, 100))
        assertEquals(1f, StepCounterLogic.progressFraction(200, 100))
    }

    @Test
    fun progressFraction_withZeroMaximumReturnsOne() {
        // Degenerate case: caller passed a non-positive maximum, so the bar is
        // treated as full rather than dividing by zero.
        assertEquals(1f, StepCounterLogic.progressFraction(10, 0))
    }
}
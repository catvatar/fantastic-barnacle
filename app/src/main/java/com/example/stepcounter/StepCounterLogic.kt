package com.example.stepcounter

import kotlin.math.roundToInt

/**
 * Pure, deterministic derivation rules for the step counter.
 *
 * No Android dependencies here on purpose so every rule can be unit-tested
 * with plain JUnit and no mocking.
 *
 * These functions are "honest": their behavior is fully controlled by their
 * explicit arguments — they never read globals, static state, clocks, or
 * ambient sources. Callers resolve configuration (milestone values, maximum
 * step count) at the dishonest root nodes (MainActivity, ViewModel, the
 * composables) and pass the resolved values in.
 */
object StepCounterLogic {

    /** Displayed count = sensor delta since baseline + manual overrides. Never negative. */
    fun computeDisplayCount(sensorSteps: Int, manualAdjust: Int): Int =
        (sensorSteps + manualAdjust).coerceAtLeast(0)

    /** Rounds a raw cumulative sensor value to a whole step count. */
    fun roundedSensorSteps(rawSensorValue: Float): Int = rawSensorValue.roundToInt()

    /**
     * Steps taken since the baseline was captured.
     *
     * [baselineTotal] is null until the first sensor reading establishes it.
     * Before that, the sensor's cumulative value is not yet meaningful, so the
     * delta is zero.
     */
    fun sensorDeltaSinceBaseline(sensorTotal: Int, baselineTotal: Int?): Int =
        baselineTotal?.let { sensorTotal - it } ?: 0

    /**
     * Fraction of the bar filled, clamped to [0..1].
     *
     * [maximumSteps] must be positive; a non-positive maximum is treated as a
     * full bar rather than dividing by zero (or producing NaN).
     */
    fun progressFraction(steps: Int, maximumSteps: Int): Float =
        if (maximumSteps > 0) {
            (steps.toFloat() / maximumSteps).coerceIn(0f, 1f)
        } else {
            1f
        }

    /** Milestones that have been reached at the given step count. */
    fun reachedMilestones(
        steps: Int,
        milestones: List<Int>,
    ): List<Int> = milestones.filter { steps >= it }
}
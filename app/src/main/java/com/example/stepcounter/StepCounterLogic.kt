package com.example.stepcounter

/**
 * Pure, deterministic derivation rules for the step counter.
 *
 * No Android dependencies here on purpose so every rule can be unit-tested
 * with plain JUnit and no mocking.
 */
object StepCounterLogic {
    const val MAXIMUM_STEPS = 100
    val MILESTONES: List<Int> = listOf(10, 20, 50)

    /** Displayed count = sensor delta since baseline + manual overrides. Never negative. */
    fun computeDisplayCount(sensorSteps: Int, manualAdjust: Int): Int =
        (sensorSteps + manualAdjust).coerceAtLeast(0)

    /** Milestones that have been reached at the given step count. */
    fun reachedMilestones(steps: Int): List<Int> = MILESTONES.filter { steps >= it }
}
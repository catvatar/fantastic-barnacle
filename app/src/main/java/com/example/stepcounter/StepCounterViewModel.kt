package com.example.stepcounter

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Thin state holder for the step counter.
 *
 * The step sensor reports a cumulative total since boot; `sensorBase` is the
 * value captured when counting started, so the shown delta grows only from
 * steps actually taken while the app was in the foreground.
 *
 * All count derivation is routed through [StepCounterLogic] so the UI and the
 * unit tests exercise the exact same rules. The ViewModel is intentionally a
 * "dishonest root": it holds mutable instance state and feeds it into the
 * honest functions rather than duplicating derivation logic.
 */
class StepCounterViewModel : ViewModel() {

    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps.asStateFlow()

    private var sensorTotal = 0
    private var sensorBase: Int? = null
    private var manualAdjust = 0

    /** User tapped +1 Step: counts as a manual step. */
    fun increment() {
        manualAdjust++
        emit()
    }

    /** User tapped Reset: re-baselines the sensor and zeroes manual steps. */
    fun reset() {
        sensorBase = sensorTotal
        manualAdjust = 0
        emit()
    }

    /** Raw cumulative step-sensor reading forwarded from the sensor listener. */
    fun onSensorEvent(rawSensorValue: Float) {
        sensorTotal = StepCounterLogic.roundedSensorSteps(rawSensorValue)
        // The very first reading establishes the baseline, since the counter
        // reports a cumulative total since boot, not since we started.
        if (sensorBase == null) {
            sensorBase = sensorTotal
        }
        emit()
    }

    private fun emit() {
        val display = StepCounterLogic.computeDisplayCount(
            sensorSteps = StepCounterLogic.sensorDeltaSinceBaseline(
                sensorTotal = sensorTotal,
                baselineTotal = sensorBase,
            ),
            manualAdjust = manualAdjust,
        )
        if (display != _steps.value) {
            _steps.value = display
        }
    }
}
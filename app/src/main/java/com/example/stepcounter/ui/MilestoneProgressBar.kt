package com.example.stepcounter.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp

/**
 * A 100-step progress bar with milestone markers.
 *
 * [milestones] positions are mapped proportionally along the track
 * (milestone / [maximumSteps] of the width). A marker turns accent-colored once
 * [steps] reaches it, and a small value label is drawn below bar.
 */
@Composable
fun MilestoneProgressBar(
    steps: Int,
    modifier: Modifier = Modifier,
    maximumSteps: Int = StepCounterDefaults.MaximumSteps,
    milestones: List<Int> = StepCounterDefaults.Milestones,
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val fillColor = MaterialTheme.colorScheme.primary
    val markerActive = MaterialTheme.colorScheme.primary
    val markerInactive = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val activeLabelStyle = MaterialTheme.typography.labelSmall.copy(color = markerActive)
    val inactiveLabelStyle = MaterialTheme.typography.labelSmall.copy(color = labelColor)
    val textMeasurer = rememberTextMeasurer()
    val reached = milestones.filter { steps >= it }
    val fraction = (steps.toFloat() / maximumSteps).coerceIn(0f, 1f)

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
        ) {
            val barHeight = 16.dp.toPx()
            val top = (size.height - barHeight) / 2f
            val track = Size(size.width, barHeight)
            val trackCornerRadius = CornerRadius(barHeight / 2f)

            drawRoundRect(
                color = trackColor,
                topLeft = Offset(0f, top),
                size = track,
                cornerRadius = trackCornerRadius,
            )

            val fillWidth = track.width * fraction
            if (fillWidth > 0f) {
                drawRoundRect(
                    color = fillColor,
                    topLeft = Offset(0f, top),
                    size = Size(fillWidth, barHeight),
                    cornerRadius = trackCornerRadius,
                )
            }

            milestones.forEach { milestone ->
                val x = track.width * milestone / maximumSteps
                val active = reached.contains(milestone)
                val markerColor = if (active) markerActive else markerInactive
                // Notch above the bar.
                drawLine(
                    color = markerColor,
                    start = Offset(x, top),
                    end = Offset(x, top + barHeight),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
        ) {
            milestones.forEach { milestone ->
                val x = size.width * milestone / maximumSteps
                val active = reached.contains(milestone)
                val layout = textMeasurer.measure(
                    text = milestone.toString(),
                    style = if (active) activeLabelStyle else inactiveLabelStyle,
                )
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(
                        x = (x - layout.size.width / 2f).coerceIn(0f, size.width - layout.size.width),
                        y = 0f,
                    ),
                )
            }
        }
    }
}

/** Defaults for [MilestoneProgressBar]; kept as parameters for flexibility. */
object StepCounterDefaults {
    const val MaximumSteps: Int = 100
    val Milestones: List<Int> = listOf(10, 20, 50)
}
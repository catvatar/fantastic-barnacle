package com.example.stepcounter.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.example.stepcounter.StepCounterLogic

/**
 * A progress bar with milestone markers.
 *
 * [milestones] positions are mapped proportionally along the track
 * (milestone / [maximumSteps] of the width). A marker turns accent-colored once
 * [steps] reaches it, and a small value label is drawn below the bar.
 *
 * This composable is a rendering root, so it receives [maximumSteps] and
 * [milestones] explicitly from its caller rather than reading global
 * configuration itself. The geometry it draws is derived by the honest
 * functions in [StepCounterLogic].
 */
@Composable
fun MilestoneProgressBar(
    steps: Int,
    modifier: Modifier = Modifier,
    maximumSteps: Int,
    milestones: List<Int>,
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val fillColor = MaterialTheme.colorScheme.primary
    val markerInactiveColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val activeLabelStyle = MaterialTheme.typography.labelSmall.copy(color = fillColor)
    val inactiveLabelStyle = MaterialTheme.typography.labelSmall.copy(color = labelColor)
    val textMeasurer = rememberTextMeasurer()

    val fraction = StepCounterLogic.progressFraction(
        steps = steps,
        maximumSteps = maximumSteps,
    )
    val reachedMilestones = StepCounterLogic.reachedMilestones(
        steps = steps,
        milestones = milestones,
    )

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
                val markerPosition = track.width * milestone / maximumSteps
                val reached = reachedMilestones.contains(milestone)
                drawLine(
                    color = if (reached) fillColor else markerInactiveColor,
                    start = Offset(markerPosition, top),
                    end = Offset(markerPosition, top + barHeight),
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
                val markerPosition = size.width * milestone / maximumSteps
                val reached = reachedMilestones.contains(milestone)
                val layout = textMeasurer.measure(
                    text = milestone.toString(),
                    style = if (reached) activeLabelStyle else inactiveLabelStyle,
                )
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(
                        x = (markerPosition - layout.size.width / 2f)
                            .coerceIn(0f, size.width - layout.size.width),
                        y = 0f,
                    ),
                )
            }
        }
    }
}
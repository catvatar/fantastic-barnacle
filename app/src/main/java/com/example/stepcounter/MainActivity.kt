package com.example.stepcounter

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stepcounter.ui.MilestoneProgressBar

class MainActivity : ComponentActivity() {

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var viewModel: StepCounterViewModel? = null

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.values?.firstOrNull()?.let { value ->
                viewModel?.onSensorEvent(value)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        setContent {
            val vm: StepCounterViewModel = viewModel()
            viewModel = vm

            val context = LocalContext.current
            var sensorPermissionDenied by remember { mutableStateOf(false) }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
            ) { granted ->
                if (granted) {
                    registerSensorListener()
                } else {
                    unregisterSensorListener()
                    sensorPermissionDenied = true
                }
            }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACTIVITY_RECOGNITION,
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                }
            }

            val hasPermission = hasActivityRecognitionPermission(context)
            val steps by vm.steps.collectAsStateWithLifecycle()

            StepCounterTheme {
                StepCounterScreen(
                    steps = steps,
                    sensorEnabled = stepSensor != null && hasPermission && !sensorPermissionDenied,
                    onIncrement = vm::increment,
                    onReset = vm::reset,
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerSensorListener()
    }

    override fun onStop() {
        unregisterSensorListener()
        super.onStop()
    }

    private fun hasActivityRecognitionPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION,
            ) == PackageManager.PERMISSION_GRANTED

    private fun registerSensorListener() {
        val sensor = stepSensor ?: return
        if (!hasActivityRecognitionPermission(this)) return
        sensorManager?.registerListener(
            sensorListener,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL,
        )
    }

    private fun unregisterSensorListener() {
        sensorManager?.unregisterListener(sensorListener)
    }
}

@Composable
private fun StepCounterScreen(
    steps: Int,
    sensorEnabled: Boolean,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.step_count_label, steps),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(24.dp))

        MilestoneProgressBar(
            steps = steps,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(40.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = onIncrement) {
                Text(stringResource(R.string.increment_step))
            }
            OutlinedButton(onClick = onReset) {
                Text(stringResource(R.string.reset))
            }
        }

        if (!sensorEnabled) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.sensor_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun StepCounterTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}

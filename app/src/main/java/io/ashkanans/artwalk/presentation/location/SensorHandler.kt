package io.ashkanans.artwalk.presentation.location

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.atan2

class SensorHandler(
    private val context: Context,
    private val onSensorUpdate: (Float) -> Unit
) {
    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val magnetometerListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
                val magneticFieldValues = event.values
                val direction = calculateDirection(magneticFieldValues[0], magneticFieldValues[1])
                onSensorUpdate(direction)
            }
        }
    }

    fun startSensorUpdates() {
        magnetometer?.let {
            sensorManager.registerListener(
                magnetometerListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    fun stopSensorUpdates() {
        sensorManager.unregisterListener(magnetometerListener)
    }

    private fun calculateDirection(x: Float, y: Float): Float {
        var direction = Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat()
        if (direction < 0) {
            direction += 360f
        }
        return direction
    }
}

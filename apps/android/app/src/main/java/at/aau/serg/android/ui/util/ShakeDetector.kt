package at.aau.serg.android.ui.util

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener

class ShakeDetector(
    private val onShake: () -> Unit
) : SensorEventListener {

    private var lastTime = 0L
    private val shakeThreshold = 20f
    private val SHAKE_INTERVAL = 800

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val acceleration = kotlin.math.sqrt(x * x + y * y + z * z)

        val now = System.currentTimeMillis()
        if (acceleration > shakeThreshold && now - lastTime > SHAKE_INTERVAL) {
            lastTime = now
            onShake()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}

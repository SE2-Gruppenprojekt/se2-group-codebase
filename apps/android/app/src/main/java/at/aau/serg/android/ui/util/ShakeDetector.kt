package at.aau.serg.android.ui.util

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener

class ShakeDetector(
    private val onShake: () -> Unit
) : SensorEventListener {

    private var lastTime = 0L
    private val shakeThreshold = 20f
    private val shakeInterval = 800

    fun handleValues(x: Float, y: Float, z: Float, now: Long = System.currentTimeMillis()) {
        val acceleration = kotlin.math.sqrt(x * x + y * y + z * z)

        if (acceleration > shakeThreshold && now - lastTime > shakeInterval) {
            lastTime = now
            onShake()
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        handleValues(event.values[0], event.values[1], event.values[2])
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}

package com.example.myfitnessapp.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt


class SensorHandler(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val lightSensor: Sensor?   = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    private val gyroscope: Sensor?     = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)


    var onAccelerationChanged: ((Float) -> Unit)? = null
    var onRawAccelerationChanged: ((Float, Float, Float) -> Unit)? = null
    var onStepDetected: (() -> Unit)? = null
    var onLightChanged: ((Float) -> Unit)? = null
    var onGyroscopeChanged: ((Float, Float, Float) -> Unit)? = null

    var onMovementTypeChanged: ((GyroscopeMovementType) -> Unit)? = null

    var onBrightLightDetected: ((Boolean) -> Unit)? = null

    val hasAccelerometer: Boolean get() = accelerometer != null
    val hasLightSensor: Boolean   get() = lightSensor != null
    val hasGyroscope: Boolean     get() = gyroscope != null


    private var filteredMagnitude = 9.8f
    private val filterFactor = 0.1f
    private var isPeak = false
    private var lastStepTimestamp: Long = 0
    private val stepThreshold = 12.5f
    private val resetThreshold = 10.2f
    private val minTimeBetweenSteps = 280L


    private var filteredGyroX = 0f
    private var filteredGyroY = 0f
    private var filteredGyroZ = 0f
    private val gyroFilterFactor = 0.15f


    private val gyroStillThreshold  = 0.15f
    private val gyroWalkThreshold   = 0.8f
    private val gyroRunThreshold    = 2.2f

    private var lastMovementType = GyroscopeMovementType.STILL


    private val brightLightThreshold = 8000f
    private var isBrightMode = false


    private var isListening = false

    fun startListening() {
        if (isListening) return
        isListening = true
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        if (!isListening) return
        isListening = false
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {

            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                filteredMagnitude += filterFactor * (magnitude - filteredMagnitude)
                onAccelerationChanged?.invoke(filteredMagnitude)
                onRawAccelerationChanged?.invoke(x, y, z)

                val currentTime = System.currentTimeMillis()
                val timeSinceLastStep = currentTime - lastStepTimestamp

                if (filteredMagnitude > stepThreshold && !isPeak) {
                    if (timeSinceLastStep >= minTimeBetweenSteps) {
                        isPeak = true
                        lastStepTimestamp = currentTime
                        onStepDetected?.invoke()
                    }
                } else if (filteredMagnitude < resetThreshold) {
                    isPeak = false
                }
            }

            Sensor.TYPE_LIGHT -> {
                val lux = event.values[0]
                onLightChanged?.invoke(lux)


                val needsBright = lux >= brightLightThreshold
                if (needsBright != isBrightMode) {
                    isBrightMode = needsBright
                    onBrightLightDetected?.invoke(needsBright)
                }
            }

            Sensor.TYPE_GYROSCOPE -> {
                val gx = event.values[0]
                val gy = event.values[1]
                val gz = event.values[2]

                filteredGyroX += gyroFilterFactor * (gx - filteredGyroX)
                filteredGyroY += gyroFilterFactor * (gy - filteredGyroY)
                filteredGyroZ += gyroFilterFactor * (gz - filteredGyroZ)

                onGyroscopeChanged?.invoke(filteredGyroX, filteredGyroY, filteredGyroZ)

                // Clasificar tipo de movimiento segun magnitud angular
                val angMag = sqrt(
                    (filteredGyroX * filteredGyroX +
                     filteredGyroY * filteredGyroY +
                     filteredGyroZ * filteredGyroZ).toDouble()
                ).toFloat()

                val movementType = when {
                    angMag < gyroStillThreshold -> GyroscopeMovementType.STILL
                    angMag < gyroWalkThreshold  -> GyroscopeMovementType.WALKING
                    angMag < gyroRunThreshold   -> GyroscopeMovementType.RUNNING
                    else                        -> GyroscopeMovementType.ROTATING
                }

                if (movementType != lastMovementType) {
                    lastMovementType = movementType
                    onMovementTypeChanged?.invoke(movementType)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

/** Clasificacion del tipo de movimiento detectado por el giroscopio */
enum class GyroscopeMovementType(val label: String, val description: String) {
    STILL    ("Sin movimiento",   "El dispositivo esta en reposo"),
    WALKING  ("Caminando",        "Movimiento suave y ritmico detectado"),
    RUNNING  ("Corriendo",        "Movimiento dinamico de alta cadencia"),
    ROTATING ("Giro pronunciado", "Rotacion rapida del dispositivo")
}
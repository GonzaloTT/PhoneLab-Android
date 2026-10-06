package com.example.phonelab

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs
import kotlin.math.sqrt

class AccelerometerActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private lateinit var tvSensorState: TextView
    private lateinit var tvX: TextView
    private lateinit var tvY: TextView
    private lateinit var tvZ: TextView
    private lateinit var tvMagnitude: TextView
    private lateinit var tvMaxMagnitude: TextView
    private lateinit var tvMovement: TextView
    private lateinit var tvStrongMovementCount: TextView
    private lateinit var tvOrientation: TextView

    private var maxMagnitude = 0f
    private var strongMovementCount = 0
    private var wasStrongMovement = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accelerometer)

        tvSensorState = findViewById(R.id.tvSensorState)
        tvX = findViewById(R.id.tvX)
        tvY = findViewById(R.id.tvY)
        tvZ = findViewById(R.id.tvZ)
        tvMagnitude = findViewById(R.id.tvMagnitude)
        tvMaxMagnitude = findViewById(R.id.tvMaxMagnitude)
        tvMovement = findViewById(R.id.tvMovement)
        tvStrongMovementCount = findViewById(R.id.tvStrongMovementCount)
        tvOrientation = findViewById(R.id.tvOrientation)

        sensorManager = getSystemService(SensorManager::class.java)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer == null) {
            tvSensorState.text = getString(R.string.sensor_unavailable)
        } else {
            tvSensorState.text = "Sensor detectado: ${accelerometer?.name}"
        }
    }

    override fun onResume() {
        super.onResume()

        accelerometer?.let { sensor ->
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {

        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) {
            return
        }

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val magnitude = sqrt(
            x * x +
                    y * y +
                    z * z
        )

        val differenceFromGravity =
            abs(magnitude - SensorManager.GRAVITY_EARTH)

        val movementState = when {
            differenceFromGravity < 1.5f -> {
                "Estable"
            }

            differenceFromGravity < 4.0f -> {
                "Movimiento moderado"
            }

            else -> {
                "Movimiento fuerte"
            }
        }

        val orientationState = when {
            abs(z) > 7f -> {
                if (z > 0) {
                    "Pantalla hacia arriba"
                } else {
                    "Pantalla hacia abajo"
                }
            }

            abs(y) > 7f -> {
                if (y > 0) {
                    "Vertical"
                } else {
                    "Vertical invertido"
                }
            }

            abs(x) > 7f -> {
                "Horizontal / paisaje"
            }

            else -> {
                "Inclinado"
            }
        }

        // RETO A1:
        // Guardar la magnitud máxima registrada.
        if (magnitude > maxMagnitude) {
            maxMagnitude = magnitude
        }

        // RETO A2:
        // Detectar únicamente el inicio de un movimiento fuerte.
        val isStrongMovement =
            movementState == "Movimiento fuerte"

        if (isStrongMovement && !wasStrongMovement) {
            strongMovementCount++
        }

        wasStrongMovement = isStrongMovement

        tvX.text = "X = %.2f m/s²".format(x)
        tvY.text = "Y = %.2f m/s²".format(y)
        tvZ.text = "Z = %.2f m/s²".format(z)

        tvMagnitude.text =
            "Magnitud: %.2f m/s²".format(magnitude)

        tvMaxMagnitude.text =
            "Magnitud máxima: %.2f m/s²".format(maxMagnitude)

        tvMovement.text =
            "Movimiento: $movementState"

        tvStrongMovementCount.text =
            "Movimientos fuertes detectados: $strongMovementCount"

        tvOrientation.text =
            "Orientación: $orientationState"
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {
        // No se requiere una acción para esta práctica.
    }
}
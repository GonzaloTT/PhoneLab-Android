package com.example.phonelab

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.sqrt

class AccelerometerActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private lateinit var database: AppDatabase
    private lateinit var readDao: ReadDao

    private lateinit var tvSensorState: TextView
    private lateinit var tvX: TextView
    private lateinit var tvY: TextView
    private lateinit var tvZ: TextView
    private lateinit var tvMagnitude: TextView
    private lateinit var tvMaxMagnitude: TextView
    private lateinit var tvMovement: TextView
    private lateinit var tvStrongMovementCount: TextView
    private lateinit var tvOrientation: TextView
    private lateinit var tvDatabaseStatus: TextView

    private lateinit var btnSaveReading: MaterialButton
    private lateinit var btnViewReadings: MaterialButton

    private var maxMagnitude = 0f
    private var strongMovementCount = 0
    private var wasStrongMovement = false

    // Última lectura recibida del acelerómetro.
    // Estos valores serán los que se guarden en Room.
    private var currentX = 0f
    private var currentY = 0f
    private var currentZ = 0f
    private var currentMagnitude = 0f

    private var hasSensorReading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accelerometer)

        // Referencias de la interfaz
        tvSensorState = findViewById(R.id.tvSensorState)
        tvX = findViewById(R.id.tvX)
        tvY = findViewById(R.id.tvY)
        tvZ = findViewById(R.id.tvZ)
        tvMagnitude = findViewById(R.id.tvMagnitude)
        tvMaxMagnitude = findViewById(R.id.tvMaxMagnitude)
        tvMovement = findViewById(R.id.tvMovement)
        tvStrongMovementCount = findViewById(R.id.tvStrongMovementCount)
        tvOrientation = findViewById(R.id.tvOrientation)

        tvDatabaseStatus = findViewById(R.id.tvDatabaseStatus)
        btnSaveReading = findViewById(R.id.btnSaveReading)
        btnViewReadings = findViewById(R.id.btnViewReadings)

        // Inicialización de Room
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "phonelab_database"
        ).build()

        readDao = database.readDao()

        // Inicialización del acelerómetro
        sensorManager = getSystemService(SensorManager::class.java)

        accelerometer =
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer == null) {

            tvSensorState.text =
                getString(R.string.sensor_unavailable)

            btnSaveReading.isEnabled = false

        } else {

            tvSensorState.text =
                "Sensor detectado: ${accelerometer?.name}"

            // Hasta recibir la primera lectura no permitimos guardar.
            btnSaveReading.isEnabled = false
        }

        // Guardar la lectura actual en Room
        btnSaveReading.setOnClickListener {

            if (hasSensorReading) {
                saveCurrentReading()
            }
        }

        // Abrir la pantalla del historial
        btnViewReadings.setOnClickListener {

            val intent =
                Intent(
                    this,
                    HistorialActivity::class.java
                )

            startActivity(intent)
        }

        updateSavedReadingsCount()
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

        // Al regresar del historial actualizamos el contador.
        updateSavedReadingsCount()
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

        val magnitude =
            sqrt(
                x * x +
                        y * y +
                        z * z
            )

        val differenceFromGravity =
            abs(
                magnitude -
                        SensorManager.GRAVITY_EARTH
            )

        // Guardamos la lectura más reciente.
        currentX = x
        currentY = y
        currentZ = z
        currentMagnitude = magnitude

        hasSensorReading = true
        btnSaveReading.isEnabled = true

        val movementState = when {

            differenceFromGravity < 1.5f ->
                "Estable"

            differenceFromGravity < 4.0f ->
                "Movimiento moderado"

            else ->
                "Movimiento fuerte"
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

            abs(x) > 7f ->
                "Horizontal / paisaje"

            else ->
                "Inclinado"
        }

        // Reto A1:
        // conservar la magnitud máxima registrada.
        if (magnitude > maxMagnitude) {
            maxMagnitude = magnitude
        }

        // Reto A2:
        // contar únicamente cuando inicia
        // un nuevo movimiento fuerte.
        val isStrongMovement =
            movementState == "Movimiento fuerte"

        if (
            isStrongMovement &&
            !wasStrongMovement
        ) {
            strongMovementCount++
        }

        wasStrongMovement =
            isStrongMovement

        // Actualización de interfaz
        tvX.text =
            "X = %.2f m/s²".format(x)

        tvY.text =
            "Y = %.2f m/s²".format(y)

        tvZ.text =
            "Z = %.2f m/s²".format(z)

        tvMagnitude.text =
            "Magnitud: %.2f m/s²".format(magnitude)

        tvMaxMagnitude.text =
            "Magnitud máxima: %.2f m/s²".format(
                maxMagnitude
            )

        tvMovement.text =
            "Movimiento: $movementState"

        tvStrongMovementCount.text =
            "Movimientos fuertes detectados: $strongMovementCount"

        tvOrientation.text =
            "Orientación: $orientationState"
    }

    private fun saveCurrentReading() {

        val reading = ReadAccelerometer(
            x = currentX,
            y = currentY,
            z = currentZ,
            magnitude = currentMagnitude
        )

        lifecycleScope.launch(Dispatchers.IO) {

            readDao.insert(reading)

            val totalReadings =
                readDao.getAll().size

            withContext(Dispatchers.Main) {

                tvDatabaseStatus.text =
                    "Lectura guardada correctamente. Total: $totalReadings"
            }
        }
    }

    private fun updateSavedReadingsCount() {

        lifecycleScope.launch(Dispatchers.IO) {

            val totalReadings =
                readDao.getAll().size

            withContext(Dispatchers.Main) {

                tvDatabaseStatus.text =
                    if (totalReadings == 0) {

                        "No hay lecturas guardadas."

                    } else {

                        "Lecturas guardadas: $totalReadings"
                    }
            }
        }
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {
        // No se requiere una acción para esta práctica.
    }
}
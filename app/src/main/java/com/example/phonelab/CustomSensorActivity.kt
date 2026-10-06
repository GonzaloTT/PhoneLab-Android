package com.example.phonelab

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class CustomSensorActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepDetector: Sensor? = null

    private lateinit var tvSensorStatus: TextView
    private lateinit var tvStepCount: TextView
    private lateinit var tvActivityState: TextView
    private lateinit var tvProgressInfo: TextView
    private lateinit var progressSteps: ProgressBar
    private lateinit var btnResetSteps: MaterialButton

    private var sessionSteps = 0

    private val demoGoal = 20

    private val activityRecognitionPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                tvSensorStatus.text =
                    "Permiso concedido. El podómetro está listo."

                registerStepSensor()
            } else {
                tvSensorStatus.text =
                    "Se necesita permiso de actividad física para detectar pasos."
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_sensor)

        tvSensorStatus = findViewById(R.id.tvSensorStatus)
        tvStepCount = findViewById(R.id.tvStepCount)
        tvActivityState = findViewById(R.id.tvActivityState)
        tvProgressInfo = findViewById(R.id.tvProgressInfo)
        progressSteps = findViewById(R.id.progressSteps)
        btnResetSteps = findViewById(R.id.btnResetSteps)

        sensorManager = getSystemService(SensorManager::class.java)

        stepDetector =
            sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        if (stepDetector == null) {

            tvSensorStatus.text =
                "Sensor de pasos no disponible en este dispositivo."

            btnResetSteps.isEnabled = false

        } else {

            tvSensorStatus.text =
                "Sensor detectado: ${stepDetector?.name}"

            checkActivityRecognitionPermission()
        }

        btnResetSteps.setOnClickListener {
            sessionSteps = 0
            updateStepInterface()
        }

        updateStepInterface()
    }

    private fun checkActivityRecognitionPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val permissionGranted =
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED

            if (permissionGranted) {

                registerStepSensor()

            } else {

                tvSensorStatus.text =
                    "Autoriza el acceso a actividad física para contar pasos."

                activityRecognitionPermissionLauncher.launch(
                    Manifest.permission.ACTIVITY_RECOGNITION
                )
            }

        } else {

            registerStepSensor()
        }
    }

    private fun registerStepSensor() {

        val sensor = stepDetector ?: return

        sensorManager.registerListener(
            this,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onSensorChanged(event: SensorEvent?) {

        if (event?.sensor?.type != Sensor.TYPE_STEP_DETECTOR) {
            return
        }

        /*
         * TYPE_STEP_DETECTOR genera un evento cada vez
         * que Android identifica un paso.
         *
         * Normalmente event.values[0] contiene 1.0.
         */
        if (event.values.isNotEmpty() && event.values[0] == 1.0f) {

            sessionSteps++

            updateStepInterface()
        }
    }

    private fun updateStepInterface() {

        tvStepCount.text =
            sessionSteps.toString()

        val activityState = when {

            sessionSteps == 0 -> {
                "Sin actividad registrada"
            }

            sessionSteps <= 5 -> {
                "Primeros pasos de la sesión"
            }

            sessionSteps <= 15 -> {
                "Caminata corta detectada"
            }

            sessionSteps < demoGoal -> {
                "Actividad continua"
            }

            else -> {
                "Meta de demostración alcanzada"
            }
        }

        tvActivityState.text =
            activityState

        val progress =
            ((sessionSteps.toFloat() / demoGoal) * 100)
                .toInt()
                .coerceIn(0, 100)

        progressSteps.progress = progress

        tvProgressInfo.text =
            "$sessionSteps de $demoGoal pasos · $progress%"

        if (sessionSteps >= demoGoal) {

            tvProgressInfo.text =
                "$sessionSteps pasos · Meta de demostración completada"
        }
    }

    override fun onResume() {
        super.onResume()

        if (
            stepDetector != null &&
            hasActivityRecognitionPermission()
        ) {
            registerStepSensor()
        }
    }

    override fun onPause() {
        super.onPause()

        sensorManager.unregisterListener(this)
    }

    private fun hasActivityRecognitionPermission(): Boolean {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED

        } else {

            true
        }
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {
        // No se requiere una acción para esta práctica.
    }
}
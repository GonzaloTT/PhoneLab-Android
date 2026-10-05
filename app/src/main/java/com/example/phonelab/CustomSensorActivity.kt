package com.example.phonelab

import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CustomSensorActivity : AppCompatActivity() {

    private lateinit var sensorManager: SensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_sensor)

        val tvAvailableSensors = findViewById<TextView>(R.id.tvAvailableSensors)
        val tvImplementationStatus = findViewById<TextView>(R.id.tvImplementationStatus)

        sensorManager = getSystemService(SensorManager::class.java)

        val availableSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
            .sortedBy { it.name }

        tvAvailableSensors.text = if (availableSensors.isEmpty()) {
            "No se detectaron sensores mediante SensorManager."
        } else {
            availableSensors.joinToString(separator = "\n") { sensor ->
                "• ${sensor.name}  |  tipo ${sensor.type}"
            }
        }

        tvImplementationStatus.text = getString(R.string.implementation_pending)

        // TODO RETO S1 - OBLIGATORIO:
        // 1. Elige un sensor/periférico que NO sea el acelerómetro, linterna o vibración.
        // 2. Rediseña activity_custom_sensor.xml para tu propuesta.
        // 3. Implementa la lectura o el control correspondiente en esta Activity.
        // 4. Interpreta el dato: no basta con mostrar un número crudo.
        //
        // Sugerencias de dificultad media:
        // Sensor.TYPE_LIGHT, Sensor.TYPE_PROXIMITY,
        // Sensor.TYPE_GYROSCOPE, Sensor.TYPE_MAGNETIC_FIELD.
        //
        // Opciones avanzadas (requieren investigar permisos/APIs adicionales):
        // ubicación, micrófono o cámara.
    }
}

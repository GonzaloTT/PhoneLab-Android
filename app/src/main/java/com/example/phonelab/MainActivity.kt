package com.example.phonelab

import android.content.Intent
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<MaterialButton>(R.id.btnAccelerometer).setOnClickListener {
            startActivity(Intent(this, AccelerometerActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnFlashlight).setOnClickListener {
            startActivity(Intent(this, FlashlightActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnVibration).setOnClickListener {
            startActivity(Intent(this, VibrationActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnCustomSensor).setOnClickListener {
            startActivity(Intent(this, CustomSensorActivity::class.java))
        }

        // TODO RETO GENERAL:
        // Personaliza la pantalla principal para reflejar el propósito de tu versión de PhoneLab.
        // El cambio no debe ser únicamente de colores: agrega información o funcionalidad útil.
    }
}

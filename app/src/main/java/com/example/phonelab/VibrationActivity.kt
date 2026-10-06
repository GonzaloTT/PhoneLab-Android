package com.example.phonelab

import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class VibrationActivity : AppCompatActivity() {

    private lateinit var vibrator: Vibrator
    private lateinit var spinnerPatterns: Spinner
    private lateinit var tvVibrationInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vibration)

        vibrator = getSystemService(Vibrator::class.java)
        spinnerPatterns = findViewById(R.id.spinnerPatterns)
        tvVibrationInfo = findViewById(R.id.tvVibrationInfo)

        val btnVibrate = findViewById<MaterialButton>(R.id.btnVibrate)

        if (!vibrator.hasVibrator()) {
            btnVibrate.isEnabled = false
            tvVibrationInfo.text = getString(R.string.vibrator_unavailable)
        }

        btnVibrate.setOnClickListener {
            playSelectedPattern()
        }
    }

    private fun playSelectedPattern() {
        when (spinnerPatterns.selectedItemPosition) {

            0 -> {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        150,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )

                tvVibrationInfo.text =
                    "Patrón ejecutado: vibración corta"
            }

            1 -> {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        700,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )

                tvVibrationInfo.text =
                    "Patrón ejecutado: vibración larga"
            }

            2 -> {
                val pattern = longArrayOf(
                    0,
                    180,
                    120,
                    180
                )

                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        pattern,
                        -1
                    )
                )

                tvVibrationInfo.text =
                    "Patrón ejecutado: doble vibración"
            }

            3 -> {
                // RETO V1:
                // Patrón personalizado de tres pulsos cortos.
                val customPattern = longArrayOf(
                    0,
                    120,
                    100,
                    120,
                    100,
                    120
                )

                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        customPattern,
                        -1
                    )
                )

                tvVibrationInfo.text =
                    "Patrón ejecutado: triple pulso personalizado"
            }
        }
    }

    // RETO V2 (opcional):
    // Permite que el usuario configure algún parámetro del patrón desde la interfaz.
}
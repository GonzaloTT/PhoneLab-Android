package com.example.phonelab

import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class FlashlightActivity : AppCompatActivity() {

    private lateinit var cameraManager: CameraManager
    private var flashCameraId: String? = null
    private var isFlashOn = false
    private var isIntermittentMode = false

    private lateinit var tvFlashStatus: TextView
    private lateinit var tvFlashMessage: TextView
    private lateinit var btnToggleFlash: MaterialButton
    private lateinit var btnIntermittent: MaterialButton

    private val handler = Handler(Looper.getMainLooper())

    private val intermittentRunnable = object : Runnable {
        override fun run() {
            if (!isIntermittentMode) {
                return
            }

            setFlash(!isFlashOn)

            handler.postDelayed(
                this,
                500
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flashlight)

        tvFlashStatus = findViewById(R.id.tvFlashStatus)
        tvFlashMessage = findViewById(R.id.tvFlashMessage)
        btnToggleFlash = findViewById(R.id.btnToggleFlash)
        btnIntermittent = findViewById(R.id.btnIntermittent)

        cameraManager = getSystemService(CameraManager::class.java)
        flashCameraId = findCameraWithFlash()

        if (flashCameraId == null) {
            btnToggleFlash.isEnabled = false
            btnIntermittent.isEnabled = false
            tvFlashMessage.text = getString(R.string.flash_unavailable)
        }

        btnToggleFlash.setOnClickListener {
            stopIntermittentMode()
            setFlash(!isFlashOn)
        }

        btnIntermittent.setOnClickListener {
            if (isIntermittentMode) {
                stopIntermittentMode()
            } else {
                startIntermittentMode()
            }
        }
    }

    private fun findCameraWithFlash(): String? {
        return try {
            cameraManager.cameraIdList.firstOrNull { cameraId ->
                val characteristics =
                    cameraManager.getCameraCharacteristics(cameraId)

                characteristics.get(
                    CameraCharacteristics.FLASH_INFO_AVAILABLE
                ) == true
            }
        } catch (e: CameraAccessException) {
            null
        }
    }

    private fun setFlash(enabled: Boolean) {
        val cameraId = flashCameraId ?: return

        try {
            cameraManager.setTorchMode(
                cameraId,
                enabled
            )

            isFlashOn = enabled

            tvFlashStatus.text =
                if (enabled) {
                    getString(R.string.flash_on)
                } else {
                    getString(R.string.flash_off)
                }

            if (!isIntermittentMode) {
                tvFlashMessage.text =
                    if (enabled) {
                        "La linterna está activa."
                    } else {
                        "La linterna está apagada."
                    }
            }

        } catch (e: CameraAccessException) {
            tvFlashMessage.text =
                "No fue posible acceder al flash: ${e.reason}"

        } catch (e: IllegalArgumentException) {
            tvFlashMessage.text =
                "El flash seleccionado no está disponible."
        }
    }

    private fun startIntermittentMode() {
        isIntermittentMode = true

        btnIntermittent.text =
            "DETENER INTERMITENTE"

        tvFlashMessage.text =
            "Modo intermitente activo."

        handler.post(intermittentRunnable)
    }

    private fun stopIntermittentMode() {
        isIntermittentMode = false

        handler.removeCallbacks(
            intermittentRunnable
        )

        btnIntermittent.text =
            getString(R.string.intermittent_challenge)

        if (isFlashOn) {
            setFlash(false)
        }

        tvFlashMessage.text =
            "Modo intermitente detenido."
    }

    override fun onStop() {
        super.onStop()

        stopIntermittentMode()

        if (isFlashOn) {
            setFlash(false)
        }
    }

    // RETO L2 (opcional):
    // Agrega un modo SOS o permite elegir la velocidad de parpadeo.
}
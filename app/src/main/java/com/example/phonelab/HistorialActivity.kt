package com.example.phonelab

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistorialActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var readDao: ReadDao

    private lateinit var tvTotalReadings: TextView
    private lateinit var tvEmptyHistory: TextView
    private lateinit var historyContainer: LinearLayout
    private lateinit var btnDeleteHistory: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        tvTotalReadings = findViewById(R.id.tvTotalReadings)
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory)
        historyContainer = findViewById(R.id.historyContainer)
        btnDeleteHistory = findViewById(R.id.btnDeleteHistory)

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "phonelab_database"
        ).build()

        readDao = database.readDao()

        btnDeleteHistory.setOnClickListener {
            deleteHistory()
        }

        loadHistory()
    }

    private fun loadHistory() {

        lifecycleScope.launch(Dispatchers.IO) {

            val readings = readDao.getAll()

            withContext(Dispatchers.Main) {

                historyContainer.removeAllViews()

                tvTotalReadings.text =
                    "Lecturas guardadas: ${readings.size}"

                if (readings.isEmpty()) {

                    tvEmptyHistory.visibility = View.VISIBLE
                    btnDeleteHistory.isEnabled = false

                } else {

                    tvEmptyHistory.visibility = View.GONE
                    btnDeleteHistory.isEnabled = true

                    readings.forEach { reading ->
                        addReadingCard(reading)
                    }
                }
            }
        }
    }

    private fun addReadingCard(reading: ReadAccelerometer) {

        val card = MaterialCardView(this).apply {

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(10)
            }

            radius = dpToPx(18).toFloat()

            setContentPadding(
                dpToPx(18),
                dpToPx(18),
                dpToPx(18),
                dpToPx(18)
            )
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val title = TextView(this).apply {
            text = "Lectura #${reading.id}"
            textSize = 18f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        val values = TextView(this).apply {

            text = """
                X: %.2f m/s²
                Y: %.2f m/s²
                Z: %.2f m/s²
                Magnitud: %.2f m/s²
            """.trimIndent().format(
                reading.x,
                reading.y,
                reading.z,
                reading.magnitude
            )

            textSize = 16f

            setPadding(
                0,
                dpToPx(8),
                0,
                0
            )
        }

        val date = TextView(this).apply {

            text = formatTimestamp(reading.timestamp)
            textSize = 14f

            setPadding(
                0,
                dpToPx(8),
                0,
                0
            )
        }

        content.addView(title)
        content.addView(values)
        content.addView(date)

        card.addView(content)

        historyContainer.addView(card)
    }

    private fun deleteHistory() {

        lifecycleScope.launch(Dispatchers.IO) {

            readDao.deleteAll()

            withContext(Dispatchers.Main) {
                loadHistory()
            }
        }
    }

    private fun formatTimestamp(timestamp: Long): String {

        val formatter = SimpleDateFormat(
            "dd/MM/yyyy HH:mm:ss",
            Locale.getDefault()
        )

        return "Fecha: ${formatter.format(Date(timestamp))}"
    }

    private fun dpToPx(dp: Int): Int {

        return (
                dp * resources.displayMetrics.density
                ).toInt()
    }
}
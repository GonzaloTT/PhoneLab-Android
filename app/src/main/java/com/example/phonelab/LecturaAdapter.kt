package com.example.phonelab

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LecturaAdapter :
    RecyclerView.Adapter<LecturaAdapter.LecturaViewHolder>() {

    private val lecturas =
        mutableListOf<ReadAccelerometer>()

    fun actualizar(
        nuevasLecturas: List<ReadAccelerometer>
    ) {
        lecturas.clear()
        lecturas.addAll(nuevasLecturas)

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LecturaViewHolder {

        val view =
            LayoutInflater
                .from(parent.context)
                .inflate(
                    R.layout.item_lectura,
                    parent,
                    false
                )

        return LecturaViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: LecturaViewHolder,
        position: Int
    ) {
        holder.bind(
            lecturas[position]
        )
    }

    override fun getItemCount(): Int {
        return lecturas.size
    }

    class LecturaViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val txtTituloLectura:
                TextView =
            itemView.findViewById(
                R.id.txtTituloLectura
            )

        private val txtValoresLectura:
                TextView =
            itemView.findViewById(
                R.id.txtValoresLectura
            )

        private val txtFechaLectura:
                TextView =
            itemView.findViewById(
                R.id.txtFechaLectura
            )

        private val txtSincronizacion:
                TextView =
            itemView.findViewById(
                R.id.txtSincronizacion
            )

        fun bind(
            lectura: ReadAccelerometer
        ) {

            txtTituloLectura.text =
                "Lectura #${lectura.id}"

            txtValoresLectura.text =
                """
                X: %.2f m/s²
                Y: %.2f m/s²
                Z: %.2f m/s²
                Magnitud: %.2f m/s²
                """.trimIndent()
                    .format(
                        lectura.x,
                        lectura.y,
                        lectura.z,
                        lectura.magnitude
                    )

            val formatter =
                SimpleDateFormat(
                    "dd/MM/yyyy HH:mm:ss",
                    Locale.getDefault()
                )

            txtFechaLectura.text =
                "Fecha: ${
                    formatter.format(
                        Date(lectura.timestamp)
                    )
                }"

            txtSincronizacion.text =
                if (lectura.sincronizada) {

                    "Sincronizada con Firebase"

                } else {

                    "Pendiente de sincronización"
                }
        }
    }
}
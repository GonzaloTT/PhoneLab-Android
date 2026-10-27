package com.example.phonelab

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistorialActivity : AppCompatActivity() {

    /*
     * Room
     *
     * Utilizamos la instancia única definida en AppDatabase
     * y obtenemos el DAO para consultar las lecturas locales.
     */
    private val readDao by lazy {
        AppDatabase
            .getInstance(applicationContext)
            .readDao()
    }

    /*
     * Firebase Firestore:
     * almacena las lecturas sincronizadas en la nube.
     */
    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    /*
     * Firebase Authentication:
     * identifica este dispositivo mediante
     * autenticación anónima.
     */
    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    /*
     * SharedPreferences:
     * conserva el nombre asignado al equipo.
     */
    private val preferences by lazy {
        getSharedPreferences(
            "iot_equipo",
            MODE_PRIVATE
        )
    }

    private val adapter =
        LecturaAdapter()

    private lateinit var txtResumen: TextView
    private lateinit var txtEstadoNube: TextView
    private lateinit var txtUltimasNube: TextView
    private lateinit var etEquipo: EditText

    private lateinit var btnNube: Button
    private lateinit var btnConsultarNube: Button

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_historial
        )

        txtResumen =
            findViewById(
                R.id.txtResumen
            )

        txtEstadoNube =
            findViewById(
                R.id.txtEstadoNube
            )

        txtUltimasNube =
            findViewById(
                R.id.txtUltimasNube
            )

        etEquipo =
            findViewById(
                R.id.etEquipo
            )

        btnNube =
            findViewById(
                R.id.btnNube
            )

        btnConsultarNube =
            findViewById(
                R.id.btnConsultarNube
            )

        val btnActualizar =
            findViewById<Button>(
                R.id.btnActualizar
            )

        val btnBorrar =
            findViewById<Button>(
                R.id.btnBorrar
            )

        val lista =
            findViewById<RecyclerView>(
                R.id.listaLecturas
            )

        /*
         * Recuperamos el nombre del equipo guardado.
         * Si todavía no existe, usamos Equipo_01.
         */
        etEquipo.setText(
            preferences.getString(
                "nombre",
                "Equipo_01"
            )
        )

        /*
         * Configuración del RecyclerView.
         */
        lista.layoutManager =
            LinearLayoutManager(this)

        lista.adapter =
            adapter

        /*
         * Botón para volver a consultar Room.
         */
        btnActualizar.setOnClickListener {
            cargarHistorial()
        }

        /*
         * Elimina únicamente el historial local.
         * Firebase no se modifica.
         */
        btnBorrar.setOnClickListener {
            confirmarBorrado()
        }

        /*
         * Envía a Firebase las lecturas
         * todavía no sincronizadas.
         */
        btnNube.setOnClickListener {
            sincronizarConFirebase()
        }

        /*
         * Consulta las últimas tres lecturas
         * almacenadas en Firestore.
         */
        btnConsultarNube.setOnClickListener {
            consultarNube()
        }

        mostrarIdentidad()
    }

    override fun onResume() {
        super.onResume()

        cargarHistorial()
    }

    /*
     * Muestra el UID asignado por Firebase Authentication.
     */
    private fun mostrarIdentidad() {

        txtEstadoNube.text =
            auth.currentUser?.let { user ->

                "UID del teléfono: ${user.uid}"

            } ?: (
                    "UID del teléfono: " +
                            "se creará en la primera sincronización"
                    )
    }

    /*
     * Consulta Room y actualiza el RecyclerView.
     */
    private fun cargarHistorial() {

        lifecycleScope.launch {

            try {

                val result =
                    withContext(
                        Dispatchers.IO
                    ) {

                        Triple(
                            readDao.getAll(),
                            readDao.countAll(),
                            readDao.countPending()
                        )
                    }

                val readings =
                    result.first

                val total =
                    result.second

                val pending =
                    result.third

                adapter.actualizar(
                    readings
                )

                txtResumen.text =
                    "Mostrando ${readings.size} de $total | " +
                            "Pendientes: $pending"

            } catch (e: Exception) {

                Log.e(
                    "FirebaseRoom",
                    "Error consultando Room",
                    e
                )

                txtResumen.text =
                    "Error al consultar Room: " +
                            e.localizedMessage
            }
        }
    }

    /*
     * Sincroniza hasta 25 registros pendientes.
     */
    private fun sincronizarConFirebase() {

        val teamName =
            etEquipo.text
                .toString()
                .trim()

        if (
            teamName.isEmpty() ||
            teamName.length > 40
        ) {

            etEquipo.error =
                "Escribe un nombre de equipo " +
                        "(máximo 40 caracteres)"

            return
        }

        /*
         * Guardamos el nombre del dispositivo
         * para reutilizarlo posteriormente.
         */
        preferences
            .edit()
            .putString(
                "nombre",
                teamName
            )
            .apply()

        btnNube.isEnabled =
            false

        lifecycleScope.launch {

            try {

                txtEstadoNube.text =
                    "Autenticando y preparando el envío..."

                /*
                 * Si Firebase todavía no tiene usuario,
                 * iniciamos una sesión anónima.
                 */
                val user =
                    auth.currentUser
                        ?: auth
                            .signInAnonymously()
                            .await()
                            .user
                        ?: error(
                            "Firebase no devolvió un usuario"
                        )

                mostrarIdentidad()

                /*
                 * Consultamos únicamente registros
                 * que todavía no se han sincronizado.
                 *
                 * Máximo 25 por sincronización.
                 */
                val pendingReadings =
                    withContext(
                        Dispatchers.IO
                    ) {

                        readDao.getPending(
                            25
                        )
                    }

                if (
                    pendingReadings.isEmpty()
                ) {

                    Toast.makeText(
                        this@HistorialActivity,
                        "No hay lecturas pendientes",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@launch
                }

                /*
                 * Cada dispositivo queda identificado
                 * mediante su UID de Firebase.
                 *
                 * dispositivos/{UID}
                 */
                val deviceRef =
                    firestore
                        .collection(
                            "dispositivos"
                        )
                        .document(
                            user.uid
                        )

                /*
                 * Utilizamos un WriteBatch para enviar
                 * todas las lecturas de forma conjunta.
                 */
                val batch =
                    firestore.batch()

                /*
                 * Información general del dispositivo.
                 */
                batch.set(
                    deviceRef,
                    mapOf(
                        "equipo" to teamName,

                        "uid" to user.uid,

                        "ultimaSincronizacion" to
                                FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )

                /*
                 * Cada lectura se guarda en:
                 *
                 * dispositivos/{UID}/mediciones/{ID}
                 *
                 * Utilizamos acc_ID como ID estable.
                 * Así evitamos duplicar registros
                 * si se repite una sincronización.
                 */
                pendingReadings.forEach { reading ->

                    val document =
                        deviceRef
                            .collection(
                                "mediciones"
                            )
                            .document(
                                "acc_${reading.id}"
                            )

                    batch.set(
                        document,
                        mapOf(
                            "origen" to
                                    "Android",

                            "sensor" to
                                    "Acelerómetro",

                            "timestampMs" to
                                    reading.timestamp,

                            "valores" to
                                    mapOf(
                                        "x" to
                                                reading.x
                                                    .toDouble(),

                                        "y" to
                                                reading.y
                                                    .toDouble(),

                                        "z" to
                                                reading.z
                                                    .toDouble(),

                                        "magnitud" to
                                                reading.magnitude
                                                    .toDouble()
                                    ),

                            "recibidoEn" to
                                    FieldValue.serverTimestamp()
                        )
                    )
                }

                txtEstadoNube.text =
                    "Enviando ${pendingReadings.size} lecturas..."

                /*
                 * await() espera hasta que Firebase
                 * confirme correctamente el lote.
                 */
                batch.commit()
                    .await()

                /*
                 * Solo después de que Firebase confirma,
                 * marcamos los registros locales
                 * como sincronizados.
                 */
                withContext(
                    Dispatchers.IO
                ) {

                    readDao.markAsSynced(
                        pendingReadings.map {
                            it.id
                        }
                    )
                }

                Toast.makeText(
                    this@HistorialActivity,
                    "${pendingReadings.size} lecturas sincronizadas",
                    Toast.LENGTH_LONG
                ).show()

                mostrarIdentidad()

                cargarHistorial()

            } catch (e: Exception) {

                Log.e(
                    "FirebaseRoom",
                    "Error de sincronización",
                    e
                )

                txtEstadoNube.text =
                    "Error: ${e.localizedMessage}"

                Toast.makeText(
                    this@HistorialActivity,
                    "No se completó la sincronización. " +
                            "Revisa internet y Firebase.",
                    Toast.LENGTH_LONG
                ).show()

                cargarHistorial()

            } finally {

                btnNube.isEnabled =
                    true
            }
        }
    }

    /*
     * Consulta directamente Firestore para comprobar
     * que los datos realmente llegaron al servidor.
     */
    private fun consultarNube() {

        val user =
            auth.currentUser

        if (user == null) {

            Toast.makeText(
                this,
                "Primero sincroniza para autenticar el teléfono",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        btnConsultarNube.isEnabled =
            false

        lifecycleScope.launch {

            try {

                val records =
                    firestore
                        .collection(
                            "dispositivos"
                        )
                        .document(
                            user.uid
                        )
                        .collection(
                            "mediciones"
                        )
                        .orderBy(
                            "timestampMs",
                            Query.Direction.DESCENDING
                        )
                        .limit(3)
                        .get(
                            Source.SERVER
                        )
                        .await()

                if (records.isEmpty) {

                    txtUltimasNube.text =
                        "La nube no tiene mediciones " +
                                "para este UID."

                } else {

                    val formatter =
                        SimpleDateFormat(
                            "dd/MM HH:mm:ss",
                            Locale.getDefault()
                        )

                    val lines =
                        records.documents.map { document ->

                            val timestamp =
                                document.getLong(
                                    "timestampMs"
                                ) ?: 0L

                            val sensor =
                                document.getString(
                                    "sensor"
                                ) ?: "Sensor"

                            "${document.id} | " +
                                    "${
                                        formatter.format(
                                            Date(timestamp)
                                        )
                                    } | " +
                                    sensor
                        }

                    txtUltimasNube.text =
                        "Últimas lecturas en Firebase:\n" +
                                lines.joinToString(
                                    "\n"
                                )
                }

            } catch (e: Exception) {

                Log.e(
                    "FirebaseRoom",
                    "Error consultando Firestore",
                    e
                )

                txtUltimasNube.text =
                    "No se pudo consultar la nube: " +
                            e.localizedMessage

            } finally {

                btnConsultarNube.isEnabled =
                    true
            }
        }
    }

    /*
     * Confirma antes de eliminar los registros locales.
     *
     * Importante:
     * borrar Room NO borra Firebase.
     */
    private fun confirmarBorrado() {

        AlertDialog.Builder(this)
            .setTitle(
                "Borrar historial local"
            )
            .setMessage(
                "Se borrarán incluso las lecturas " +
                        "pendientes de sincronización. " +
                        "Los documentos que ya están en " +
                        "Firebase NO se borrarán. " +
                        "¿Continuar?"
            )
            .setPositiveButton(
                "Eliminar"
            ) { _, _ ->

                lifecycleScope.launch {

                    withContext(
                        Dispatchers.IO
                    ) {

                        readDao.deleteAll()
                    }

                    cargarHistorial()
                }
            }
            .setNegativeButton(
                "Cancelar",
                null
            )
            .show()
    }
}
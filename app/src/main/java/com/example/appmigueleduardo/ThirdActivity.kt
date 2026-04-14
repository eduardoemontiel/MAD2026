package com.example.appmigueleduardo

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appmigueleduardo.room.AppDatabase
import com.example.appmigueleduardo.room.CoordinatesEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ThirdActivity : AppCompatActivity() {
    private val TAG = "btaThirdActivity"

    // Referencias a los elementos de la interfaz
    private lateinit var etTimestamp: EditText
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText
    private lateinit var etAltitude: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)

        // 1. Vincular con los IDs del XML (Asegúrate de que coincidan con tu activity_third.xml)
        etTimestamp = findViewById(R.id.etTimestamp)
        etLatitude = findViewById(R.id.etLatitude)
        etLongitude = findViewById(R.id.etLongitude)
        etAltitude = findViewById(R.id.etAltitude)

        // TextViews que ya tenías (opcional mantenerlos o usar solo los EditText)
        val tvLat: TextView = findViewById(R.id.tvDetailLat)
        val tvLon: TextView = findViewById(R.id.tvDetailLon)
        val tvAlt: TextView = findViewById(R.id.tvDetailAlt)

        // 2. Recibir los datos enviados desde SecondActivity
        val timestamp = intent.getStringExtra("timestamp") ?: "0"
        val latitude = intent.getStringExtra("latitude") ?: "0.0"
        val longitude = intent.getStringExtra("longitude") ?: "0.0"
        val altitude = intent.getStringExtra("altitude") ?: "0.0"

        // 3. Mostrar los datos en los campos
        Log.d(TAG, "Datos recibidos -> Lat: $latitude, Lon: $longitude, Alt: $altitude")

        etTimestamp.setText(timestamp)
        etLatitude.setText(latitude)
        etLongitude.setText(longitude)
        etAltitude.setText(altitude)

        // Mantener la visualización en los TextViews que ya tenías
        tvLat.text = "Latitud: $latitude"
        tvLon.text = "Longitud: $longitude"
        tvAlt.text = "Altitud: $altitude"

        // 4. Configurar botones de acción

        // Botón Actualizar
        val updateButton: Button = findViewById(R.id.buttonUpdate)
        updateButton.setOnClickListener {
            showUpdateConfirmationDialog()
        }

        // Botón Eliminar
        val deleteButton: Button = findViewById(R.id.buttonDelete)
        deleteButton.setOnClickListener {
            val ts = etTimestamp.text.toString().toLongOrNull()
            if (ts != null) {
                showDeleteConfirmationDialog(ts)
            } else {
                Toast.makeText(this, "Timestamp inválido", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón Volver
        val btnBack: Button = findViewById(R.id.btnBackToList)
        btnBack.setOnClickListener {
            finish()
        }
    }

    // --- LÓGICA DE ACTUALIZACIÓN ---

    private fun showUpdateConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Update")
            .setMessage("¿Estás seguro de que quieres actualizar esta coordenada?")
            .setPositiveButton("Update") { _, _ ->
                updateCoordinate()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateCoordinate() {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val timestamp = etTimestamp.text.toString().toLong()
                val updatedCoordinate = CoordinatesEntity(
                    timestamp = timestamp,
                    latitude = etLatitude.text.toString().toDouble(),
                    longitude = etLongitude.text.toString().toDouble(),
                    altitude = etAltitude.text.toString().toDouble()
                )

                db.coordinatesDao().updateCoordinate(updatedCoordinate)
                Log.d(TAG, "Coordinate updated: $updatedCoordinate")

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ThirdActivity, "Actualizado correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ThirdActivity, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // --- LÓGICA DE ELIMINACIÓN ---

    private fun showDeleteConfirmationDialog(timestamp: Long) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Delete")
            .setMessage("¿Estás seguro de que quieres eliminar esta coordenada?")
            .setPositiveButton("Delete") { _, _ ->
                deleteCoordinate(timestamp)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteCoordinate(timestamp: Long) {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            db.coordinatesDao().deleteWithTimestamp(timestamp)
            Log.d(TAG, "Coordinate with timestamp $timestamp deleted.")

            withContext(Dispatchers.Main) {
                Toast.makeText(this@ThirdActivity, "Eliminado correctamente", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
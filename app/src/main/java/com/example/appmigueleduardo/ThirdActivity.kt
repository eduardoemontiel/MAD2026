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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ThirdActivity : AppCompatActivity() {
    private val TAG = "btaThirdActivity"
    private lateinit var etTimestamp: EditText
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText
    private lateinit var etAltitude: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)
        etTimestamp = findViewById(R.id.etTimestamp)
        etLatitude = findViewById(R.id.etLatitude)
        etLongitude = findViewById(R.id.etLongitude)
        etAltitude = findViewById(R.id.etAltitude)
        val tvLat: TextView = findViewById(R.id.tvDetailLat)
        val tvLon: TextView = findViewById(R.id.tvDetailLon)
        val tvAlt: TextView = findViewById(R.id.tvDetailAlt)
        val timestamp = intent.getStringExtra("timestamp") ?: "0"
        val latitude = intent.getStringExtra("latitude") ?: "0.0"
        val longitude = intent.getStringExtra("longitude") ?: "0.0"
        val altitude = intent.getStringExtra("altitude") ?: "0.0"
        Log.d(TAG, "Datos recibidos -> Lat: $latitude, Lon: $longitude, Alt: $altitude")
        etTimestamp.setText(timestamp)
        etLatitude.setText(latitude)
        etLongitude.setText(longitude)
        etAltitude.setText(altitude)
        tvLat.text = "Latitud: $latitude"
        tvLon.text = "Longitud: $longitude"
        tvAlt.text = "Altitud: $altitude"
        val updateButton: Button = findViewById(R.id.buttonUpdate)
        updateButton.setOnClickListener {
            showUpdateConfirmationDialog()
        }
        val deleteButton: Button = findViewById(R.id.buttonDelete)
        deleteButton.setOnClickListener {
            val ts = etTimestamp.text.toString().toLongOrNull()
            if (ts != null) {
                showDeleteConfirmationDialog(ts)
            } else {
                Toast.makeText(this, "Timestamp inválido", Toast.LENGTH_SHORT).show()
            }
        }
        val btnBack: Button = findViewById(R.id.btnBackToList)
        btnBack.setOnClickListener {
            finish()
        }
        val addReportButton: Button = findViewById(R.id.addReportButton)
        addReportButton.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            val userId = user?.uid
            if (userId != null) {
                val tsValue = etTimestamp.text.toString().toLongOrNull() ?: System.currentTimeMillis()
                val latValue = etLatitude.text.toString().toDoubleOrNull() ?: 0.0
                val lonValue = etLongitude.text.toString().toDoubleOrNull() ?: 0.0
                val report = mapOf(
                    "userId" to userId,
                "timestamp" to tsValue,
                "report" to "Reporte de Miguel en: ${etLatitude.text}, ${etLongitude.text}",
                "latitude" to latValue,
                "longitude" to lonValue
                )
                addReportToDatabase(report)
            } else {
                Toast.makeText(this, "Debes iniciar sesión primero", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addReportToDatabase(report: Map<String, Any>) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("hotspots").push()
        databaseReference.setValue(report)
            .addOnSuccessListener {
                Toast.makeText(this, "Reporte subido a Firebase con éxito", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al subir: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

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
                    Toast.makeText(this@ThirdActivity, "Actualizado en local", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ThirdActivity, "Error al actualizar local", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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
                Toast.makeText(this@ThirdActivity, "Eliminado de local", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
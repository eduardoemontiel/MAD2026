package com.example.appmigueleduardo

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity

class ThirdActivity : ComponentActivity() {
    private val TAG = "btaThirdActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)

        // 1. Recibir los datos enviados desde SecondActivity
        val latitude = intent.getStringExtra("latitude") ?: "0.0"
        val longitude = intent.getStringExtra("longitude") ?: "0.0"
        val altitude = intent.getStringExtra("altitude") ?: "0.0"

        // 2. Log de verificación según el snippet
        Log.d(TAG, "Datos recibidos -> Lat: $latitude, Lon: $longitude, Alt: $altitude")

        // 3. Vincular con los TextView del XML
        val tvLat: TextView = findViewById(R.id.tvDetailLat)
        val tvLon: TextView = findViewById(R.id.tvDetailLon)
        val tvAlt: TextView = findViewById(R.id.tvDetailAlt)

        // 4. Mostrar los datos en la pantalla
        tvLat.text = "Latitud: $latitude"
        tvLon.text = "Longitud: $longitude"
        tvAlt.text = "Altitud: $altitude"

        // Botón para volver [cite: 270-275]
        val btnBack: Button = findViewById(R.id.btnBackToList)
        btnBack.setOnClickListener {
            finish()
        }
    }
}
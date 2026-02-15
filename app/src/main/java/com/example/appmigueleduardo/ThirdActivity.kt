package com.example.appmigueleduardo

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ThirdActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)

        // Botón para volver al Nivel 2 (Lista)
        val btnBack: Button = findViewById(R.id.btnBackToList)
        btnBack.setOnClickListener {
            finish() // Cierra el detalle y vuelve a la lista
        }
    }
}
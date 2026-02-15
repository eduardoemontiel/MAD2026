package com.example.appmigueleduardo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SecondActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        // Botón para ir al Nivel 3 (Detalle)
        val btnDetail: Button = findViewById(R.id.btnItem1)
        btnDetail.setOnClickListener {
            val intent = Intent(this, ThirdActivity::class.java)
            startActivity(intent)
        }

        // Botón para volver al Nivel 1 (Landing)
        val btnBack: Button = findViewById(R.id.btnBackToMain)
        btnBack.setOnClickListener {
            finish() // Cierra esta actividad y vuelve a la anterior
        }
    }
}
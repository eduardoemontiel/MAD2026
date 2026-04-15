package com.example.appmigueleduardo

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val editTextUserIdentifier: EditText = findViewById(R.id.editTextUserIdentifier)
        val editTextApiKey: EditText = findViewById(R.id.editTextApiKey)
        val buttonSave: Button = findViewById(R.id.buttonSave)

        // Cargamos las SharedPreferences (AppPreferences es el nombre que usamos en todo el proyecto)
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        // Rellenar los campos con lo que ya esté guardado (si existe)
        val userIdentifier = sharedPreferences.getString("userIdentifier", "")
        val apiKey = sharedPreferences.getString("API_KEY", "")

        editTextUserIdentifier.setText(userIdentifier)
        editTextApiKey.setText(apiKey)

        buttonSave.setOnClickListener {
            val newUserIdentifier = editTextUserIdentifier.text.toString()
            val newApiKey = editTextApiKey.text.toString()

            if (newUserIdentifier.isNotBlank()) {
                sharedPreferences.edit().apply {
                    putString("userIdentifier", newUserIdentifier)
                    putString("API_KEY", newApiKey)
                    apply() // Guarda los cambios
                }
                Toast.makeText(this, "Ajustes guardados: $newUserIdentifier", Toast.LENGTH_LONG).show()
                finish() // Cierra la pantalla y vuelve a la anterior
            } else {
                Toast.makeText(this, "El User ID no puede estar vacío", Toast.LENGTH_LONG).show()
            }
        }
    }
}
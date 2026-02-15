package com.example.appmigueleduardo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.appmigueleduardo.ui.theme.AppMiguelEduardoTheme

import android.util.Log
import android.widget.Button

class MainActivity : ComponentActivity() {
    private val TAG = "WeeklyReleaseApp"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Conecta con tu archivo XML
        setContentView(R.layout.activity_main)

        // Esto aparecerá en la pestaña Logcat de abajo
        Log.d(TAG, "onCreate: Landing Page cargada")

        // Dentro de onCreate, después de setContentView
        val buttonNext: Button = findViewById(R.id.btnGoToList)

        buttonNext.setOnClickListener {
            // Esto crea la navegación hacia la siguiente pantalla
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)

            Log.d(TAG, "Botón presionado: Navegando a la lista")
        }
    }
}
package com.example.appmigueleduardo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/*class SecondActivity : ComponentActivity() {
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
} esto sería si usamos xml como en el resto de pantallas*/

// Así sería con jetpack compose:

class SecondActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SecondScreenContent(
                onNavigateToThird = {
                    val intent = Intent(this, ThirdActivity::class.java)
                    startActivity(intent)
                },
                // 'finish()' cierra esta Activity y nos devuelve a la MainActivity que está debajo en la pila
                onBack = { finish() }
            )
        }
    }
}

// @Composable: Marca una función que 'dibuja' UI. Es como el sustituto del archivo XML
@Composable
fun SecondScreenContent(onNavigateToThird: () -> Unit, onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onNavigateToThird,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "GO TO THIRD ACTIVITY", color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text(text = "GO TO MAIN ACTIVITY", color = Color.White)
            }
        }
    }
}
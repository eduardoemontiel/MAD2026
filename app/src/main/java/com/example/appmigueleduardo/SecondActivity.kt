package com.example.appmigueleduardo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.io.IOException

class SecondActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var fileContent by remember { mutableStateOf("Cargando historial...") }

            LaunchedEffect(Unit) {
                fileContent = readFileContents()
            }

            SecondScreenContent(
                fileContent = fileContent,
                onNavigateToThird = {
                    val intent = Intent(this, ThirdActivity::class.java)
                    startActivity(intent)
                },
                onBack = { finish() }
            )
        }
    }

    private fun readFileContents(): String {
        val fileName = "gps_coordinates.csv"
        return try {
            openFileInput(fileName).bufferedReader().use { reader ->
                reader.readText() // Lee todo el contenido del archivo
            }
        } catch (e: IOException) {
            "No hay historial de coordenadas todavía."
        }
    }
}

@Composable
fun SecondScreenContent(fileContent: String, onNavigateToThird: () -> Unit, onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            // Esto centra todo el grupo de elementos verticalmente en la pantalla
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "HISTORIAL DE RUTAS",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp),
                color = Color.Black
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = fileContent,
                    color = Color.DarkGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onNavigateToThird,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text("GO TO THIRD ACTIVITY")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text("GO TO MAIN ACTIVITY")
            }
        }
    }
}
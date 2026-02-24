package com.example.appmigueleduardo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat

class MainActivity : ComponentActivity(), LocationListener {
    private val TAG = "btaMainActivity"
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2

    private var latestLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Conecta con tu archivo XML
        setContentView(R.layout.activity_main)
        // Esto aparecerá en la pestaña Logcat de abajo
        Log.d(TAG, "onCreate: Landing Page cargada")

        // Snippet: Inicialización del servicio
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // Snippet: Verificación de permisos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        } else {
            // Snippet: Solicitar actualizaciones (5 seg, 5 metros)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
        }

        // Dentro de onCreate, después de setContentView
        val buttonNext: Button = findViewById(R.id.btnGoToList)
        buttonNext.setOnClickListener {
            // Esto crea la navegación hacia la siguiente pantalla
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
            Log.d(TAG, "Botón presionado: Navegando a la lista")
        }

        val buttonOsm: Button = findViewById(R.id.osmButton)
        buttonOsm.setOnClickListener {
            if (latestLocation != null) {
                val intent = Intent(this, OpenStreetMapsActivity::class.java)
                val bundle = Bundle()
                bundle.putParcelable("location", latestLocation)
                intent.putExtra("locationBundle", bundle)
                startActivity(intent)
            } else {
                Log.e(TAG, "Location not set yet.")
            }
        }
    }

    // Snippet: Métdo que se ejecuta al recibir coordenadas
    override fun onLocationChanged(location: Location) {
        latestLocation = location
        val textView: TextView = findViewById(R.id.mainTextView) // Asegúrate de que el ID esté en el XML
        textView.text = "Latitude: ${location.latitude}, Longitude: ${location.longitude}"
        Log.d(TAG, "Ubicación: ${location.latitude}, ${location.longitude}")
    }

    // Snippet: Manejo de la respuesta del usuario al permiso
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
                }
            }
        }
    }

    // Snippets: Métodos vacíos necesarios para que no dé error la interfaz
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}
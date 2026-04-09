package com.example.appmigueleduardo

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import java.io.File

class MainActivity : ComponentActivity(), LocationListener {
    private val TAG = "btaMainActivity"
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2
    private var latestLocation: Location? = null
    private lateinit var locationSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val userIdentifier = getUserIdentifier()
        if (userIdentifier == null) {
            showUserIdentifierDialog()
        } else {
            Toast.makeText(this, "User ID: $userIdentifier", Toast.LENGTH_LONG).show()
        }

        findViewById<Button>(R.id.userIdentifierButton).setOnClickListener {
            showUserIdentifierDialog()
        }

        locationSwitch = findViewById(R.id.locationSwitch)
        locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkPermissionsAndStartGPS()
                Toast.makeText(this, "Rastreo activado", Toast.LENGTH_SHORT).show()
            } else {
                locationManager.removeUpdates(this) // [cite: 307]
                limpiarDatosPantalla() // Nueva función para borrar los números
                Toast.makeText(this, "Rastreo desactivado", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnGoToList).setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.osmButton).setOnClickListener {
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

    private fun limpiarDatosPantalla() {
        findViewById<TextView>(R.id.tvLat).text = "---"
        findViewById<TextView>(R.id.tvLon).text = "---"
        findViewById<TextView>(R.id.tvUser).text = "---"
    }

    override fun onLocationChanged(location: Location) {
        latestLocation = location

        // Actualizamos los números en pantalla [cite: 318]
        findViewById<TextView>(R.id.tvLat).text = String.format("%.4f", location.latitude) // [cite: 327]
        findViewById<TextView>(R.id.tvLon).text = String.format("%.4f", location.longitude) // [cite: 328]
        findViewById<TextView>(R.id.tvUser).text = getUserIdentifier() ?: "---"

        saveCoordinatesToFile(location.latitude, location.longitude, location.altitude, System.currentTimeMillis()) // [cite: 320]
    }

    private fun showUserIdentifierDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter User Identifier")
        val input = EditText(this)
        input.setText(getUserIdentifier())
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val userInput = input.text.toString()
            if (userInput.isNotBlank()) {
                saveUserIdentifier(userInput)
                Toast.makeText(this, "User ID guardado: $userInput", Toast.LENGTH_LONG).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun saveUserIdentifier(id: String) {
        val prefs = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        prefs.edit().putString("userIdentifier", id).apply() // [cite: 357, 360, 361]
    }

    private fun getUserIdentifier(): String? {
        val prefs = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return prefs.getString("userIdentifier", null) // [cite: 364]
    }

    private fun saveCoordinatesToFile(latitude: Double, longitude: Double, altitude: Double, timestamp: Long) {
        val file = File(filesDir, "gps_coordinates.csv") // [cite: 325, 326]
        val lat = String.format("%.4f", latitude) // [cite: 327]
        val lon = String.format("%.4f", longitude) // [cite: 328]
        val alt = String.format("%.2f", altitude) // [cite: 329]
        file.appendText("$timestamp; $lat; $lon; $alt\n") // [cite: 330]
    }

    private fun checkPermissionsAndStartGPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode) // [cite: 297, 298]
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this) // [cite: 304]
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissionsAndStartGPS()
            }
        }
    }

    override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}
    override fun onProviderEnabled(p: String) {}
    override fun onProviderDisabled(p: String) {}
}
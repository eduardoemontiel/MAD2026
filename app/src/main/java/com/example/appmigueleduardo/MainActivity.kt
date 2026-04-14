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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.appmigueleduardo.room.AppDatabase
import com.example.appmigueleduardo.room.CoordinatesEntity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity(), LocationListener {

    companion object {
        var isGpsEnabledSession: Boolean = false
        var lastLatSession: String = "---"
        var lastLonSession: String = "---"
        var lastSaveTime: Long = 0
        var lastSavedLocation: Location? = null
    }

    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2
    private var latestLocation: Location? = null
    private lateinit var locationSwitch: Switch
    private val PREFS_NAME = "AppPreferences"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        setContentView(R.layout.activity_main)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val userIdentifier = getUserIdentifier()
        if (userIdentifier == null) showUserIdentifierDialog()
        else findViewById<TextView>(R.id.tvUser).text = userIdentifier

        locationSwitch = findViewById(R.id.locationSwitch)
        locationSwitch.isChecked = isGpsEnabledSession
        findViewById<TextView>(R.id.tvLat).text = lastLatSession
        findViewById<TextView>(R.id.tvLon).text = lastLonSession

        if (isGpsEnabledSession) checkPermissionsAndStartGPS()

        locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            isGpsEnabledSession = isChecked
            if (isChecked) {
                checkPermissionsAndStartGPS()
            } else {
                locationManager.removeUpdates(this)
                limpiarDatosSesion()
            }
        }

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.selectedItemId = R.id.navigation_home
        navView.setOnNavigationItemSelectedListener { item ->
            if (item.itemId == navView.selectedItemId) return@setOnNavigationItemSelectedListener true
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_map -> {
                    startActivity(Intent(this, OpenStreetMapsActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    })
                    finish()
                    true
                }
                R.id.navigation_list -> {
                    startActivity(Intent(this, SecondActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    })
                    finish()
                    true
                }
                else -> false
            }
        }
        findViewById<Button>(R.id.userIdentifierButton).setOnClickListener { showUserIdentifierDialog() }
    }

    override fun onLocationChanged(location: Location) {
        if (!isGpsEnabledSession) return

        lastLatSession = String.format("%.4f", location.latitude)
        lastLonSession = String.format("%.4f", location.longitude)
        findViewById<TextView>(R.id.tvLat).text = lastLatSession
        findViewById<TextView>(R.id.tvLon).text = lastLonSession

        val currentTime = System.currentTimeMillis()
        val distance = lastSavedLocation?.distanceTo(location) ?: Float.MAX_VALUE

        if (currentTime - lastSaveTime > 10000 && distance > 5f) {
            lastSaveTime = currentTime
            lastSavedLocation = location

            // Guardado en archivo CSV
            saveCoordinatesToFile(location.latitude, location.longitude, location.altitude, currentTime)

            // Guardado en Base de Datos Room
            saveCoordinatesToDatabase(location.latitude, location.longitude, location.altitude, currentTime)
        }
    }

    private fun saveCoordinatesToDatabase(latitude: Double, longitude: Double, altitude: Double, timestamp: Long) {
        val coordinates = CoordinatesEntity(
            timestamp = timestamp,
            latitude = latitude,
            longitude = longitude,
            altitude = altitude
        )

        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            db.coordinatesDao().insert(coordinates)
            Log.d("MainActivity", "Nueva ubicación guardada en Room")
        }
    }

    private fun limpiarDatosSesion() {
        lastLatSession = "---"
        lastLonSession = "---"
        findViewById<TextView>(R.id.tvLat).text = "---"
        findViewById<TextView>(R.id.tvLon).text = "---"
        lastSavedLocation = null
        lastSaveTime = 0
    }

    private fun getUserIdentifier(): String? {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("userIdentifier", null)
    }

    private fun saveUserIdentifier(id: String) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("userIdentifier", id).apply()
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
                findViewById<TextView>(R.id.tvUser).text = userInput
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun saveCoordinatesToFile(latitude: Double, longitude: Double, altitude: Double, timestamp: Long) {
        val file = File(filesDir, "gps_coordinates.csv")
        file.appendText("$timestamp; ${String.format("%.4f", latitude)}; ${String.format("%.4f", longitude)}; ${String.format("%.2f", altitude)}\n")
    }

    private fun checkPermissionsAndStartGPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkPermissionsAndStartGPS()
        }
    }
}
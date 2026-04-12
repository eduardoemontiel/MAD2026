package com.example.appmigueleduardo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File

class OpenStreetMapsActivity : AppCompatActivity(), LocationListener {
    private lateinit var map: MapView
    private lateinit var locationManager: LocationManager
    private var myMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        setContentView(R.layout.activity_open_street_maps)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.selectedItemId = R.id.navigation_map
        navView.setOnNavigationItemSelectedListener { item ->
            if (item.itemId == navView.selectedItemId) return@setOnNavigationItemSelectedListener true
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION) })
                    finish()
                    true
                }
                R.id.navigation_list -> {
                    startActivity(Intent(this, SecondActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION) })
                    finish()
                    true
                }
                else -> false
            }
        }

        map = findViewById(R.id.mapView)
        map.setBackgroundColor(Color.parseColor("#F5F5F5"))
        Configuration.getInstance().load(this, getSharedPreferences("osm", MODE_PRIVATE))
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(18.0)

        updateMapPosition(MainActivity.lastLatSession, MainActivity.lastLonSession)

        if (MainActivity.isGpsEnabledSession) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        if (!MainActivity.isGpsEnabledSession) return

        MainActivity.lastLatSession = String.format("%.4f", location.latitude)
        MainActivity.lastLonSession = String.format("%.4f", location.longitude)

        updateMapPosition(MainActivity.lastLatSession, MainActivity.lastLonSession)

        // Lógica de guardado también en el mapa para no perder datos
        val currentTime = System.currentTimeMillis()
        val distance = MainActivity.lastSavedLocation?.distanceTo(location) ?: Float.MAX_VALUE
        if (currentTime - MainActivity.lastSaveTime > 10000 && distance > 5f) {
            MainActivity.lastSaveTime = currentTime
            MainActivity.lastSavedLocation = location
            saveCoordinatesToFile(location.latitude, location.longitude, location.altitude, currentTime)
        }
    }

    private fun updateMapPosition(latStr: String, lonStr: String) {
        if (latStr != "---" && lonStr != "---") {
            val point = GeoPoint(latStr.replace(",", ".").toDouble(), lonStr.replace(",", ".").toDouble())
            map.controller.setCenter(point)
            if (myMarker == null) {
                myMarker = Marker(map)
                val xIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_delete)
                xIcon?.setTint(Color.RED)
                myMarker?.icon = xIcon
                map.overlays.add(myMarker)
            }
            myMarker?.position = point
            map.invalidate()
        }
    }

    private fun saveCoordinatesToFile(lat: Double, lon: Double, alt: Double, time: Long) {
        val file = File(filesDir, "gps_coordinates.csv")
        file.appendText("$time; ${String.format("%.4f", lat)}; ${String.format("%.4f", lon)}; ${String.format("%.2f", alt)}\n")
    }

    override fun onResume() { super.onResume(); map.onResume() }
    override fun onPause() {
        super.onPause()
        map.onPause()
        locationManager.removeUpdates(this)
    }
}
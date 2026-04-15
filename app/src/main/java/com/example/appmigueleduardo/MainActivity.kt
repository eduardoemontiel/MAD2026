package com.example.appmigueleduardo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.appmigueleduardo.network.WeatherApiService
import com.example.appmigueleduardo.network.WeatherResponse
import com.example.appmigueleduardo.room.AppDatabase
import com.example.appmigueleduardo.room.CoordinatesEntity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
    private lateinit var locationSwitch: Switch
    private val PREFS_NAME = "AppPreferences"

    private lateinit var weatherTextView: TextView
    private lateinit var weatherIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        setContentView(R.layout.activity_main)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        weatherTextView = findViewById(R.id.weatherTextView)
        weatherIcon = findViewById(R.id.weatherIcon)

        // Actualizar el nombre de usuario al iniciar
        updateUserDisplay()

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

        // CAMBIO: Ahora el botón naranja abre la actividad de Settings
        findViewById<Button>(R.id.userIdentifierButton).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    // Para que el nombre de usuario se actualice al volver de Settings
    override fun onResume() {
        super.onResume()
        updateUserDisplay()
    }

    private fun updateUserDisplay() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userIdentifier = prefs.getString("userIdentifier", "---")
        findViewById<TextView>(R.id.tvUser).text = userIdentifier
    }

    override fun onLocationChanged(location: Location) {
        if (!isGpsEnabledSession) return

        lastLatSession = String.format("%.4f", location.latitude)
        lastLonSession = String.format("%.4f", location.longitude)
        findViewById<TextView>(R.id.tvLat).text = lastLatSession
        findViewById<TextView>(R.id.tvLon).text = lastLonSession

        getWeatherForecast(location.latitude, location.longitude)

        val currentTime = System.currentTimeMillis()
        val distance = lastSavedLocation?.distanceTo(location) ?: Float.MAX_VALUE

        if (currentTime - lastSaveTime > 10000 && distance > 5f) {
            lastSaveTime = currentTime
            lastSavedLocation = location
            saveCoordinatesToFile(location.latitude, location.longitude, location.altitude, currentTime)
            saveCoordinatesToDatabase(location.latitude, location.longitude, location.altitude, currentTime)
        }
    }

    // --- FUNCIONES DE CLIMA (RETROFIT) ---

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun getWeatherForecast(lat: Double, lon: Double) {
        if (!isNetworkAvailable()) return

        // CAMBIO: Leemos la API KEY de SharedPreferences
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val apiKey = prefs.getString("API_KEY", "")

        if (apiKey.isNullOrBlank()) {
            weatherTextView.text = "Configura tu API KEY en ajustes"
            return
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherApiService::class.java)
        val call = service.getWeatherForecast(lat, lon, 1, apiKey)

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { showWeatherInfo(it) }
                }
            }
            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e("MainActivity", "Error fetching weather: ${t.message}")
            }
        })
    }

    private fun showWeatherInfo(weatherResponse: WeatherResponse) {
        val item = weatherResponse.list.firstOrNull()
        if (item != null) {
            val tempCelsius = item.main.temp - 273.15
            val tempFormatted = String.format("%.1f", tempCelsius)
            weatherTextView.text = "${item.name}: $tempFormatted°C\n${item.weather[0].description}"
            val iconUrl = "https://openweathermap.org/img/wn/${item.weather[0].icon}@4x.png"
            Glide.with(this).load(iconUrl).into(weatherIcon)
        }
    }

    // --- FUNCIONES DE PERSISTENCIA (ROOM) ---

    private fun saveCoordinatesToDatabase(latitude: Double, longitude: Double, altitude: Double, timestamp: Long) {
        val coordinates = CoordinatesEntity(timestamp, latitude, longitude, altitude)
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            db.coordinatesDao().insert(coordinates)
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
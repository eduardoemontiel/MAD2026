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
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.appmigueleduardo.network.*
import com.example.appmigueleduardo.room.AppDatabase
import com.example.appmigueleduardo.room.CoordinatesEntity
import com.firebase.ui.auth.AuthUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
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

    private var lastClimaData: String = ""
    private var lastAirData: String = ""
    private var lastIconUrl: String? = null
    private var lastPm25: Double = 0.0
    private var lastAqiValue: Int = 0

    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2
    private lateinit var locationSwitch: Switch
    private val PREFS_NAME = "AppPreferences"
    private lateinit var weatherIcon: ImageView
    private lateinit var userNameTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        setContentView(R.layout.activity_main)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        weatherIcon = findViewById(R.id.weatherIcon)
        userNameTextView = findViewById(R.id.userNameTextView)
        updateUserDisplay()
        updateUIWithUsername()
        locationSwitch = findViewById(R.id.locationSwitch)
        locationSwitch.isChecked = isGpsEnabledSession
        locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            isGpsEnabledSession = isChecked
            if (isChecked) checkPermissionsAndStartGPS() else {
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
                    startActivity(Intent(this, OpenStreetMapsActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION) })
                    finish(); true
                }
                R.id.navigation_list -> {
                    startActivity(Intent(this, SecondActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION) })
                    finish(); true
                }
                else -> false
            }
        }

        findViewById<Button>(R.id.userIdentifierButton).setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
        findViewById<Button>(R.id.btnLogoutManual).setOnClickListener { logout() }
        if (isGpsEnabledSession) checkPermissionsAndStartGPS()
    }

    private fun updateWeatherUI(pm25: Double = lastPm25, aqiInt: Int = lastAqiValue) {
        lastPm25 = pm25
        lastAqiValue = aqiInt
        val (estado, recomendacion) = when {
            pm25 <= 0.0  -> "--" to ""
            pm25 <= 12.0 -> "Bueno" to "El aire es limpio. Disfruta de actividades al aire libre."
            pm25 <= 35.0 -> "Moderado" to "Aire aceptable, pero personas sensibles deben tener precaución."
            else         -> "Peligroso" to "Nivel elevado. Usa mascarilla y evita ejercicio intenso."
        }
        val aqiColor = when {
            pm25 <= 0.0  -> Color.parseColor("#66FFFFFF")
            pm25 <= 12.0 -> Color.parseColor("#2ECC71")
            pm25 <= 35.0 -> Color.parseColor("#F39C12")
            else         -> Color.RED
        }
        runOnUiThread {
            if (lastClimaData.isNotEmpty()) {
                val lines = lastClimaData.split("\n")
                if (lines.size >= 2) {
                    val cityAndTemp = lines[0]
                    val city = cityAndTemp.substringBefore(":").trim().uppercase()
                    val temp = cityAndTemp.substringAfter(":").trim().replace("°C", "°")
                    val desc = lines[1].replaceFirstChar { it.uppercase() }
                    findViewById<TextView>(R.id.tvWeatherLocation).text = city
                    findViewById<TextView>(R.id.tvTemperature).text = temp
                    findViewById<TextView>(R.id.tvWeatherDesc).text = desc
                }
            }

            // --- AQI ---
            val aqiText = if (aqiInt > 0) aqiInt.toString() else "--"
            findViewById<TextView>(R.id.tvAqiValue).text = aqiText

            // --- PM2.5 ---
            val pm25Text = if (pm25 > 0.0) String.format("%.2f", pm25) else "--"
            findViewById<TextView>(R.id.tvPm25).text = pm25Text

            // --- Estado ---
            findViewById<TextView>(R.id.tvEstado).apply {
                text = estado
                setTextColor(aqiColor)
            }

            findViewById<TextView>(R.id.tvAqiStatus).apply {
                text = if (pm25 > 0.0) estado else "--"
                setTextColor(aqiColor)
            }

            val progress = pm25.coerceAtMost(500.0).toInt()
            findViewById<ProgressBar>(R.id.aqiProgressBar).apply {
                this.progress = progress
                progressTintList = android.content.res.ColorStateList.valueOf(aqiColor)
            }

            findViewById<TextView>(R.id.weatherTextView).text = recomendacion
            lastIconUrl?.let { Glide.with(this).load(it).into(weatherIcon) }
        }
    }

    override fun onLocationChanged(location: Location) {
        if (!isGpsEnabledSession) return
        lastLatSession = String.format("%.4f", location.latitude)
        lastLonSession = String.format("%.4f", location.longitude)
        lastSavedLocation = location
        findViewById<TextView>(R.id.tvLat).text = lastLatSession
        findViewById<TextView>(R.id.tvLon).text = lastLonSession
        getWeatherForecast(location.latitude, location.longitude)
        getAirQuality(location.latitude, location.longitude)
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSaveTime > 10000) {
            lastSaveTime = currentTime
            saveCoordinatesToFile(location.latitude, location.longitude, location.altitude, currentTime)
            saveCoordinatesToDatabase(location.latitude, location.longitude, location.altitude, currentTime)
        }
    }

    private fun getWeatherForecast(lat: Double, lon: Double) {
        val apiKey = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString("API_KEY", "") ?: ""
        if (apiKey.isBlank()) return
        val service = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
        service.getWeatherForecast(lat, lon, 1, apiKey).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                response.body()?.list?.firstOrNull()?.let {
                    lastClimaData = "${it.name}: ${String.format("%.1f", it.main.temp - 273.15)}°C\n${it.weather[0].description}"
                    lastIconUrl = "https://openweathermap.org/img/wn/${it.weather[0].icon}@4x.png"
                    updateWeatherUI()
                }
            }
            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {}
        })
    }

    private fun getAirQuality(lat: Double, lon: Double) {
        val apiKey = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString("API_KEY", "") ?: ""
        val service = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
        service.getAirPollution(lat, lon, apiKey).enqueue(object : Callback<AirPollutionResponse> {
            override fun onResponse(call: Call<AirPollutionResponse>, response: Response<AirPollutionResponse>) {
                response.body()?.list?.firstOrNull()?.let { airData ->
                    val pm25Value = airData.components.pm2_5.toDouble()
                    val aqiValue = airData.main.aqi
                    lastAirData = "AQI: $aqiValue | PM2.5: $pm25Value μg/m³"
                    updateWeatherUI(pm25Value, aqiValue)
                }
            }
            override fun onFailure(call: Call<AirPollutionResponse>, t: Throwable) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_logout) logout()
        return true
    }

    private fun updateUIWithUsername() {
        FirebaseAuth.getInstance().currentUser?.let {
            userNameTextView.text = it.displayName ?: "Usuario"
        }
    }

    private fun logout() {
        AuthUI.getInstance().signOut(this).addOnCompleteListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun updateUserDisplay() {
        val identifier = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("userIdentifier", "--") ?: "--"
        findViewById<TextView>(R.id.tvUser).text = identifier
    }

    private fun limpiarDatosSesion() {
        lastClimaData = ""
        lastAirData = ""
        lastPm25 = 0.0
        lastAqiValue = 0
        lastIconUrl = null
        findViewById<TextView>(R.id.tvLat).text = "--"
        findViewById<TextView>(R.id.tvLon).text = "--"
        findViewById<TextView>(R.id.tvWeatherLocation).text = ""
        findViewById<TextView>(R.id.tvTemperature).text = "--°"
        findViewById<TextView>(R.id.tvWeatherDesc).text = ""
        findViewById<TextView>(R.id.tvAqiStatus).text = "--"
        findViewById<TextView>(R.id.tvAqiStatus).setTextColor(Color.parseColor("#66FFFFFF"))
        findViewById<TextView>(R.id.tvAqiValue).text = "--"
        findViewById<TextView>(R.id.tvPm25).text = "--"
        findViewById<TextView>(R.id.tvEstado).text = "--"
        findViewById<TextView>(R.id.tvEstado).setTextColor(Color.parseColor("#66FFFFFF"))
        findViewById<TextView>(R.id.weatherTextView).text = ""
        findViewById<ProgressBar>(R.id.aqiProgressBar).progress = 0
        weatherIcon.setImageDrawable(null)
    }

    private fun saveCoordinatesToDatabase(lat: Double, lon: Double, alt: Double, time: Long) {
        lifecycleScope.launch {
            AppDatabase.getDatabase(this@MainActivity).coordinatesDao().insert(
                CoordinatesEntity(time, lat, lon, alt)
            )
        }
    }

    private fun saveCoordinatesToFile(lat: Double, lon: Double, alt: Double, time: Long) {
        File(filesDir, "gps_coordinates.csv").appendText("$time; $lat; $lon; $alt\n")
    }

    private fun checkPermissionsAndStartGPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }
    }

    override fun onRequestPermissionsResult(req: Int, perms: Array<out String>, grants: IntArray) {
        super.onRequestPermissionsResult(req, perms, grants)
        if (req == locationPermissionCode && grants.isNotEmpty() && grants[0] == PackageManager.PERMISSION_GRANTED) {
            checkPermissionsAndStartGPS()
        }
    }
}
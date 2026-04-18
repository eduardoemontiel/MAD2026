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
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.appmigueleduardo.network.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class OpenStreetMapsActivity : AppCompatActivity(), LocationListener {
    private lateinit var map: MapView
    private lateinit var locationManager: LocationManager
    private lateinit var tvRecommendation: TextView
    private lateinit var btnClearRoute: Button
    private var myMarker: Marker? = null
    private var destinationMarker: Marker? = null
    private var runningPath: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_street_maps)
        tvRecommendation = findViewById(R.id.tvRecommendation)
        btnClearRoute = findViewById(R.id.btnClearRoute)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        setupNavigation()
        map = findViewById(R.id.mapView)
        Configuration.getInstance().load(this, getSharedPreferences("osm", MODE_PRIVATE))
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(17.0)
        val mReceive = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                setDestination(p)
                return true
            }
            override fun longPressHelper(p: GeoPoint): Boolean = false
        }
        map.overlays.add(MapEventsOverlay(mReceive))
        btnClearRoute.setOnClickListener {
            clearDestination()
        }
        updateMyPosition(MainActivity.lastLatSession, MainActivity.lastLonSession)
        if (MainActivity.isGpsEnabledSession) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
            }
        }
    }

    private fun setDestination(point: GeoPoint) {
        if (destinationMarker == null) {
            destinationMarker = Marker(map)
            destinationMarker?.title = "Tu Meta"
            destinationMarker?.icon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_compass)
            map.overlays.add(destinationMarker)
        }
        destinationMarker?.position = point
        tvRecommendation.text = "Destino fijado. Verificando calidad del aire..."
        tvRecommendation.setTextColor(Color.parseColor("#1976D2"))
        btnClearRoute.visibility = View.VISIBLE
        checkAirQualityAtDestination(point.latitude, point.longitude)
        drawRoute()
        map.invalidate()
    }

    private fun checkAirQualityAtDestination(lat: Double, lon: Double) {
        val apiKey = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE).getString("API_KEY", "") ?: ""
        if (apiKey.isBlank()) return
        val service = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
        service.getWeatherForecast(lat, lon, 1, apiKey).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                val weather = response.body()?.list?.firstOrNull()
                val temp = weather?.let { String.format("%.1f", it.main.temp - 273.15) } ?: "--"
                val desc = weather?.weather?.firstOrNull()?.description ?: ""
                service.getAirPollution(lat, lon, apiKey).enqueue(object : Callback<AirPollutionResponse> {
                    override fun onResponse(call: Call<AirPollutionResponse>, response: Response<AirPollutionResponse>) {
                        val airData = response.body()?.list?.firstOrNull()
                        val aqi = airData?.main?.aqi ?: 0
                        val estadoAire = when(aqi) {
                            1 -> "Buena"
                            2 -> "Moderada"
                            else -> "Mala/Peligrosa"
                        }
                        runOnUiThread {
                            tvRecommendation.text = """
Calidad del Aire: $estadoAire (AQI: $aqi)
Tiempo: $temp°C, $desc
""".trimIndent()
                            tvRecommendation.setTextColor(if (aqi <= 2) Color.parseColor("#2E7D32") else Color.RED)
                        }
                    }
                    override fun onFailure(call: Call<AirPollutionResponse>, t: Throwable) {}
                })
            }
            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {}
        })
    }

    private fun clearDestination() {
        map.overlays.remove(destinationMarker)
        map.overlays.remove(runningPath)
        destinationMarker = null
        runningPath = null
        tvRecommendation.text = "Toca el mapa para elegir un destino"
        tvRecommendation.setTextColor(Color.BLACK)
        btnClearRoute.visibility = View.GONE
        map.invalidate()
    }

    private fun updateMyPosition(latStr: String, lonStr: String) {
        if (latStr != "---" && lonStr != "---") {
            val lat = latStr.replace(",", ".").toDouble()
            val lon = lonStr.replace(",", ".").toDouble()
            val myPoint = GeoPoint(lat, lon)
            if (myMarker == null) {
                myMarker = Marker(map)
                myMarker?.title = "Tú estás aquí"
                val icon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_mylocation)
                icon?.setTint(Color.BLUE)
                myMarker?.icon = icon
                map.overlays.add(myMarker)
            }
            myMarker?.position = myPoint
            if (map.tag == null) {
                map.controller.setCenter(myPoint)
                map.tag = "centered"
            }
            drawRoute()
            map.invalidate()
        }
    }

    private fun drawRoute() {
        val start = myMarker?.position
        val end = destinationMarker?.position
        if (start != null && end != null) {
            if (runningPath == null) {
                runningPath = Polyline()
                runningPath?.outlinePaint?.color = Color.parseColor("#E91E63")
                runningPath?.outlinePaint?.strokeWidth = 12f
                map.overlays.add(runningPath)
            }
            runningPath?.setPoints(listOf(start, end))
        }
    }

    override fun onLocationChanged(location: Location) {
        if (!MainActivity.isGpsEnabledSession) return
        MainActivity.lastLatSession = String.format("%.4f", location.latitude)
        MainActivity.lastLonSession = String.format("%.4f", location.longitude)
        updateMyPosition(MainActivity.lastLatSession, MainActivity.lastLonSession)
    }

    private fun setupNavigation() {
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.selectedItemId = R.id.navigation_map
        navView.setOnNavigationItemSelectedListener { item ->
            if (item.itemId == navView.selectedItemId) return@setOnNavigationItemSelectedListener true
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION) })
                    finish(); true
                }
                R.id.navigation_list -> {
                    startActivity(Intent(this, SecondActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION) })
                    finish(); true
                }
                else -> false
            }
        }
    }

    override fun onResume() { super.onResume(); map.onResume() }

    override fun onPause() { super.onPause(); map.onPause(); locationManager.removeUpdates(this) }

    override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}

    override fun onProviderEnabled(p: String) {}

    override fun onProviderDisabled(p: String) {}

}
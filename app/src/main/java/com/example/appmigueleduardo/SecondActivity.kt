package com.example.appmigueleduardo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appmigueleduardo.room.AppDatabase
import com.example.appmigueleduardo.room.CoordinatesEntity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SecondActivity : AppCompatActivity(), LocationListener {
    private lateinit var locationManager: LocationManager
    private lateinit var adapter: GPSAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        setContentView(R.layout.activity_second)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.selectedItemId = R.id.navigation_list
        navView.setOnNavigationItemSelectedListener { item ->
            if (item.itemId == navView.selectedItemId) return@setOnNavigationItemSelectedListener true
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION) })
                    finish()
                    true
                }
                R.id.navigation_map -> {
                    startActivity(Intent(this, OpenStreetMapsActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION) })
                    finish()
                    true
                }
                else -> false
            }
        }

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewGPS)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = GPSAdapter(emptyList()) { item ->
            startActivity(Intent(this, ThirdActivity::class.java).apply {
                putExtra("timestamp", item.timestamp.toString())
                putExtra("latitude", item.latitude.toString())
                putExtra("longitude", item.longitude.toString())
                putExtra("altitude", item.altitude.toString())
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            })
        }
        recyclerView.adapter = adapter

        loadDataFromDatabase()

        if (MainActivity.isGpsEnabledSession) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
            }
        }
    }

    private fun loadDataFromDatabase() {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            val dbCoordinates = db.coordinatesDao().getAll()
            adapter.updateData(dbCoordinates)
        }
    }

    override fun onLocationChanged(location: Location) {
        if (!MainActivity.isGpsEnabledSession) return

        val currentTime = System.currentTimeMillis()
        val distance = MainActivity.lastSavedLocation?.distanceTo(location) ?: Float.MAX_VALUE

        if (currentTime - MainActivity.lastSaveTime > 10000 && distance > 5f) {
            MainActivity.lastSaveTime = currentTime
            MainActivity.lastSavedLocation = location

            saveCoordinatesToFile(location.latitude, location.longitude, location.altitude, currentTime)

            // Usamos una sola corrutina para asegurar el orden: primero guardar, luego recargar [cite: 71, 97]
            lifecycleScope.launch {
                saveCoordinatesToDatabase(location.latitude, location.longitude, location.altitude, currentTime)
                loadDataFromDatabase()
            }
        }
    }

    // Cambiada a suspend para que la corrutina espere a que termine la inserción [cite: 49]
    private suspend fun saveCoordinatesToDatabase(lat: Double, lon: Double, alt: Double, time: Long) {
        val db = AppDatabase.getDatabase(this)
        val entity = CoordinatesEntity(time, lat, lon, alt)
        db.coordinatesDao().insert(entity)
    }

    private fun saveCoordinatesToFile(lat: Double, lon: Double, alt: Double, time: Long) {
        val file = File(filesDir, "gps_coordinates.csv")
        file.appendText("$time; ${String.format("%.4f", lat)}; ${String.format("%.4f", lon)}; ${String.format("%.2f", alt)}\n")
    }

    override fun onPause() { super.onPause(); locationManager.removeUpdates(this) }
}

class GPSAdapter(private var items: List<CoordinatesEntity>, private val onClick: (CoordinatesEntity) -> Unit) :
    RecyclerView.Adapter<GPSAdapter.ViewHolder>() {

    fun updateData(newItems: List<CoordinatesEntity>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.itemDate)
        val tvLat: TextView = view.findViewById(R.id.itemLat)
        val tvLon: TextView = view.findViewById(R.id.itemLon)
        val tvAlt: TextView = view.findViewById(R.id.itemAlt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gps, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(item.timestamp))

        holder.tvDate.text = dateStr
        holder.tvLat.text = String.format("%.4f", item.latitude)
        holder.tvLon.text = String.format("%.4f", item.longitude)
        holder.tvAlt.text = String.format("%.2f", item.altitude)

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}
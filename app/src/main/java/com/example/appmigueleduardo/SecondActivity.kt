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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import java.io.IOException
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

        val data = readFileContents().toMutableList()
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewGPS)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = GPSAdapter(data) { item ->
            startActivity(Intent(this, ThirdActivity::class.java).apply {
                putExtra("latitude", item[1]); putExtra("longitude", item[2]); putExtra("altitude", item[3])
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            })
        }
        recyclerView.adapter = adapter

        if (MainActivity.isGpsEnabledSession) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
            }
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

            // Refrescar la lista visualmente
            adapter.updateData(readFileContents())
        }
    }

    private fun saveCoordinatesToFile(lat: Double, lon: Double, alt: Double, time: Long) {
        val file = File(filesDir, "gps_coordinates.csv")
        file.appendText("$time; ${String.format("%.4f", lat)}; ${String.format("%.4f", lon)}; ${String.format("%.2f", alt)}\n")
    }

    private fun readFileContents(): List<List<String>> {
        val file = File(filesDir, "gps_coordinates.csv")
        if (!file.exists()) return emptyList()
        return file.readLines().map { it.split(";").map(String::trim) }
    }

    override fun onPause() { super.onPause(); locationManager.removeUpdates(this) }
}

class GPSAdapter(private var items: List<List<String>>, private val onClick: (List<String>) -> Unit) :
    RecyclerView.Adapter<GPSAdapter.ViewHolder>() {

    fun updateData(newItems: List<List<String>>) {
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
        if (item.size >= 4) {
            val dateStr = try {
                SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(item[0].toLong()))
            } catch (e: Exception) { item[0] }
            holder.tvDate.text = dateStr
            holder.tvLat.text = item[1]
            holder.tvLon.text = item[2]
            holder.tvAlt.text = item[3]
            holder.itemView.setOnClickListener { onClick(item) }
        }
    }
    override fun getItemCount() = items.size
}
package com.example.appmigueleduardo

import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appmigueleduardo.room.AppDatabase
import com.example.appmigueleduardo.room.CoordinatesEntity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SecondActivity : AppCompatActivity(), LocationListener {

    private lateinit var adapter: GPSAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.selectedItemId = R.id.navigation_list
        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION) })
                    finish(); true
                }
                R.id.navigation_map -> {
                    startActivity(Intent(this, OpenStreetMapsActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION) })
                    finish(); true
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
    }

    private fun loadDataFromDatabase() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@SecondActivity)
            val dbCoordinates = db.coordinatesDao().getAll()
            adapter.updateData(dbCoordinates)
        }
    }

    override fun onLocationChanged(location: Location) {}
    override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}
    override fun onProviderEnabled(p: String) {}
    override fun onProviderDisabled(p: String) {}
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
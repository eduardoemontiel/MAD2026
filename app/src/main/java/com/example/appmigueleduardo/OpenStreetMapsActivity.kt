package com.example.appmigueleduardo

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.location.Location
import android.util.Log
import android.graphics.Color
import androidx.core.content.ContextCompat

import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class OpenStreetMapsActivity : AppCompatActivity() {
    private val TAG = "btaOpenStreetMapActivity"
    private lateinit var map: MapView

    private val gymkhanaCoords = listOf(
        GeoPoint(40.38779608214728, -3.627687914352839), // Tennis
        GeoPoint(40.38788595319803, -3.627048250272035), // Futsal outdoors
        GeoPoint(40.3887315224542, -3.628643539758645), // Fashion and design
        GeoPoint(40.38926842612264, -3.630067893975619), // Topos
        GeoPoint(40.38956358584258, -3.629046081389352), // Teleco
        GeoPoint(40.38992125672989, -3.6281366497769714), // ETSISI
        GeoPoint(40.39037466191718, -3.6270256763598447), // Library
        GeoPoint(40.389855884803005, -3.626782180787362) // CITSEM
    )

    private val gymkhanaNames = listOf(
        "Tennis", "Futsal outdoors", "Fashion and design school",
        "Topography school", "Telecommunications school", "ETSISI",
        "Library", "CITSEM"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Starting activity...")
        enableEdgeToEdge()
        setContentView(R.layout.activity_open_street_maps)

        // IMPORTANTE: Inicializar la vista del mapa antes de configurarla
        map = findViewById(R.id.mapView)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configuración de OSM (Usando el ID del proyecto como pide el snippet)
        Configuration.getInstance().userAgentValue = "es.upm.btb.madproject"
        Configuration.getInstance().load(this, getSharedPreferences("osm", MODE_PRIVATE))

        // Obtener ubicación del Bundle
        val bundle = intent.getBundleExtra("locationBundle")
        val location: Location? = bundle?.getParcelable("location")

        val startPoint = if (location != null) {
            Log.d(TAG, "onCreate: Location[${location.latitude}][${location.longitude}]")
            GeoPoint(location.latitude, location.longitude)
        } else {
            Log.d(TAG, "onCreate: Location is null, using default coordinates")
            GeoPoint(40.389683644051864, -3.627825356970311)
        }

        // Configuración visual del mapa
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(18.0) // Ajuste de zoom
        map.controller.setCenter(startPoint) // Centrar mapa

        // Marcador de ubicación actual
        val myLocationMarker = Marker(map)
        myLocationMarker.position = startPoint
        myLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        myLocationMarker.title = "Mi ubicación actual"

        val xIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_delete)
        xIcon?.setTint(Color.RED)
        myLocationMarker.icon = xIcon
        map.overlays.add(myLocationMarker)

        // Añadir marcadores de la Gymkhana
        addGymkhanaMarkers(map, gymkhanaCoords, gymkhanaNames, this)

        // Definir la ruta (Polyline)
        val polyline = Polyline()
        polyline.outlinePaint.color = Color.RED
        polyline.outlinePaint.strokeWidth = 7f
        val routePoints = mutableListOf(startPoint)
        routePoints.addAll(gymkhanaCoords)
        polyline.setPoints(routePoints)
        map.overlays.add(polyline)

        map.invalidate() // Refrescar para mostrar cambios
    }

    private fun addGymkhanaMarkers(map: MapView, coords: List<GeoPoint>, names: List<String>, context: Context) {
        for (i in coords.indices) {
            val marker = Marker(map)
            marker.position = coords[i]
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.icon = ContextCompat.getDrawable(context, android.R.drawable.btn_star_big_on)
            marker.title = names[i]
            marker.snippet = "Punto de control ${i + 1} del Tour UPM" // Descripción
            map.overlays.add(marker)
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}
package com.example.taller3

import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taller3.databinding.ActivityMapBinding
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import com.example.taller3.model.JsonLocation
import org.json.JSONObject

class MapActivity : AppCompatActivity(), LocationListener {
    private lateinit var binding: ActivityMapBinding
    lateinit var jsonLocations : MutableList<JsonLocation>



    private lateinit var map: MapView
    private lateinit var mapController: IMapController
    private lateinit var locationManager: LocationManager
    private var currentLocationMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración inicial del mapa
        Configuration.getInstance().load(applicationContext, androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext))
        map = binding.osMap
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        mapController = map.controller
        mapController.setZoom(18.0)

        // Inicializar el LocationManager
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        requestLocationUpdates()

        val toolbar = findViewById<Toolbar>(R.id.toolbar_menu)
        setSupportActionBar(toolbar)

        loadJson()
        setJsonLocations()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_mapa, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val clicked = item.itemId
        if(clicked == R.id.logOut){
            //auth.signOut()
            val i = Intent(this, MainActivity::class.java)
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
        }
        return super.onOptionsItemSelected(item)
    }

    fun loadJson() {
        jsonLocations = mutableListOf<JsonLocation>()
        val json_string = this.assets.open("locations.json").bufferedReader().use{ it.readText() }
        var json = JSONObject(json_string);
        var locationJsonArray = json.getJSONArray("locationsArray");
        for (i in 0..locationJsonArray.length()-1) {
            val jsonObject = locationJsonArray.getJSONObject(i)
            val latitude = jsonObject.getString("latitude").toDouble()
            val longitude = jsonObject.getString("longitude").toDouble()
            val name = jsonObject.getString("name")
            val jsonLocation = JsonLocation(latitude, longitude, name)
            jsonLocations.add(jsonLocation)
        }
    }

    fun setJsonLocations(){
        for(i in jsonLocations){
            var geoPoint = GeoPoint(i.latitud, i.longitud)
            var marker = Marker(map).apply {
                position = geoPoint
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = i.nombre

                icon = ContextCompat.getDrawable(this@MapActivity, R.drawable.baseline_museum_24)?.let { drawable ->
                    vectorToBitmap(drawable)
                }?.let { bitmap ->
                    BitmapDrawable(resources, bitmap)
                }

                map.overlays.add(this)
            }
        }
    }

    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 30f, this)
            val lastKnownLocation: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            lastKnownLocation?.let { onLocationChanged(it) }
        }
    }


    override fun onLocationChanged(location: Location) {
        val newGeoPoint = GeoPoint(location.latitude, location.longitude)

        // Crear o actualizar el marcador de ubicación actual con el ícono personalizado
        if (currentLocationMarker == null) {
            currentLocationMarker = Marker(map).apply {
                position = newGeoPoint
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Mi Ubicación"

                // Configurar el ícono personalizado para el marcador
                icon = ContextCompat.getDrawable(this@MapActivity, R.drawable.baseline_location_pin_24)?.let { drawable ->
                    vectorToBitmap(drawable)
                }?.let { bitmap ->
                    BitmapDrawable(resources, bitmap)
                }

                map.overlays.add(this)
            }
        } else {
            currentLocationMarker?.position = newGeoPoint
        }

        // Mover la cámara a la nueva ubicación
        mapController.animateTo(newGeoPoint)
        map.invalidate()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults) // Llama a la implementación de super

        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocationUpdates()
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }
}

    fun vectorToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }


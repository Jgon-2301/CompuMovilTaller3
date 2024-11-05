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
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taller3.databinding.ActivityMapBinding
import com.example.taller3.model.JsonLocation
import com.example.taller3.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONObject
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import com.google.firebase.storage.FirebaseStorage

class MapActivity : AppCompatActivity(), LocationListener {
    private lateinit var binding: ActivityMapBinding


    private lateinit var map: MapView
    private lateinit var mapController: IMapController
    private lateinit var locationManager: LocationManager
    private var currentLocationMarker: Marker? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val userUid = FirebaseAuth.getInstance().currentUser?.uid
    private val USERS = "users/"
    private val TAG = "FIREBASE_APP"
    private val storageRef = FirebaseStorage.getInstance().reference
    private var jsonLocations = mutableListOf<JsonLocation>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Configuración inicial del mapa
        Configuration.getInstance().load(applicationContext, androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext))
        map = binding.osMap
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        mapController = map.controller
        mapController.setZoom(18.0)

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
        return when (item.itemId) {
            R.id.logOut -> {
                mAuth.signOut()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                true
            }
            R.id.dispobileButton -> {
                cambiarDisponibilidad()
                true
            }
            R.id.listaUsuariosDisponibles -> {
                startActivity(Intent(this, ActiveUsersActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun cambiarDisponibilidad() {
        val user = mAuth.currentUser
        if (user != null) {
            val userRef = database.getReference(USERS).child(user.uid)
            userRef.child("available").setValue(true)
            Toast.makeText(this, "Estado de disponibilidad actualizado", Toast.LENGTH_SHORT).show()
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
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { onLocationChanged(it) }
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

        userUid?.let { uid ->
            val userRef = database.getReference("users").child(uid)
            userRef.child("latitude").setValue(location.latitude)
            userRef.child("longitude").setValue(location.longitude)
                .addOnSuccessListener {
                    Log.d("MapActivity", "Latitud y longitud actualizadas en Firebase.")
                }
                .addOnFailureListener { exception ->
                    Log.e("MapActivity", "Error al actualizar la ubicación: ${exception.message}")
                }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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

    private fun vectorToBitmap(drawable: Drawable): Bitmap {
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

    // Función para cargar ubicaciones desde un archivo JSON en el directorio de assets
    private fun loadJson() {
        jsonLocations = mutableListOf<JsonLocation>()
        val jsonString = this.assets.open("locations.json").bufferedReader().use { it.readText() }
        val json = JSONObject(jsonString)
        val locationJsonArray = json.getJSONArray("locationsArray")
        for (i in 0 until locationJsonArray.length()) {
            val jsonObject = locationJsonArray.getJSONObject(i)
            val latitude = jsonObject.getString("latitude").toDouble()
            val longitude = jsonObject.getString("longitude").toDouble()
            val name = jsonObject.getString("name")
            val jsonLocation = JsonLocation(latitude, longitude, name)
            jsonLocations.add(jsonLocation)
        }
    }

    // Función para configurar los marcadores en el mapa usando las ubicaciones del archivo JSON
    private fun setJsonLocations() {
        for (location in jsonLocations) {
            val geoPoint = GeoPoint(location.latitud, location.longitud)
            val marker = Marker(map).apply {
                position = geoPoint
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = location.nombre
                icon = ContextCompat.getDrawable(this@MapActivity, R.drawable.baseline_museum_24)?.let { drawable ->
                    vectorToBitmap(drawable)
                }?.let { bitmap ->
                    BitmapDrawable(resources, bitmap)
                }
                map.overlays.add(this)
            }
        }
    }
}





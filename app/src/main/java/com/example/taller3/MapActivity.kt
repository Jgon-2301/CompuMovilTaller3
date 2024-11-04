package com.example.taller3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taller3.databinding.ActivityMapBinding
import com.example.taller3.model.JsonLocation
import org.json.JSONObject

class MapActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapBinding
    lateinit var jsonLocations : MutableList<JsonLocation>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_menu)
        setSupportActionBar(toolbar)

        loadJson()

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
}
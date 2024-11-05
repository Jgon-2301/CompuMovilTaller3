package com.example.taller3

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taller3.adapters.UserRecyclerAdapter
import com.example.taller3.databinding.ActivityActiveUsersBinding
import com.example.taller3.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ActiveUsersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityActiveUsersBinding
    private lateinit var adapter: UserRecyclerAdapter
    private val database = FirebaseDatabase.getInstance().reference.child("users")
    private val availableUsers = mutableListOf<User>()
    private val storageRef = FirebaseStorage.getInstance().reference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActiveUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura el RecyclerView
        adapter = UserRecyclerAdapter(availableUsers) { user ->
            showUserLocationOnMap(user)
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // Cargar usuarios disponibles desde Firebase
        loadAvailableUsers()
    }

    private fun loadAvailableUsers() {
        database.orderByChild("available").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    availableUsers.clear()
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)
                        if (user != null) {
                            loadImageWithFallback(user, userSnapshot.key ?: "")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ActiveUsersActivity, "Error al cargar usuarios", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadImageWithFallback(user: User, userId: String) {
        val possibleExtensions = listOf("jpg", "png", "jpeg") // Lista de extensiones soportadas
        tryLoadingImage(user, userId, possibleExtensions, 0)
    }

    private fun tryLoadingImage(user: User, userId: String, extensions: List<String>, index: Int) {
        if (index >= extensions.size) {
            // No se encontró ninguna imagen en los formatos especificados
            user.profileImageUrl = null
            availableUsers.add(user)
            checkAndNotifyAdapter()
            return
        }

        val currentExtension = extensions[index]
        val profileImageRef = storageRef.child("images/$userId.$currentExtension")

        profileImageRef.downloadUrl.addOnSuccessListener { uri ->
            // Si se encuentra la imagen, asigna la URL
            user.profileImageUrl = uri.toString()
            availableUsers.add(user)
            checkAndNotifyAdapter()
        }.addOnFailureListener {
            // Si falla, intenta con el siguiente formato
            tryLoadingImage(user, userId, extensions, index + 1)
        }
    }

    private fun checkAndNotifyAdapter() {
        if (availableUsers.size == adapter.itemCount) {
            adapter.notifyDataSetChanged()
        }
    }

    private fun showUserLocationOnMap(user: User) {
        val intent = Intent(this, MapActivity::class.java).apply {
            putExtra("latitude", user.latitude)
            putExtra("longitude", user.longitude)
            putExtra("userName", "${user.name} ${user.lastName}")
        }
        startActivity(intent)
    }
}

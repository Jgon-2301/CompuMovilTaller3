package com.example.taller3.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.taller3.MainActivity
import com.example.taller3.OtherUserMapActivity
import com.example.taller3.model.User
import com.google.firebase.auth.FirebaseAuth
import com.example.taller3.R
import com.google.firebase.database.*

class UserStatusService : Service() {

    private val database = FirebaseDatabase.getInstance().reference.child("users")
    private val auth = FirebaseAuth.getInstance()
    private lateinit var userListener: ValueEventListener

    private val userStatusMap = mutableMapOf<String, Boolean>()


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        listenForAvailableUsers()
    }

    private fun listenForAvailableUsers() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    val userId = userSnapshot.key

                    if (user != null && userId != null) {
                        userStatusMap[userId.toString()] = user.available
                    }
                }

                addRealtimeListener()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserStatusService", "Error al cargar usuarios iniciales: ${error.message}")
            }
        })
    }

    private fun addRealtimeListener() {
        userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    val currentUser = auth.currentUser
                    val userId = userSnapshot.key

                    if (user != null && currentUser != null && userId != currentUser.uid) {
                        val wasAvailable = userStatusMap[userId.toString()] ?: false
                        if (user.available && !wasAvailable) {
                            sendNotification(user)
                        }
                        userStatusMap[userId.toString()] = user.available
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserStatusService", "Error al escuchar cambios: ${error.message}")
            }
        }
        database.addValueEventListener(userListener)
    }

    private fun sendNotification(user: User) {
        val intent = Intent(this, OtherUserMapActivity::class.java).apply {
            putExtra("latitude", user.latitude)
            putExtra("longitude", user.longitude)
            putExtra("userName", "${user.name} ${user.lastName}")
        }

        val targetActivity = if (auth.currentUser != null) OtherUserMapActivity::class.java else MainActivity::class.java
        intent.setClass(this, targetActivity)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, "USER_STATUS_CHANNEL")
            .setContentTitle("Usuario disponible")
            .setContentText("${user.name} ${user.lastName} está ahora disponible.")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "USER_STATUS_CHANNEL",
                "Notificaciones de Usuarios",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Notificaciones cuando un usuario se pone disponible"
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        database.removeEventListener(userListener)
    }
}

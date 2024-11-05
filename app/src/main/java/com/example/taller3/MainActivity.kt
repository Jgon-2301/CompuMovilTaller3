package com.example.taller3

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taller3.databinding.ActivityLoginBinding
import com.example.taller3.databinding.ActivityMainBinding
import com.example.taller3.databinding.ActivityRegisterBinding
import com.example.taller3.services.UserStatusService

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            comenzarServicioNotificaciones()
        } else {
            Toast.makeText(this, "Permiso de notificaciones denegado", Toast.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.loginButton.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.registerButton.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        permisoNotificacionesYServicio(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun permisoNotificacionesYServicio(permission:String) {
        if(ContextCompat.checkSelfPermission(this,permission)== PackageManager.PERMISSION_DENIED){
            if(shouldShowRequestPermissionRationale(permission)){
                Toast.makeText(this,"Por favor acepte las notificaciones para poder recibir notificaciones",
                    Toast.LENGTH_LONG).show()
            }
            notificationPermissionLauncher.launch(permission)
        }else{
            comenzarServicioNotificaciones()
        }
    }

    private fun comenzarServicioNotificaciones() {
        val intent = Intent(this, UserStatusService::class.java)
        startService(intent)
    }


}
package com.example.taller3

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taller3.databinding.ActivityOtherUserMapBinding

class OtherUserMapActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtherUserMapBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtherUserMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
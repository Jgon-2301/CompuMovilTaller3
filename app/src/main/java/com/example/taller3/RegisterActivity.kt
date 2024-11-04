package com.example.taller3

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.taller3.databinding.ActivityRegisterBinding
import com.example.taller3.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val myRef by lazy { database.getReference("message") }
    private val USERS = "users/"
    private val TAG = "FIREBASE_APP"
    private val storageRef = FirebaseStorage.getInstance().reference
    private var selectedImageUri: Uri? = null


    val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            loadImage(uri)
        } else {
            Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar FirebaseAuth y Database
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.AlreadyHaveAccTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.imageButton.setOnClickListener {
            galleryLauncher.launch("image/*")

        }

        binding.registerButton.setOnClickListener {
            val email = binding.EmailAddress.text.toString()
            val password = binding.passwordEditText.text.toString()
            val firstName = binding.Name.text.toString()
            val lastName = binding.Surname.text.toString()
            val userId = binding.IDEditText.text.toString()

            if (validateForm(email, password, firstName, lastName, userId)) {
                createUser(email, password, firstName, lastName, userId)
            }
        }
    }


    private fun loadImage(uri: Uri) {
        val imageStream = getContentResolver().openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(imageStream)
        binding.imageButton.setImageBitmap(bitmap)
    }


    private fun createUser(email: String, password: String, firstName: String, lastName: String, userId: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:onComplete: ${task.isSuccessful}")
                    val user = mAuth.currentUser
                    if (user != null) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName("$firstName $lastName")
                            .setPhotoUri(selectedImageUri)
                            .build()

                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener { profileTask ->
                                if (profileTask.isSuccessful) {
                                    Log.d(TAG, "User profile updated.")

                                    val newUser = User(
                                        name = firstName,
                                        lastName = lastName,
                                        email = email,
                                        id_number = userId
                                    )

                                    saveUserDataToDatabase(user.uid, newUser)
                                    uploadImageToStorage(user)
                                    updateUI(user)
                                } else {
                                    Log.e(TAG, "Profile update failed: ${profileTask.exception?.message}")
                                    Toast.makeText(this, "Error al actualizar el perfil", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    val message = task.exception?.message ?: "Error desconocido"
                    Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "createUserWithEmail:failure", task.exception)
                }
            }
    }


    private fun saveUserDataToDatabase(uid: String, user: User) {
        database.getReference(USERS).child(uid).setValue(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User data saved to database.")
                } else {
                    Log.e(TAG, "Failed to save user data: ${task.exception?.message}")
                }
            }
    }

    private fun uploadImageToStorage(user: FirebaseUser) {
        selectedImageUri?.let { uri ->
            val imageRef = storageRef.child("images/${user.uid}.jpg")
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    Log.d(TAG, "Image uploaded successfully.")
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to upload image: ${exception.message}")
                }
        }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("email", currentUser.email)
            startActivity(intent)
            finish() // Cierra la actividad actual para evitar que el usuario vuelva atrás
        }
    }

    private fun validateForm(email: String, password: String, firstName: String, lastName: String, userId: String): Boolean {
        var valid = true
        if (email.isEmpty()) {
            binding.EmailAddress.error = "Campo requerido"
            valid = false
        }
        if (password.isEmpty()) {
            binding.passwordEditText.error = "Campo requerido"
            valid = false
        } else if (password.length < 6) {
            binding.passwordEditText.error = "La contraseña debe tener al menos 6 caracteres"
            valid = false
        }
        if (firstName.isEmpty()) {
            binding.Name.error = "Campo requerido"
            valid = false
        }
        if (lastName.isEmpty()) {
            binding.Surname.error = "Campo requerido"
            valid = false
        }
        if (userId.isEmpty()) {
            binding.IDEditText.error = "Campo requerido"
            valid = false
        }
        return valid
    }
}

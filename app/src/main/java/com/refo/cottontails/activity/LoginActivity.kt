package com.refo.cottontails.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.refo.cottontails.ShowSnackBar
import com.refo.cottontails.data.DataUser
import com.refo.cottontails.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val rootView = binding.rootView
        firebaseAuth = FirebaseAuth.getInstance()
        FirebaseApp.initializeApp(this)
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Pengguna telah login. Anda dapat melakukan apa pun yang diperlukan.
            val uid = currentUser.uid
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        } else {
            // Pengguna belum login atau sesi login telah kadaluarsa. Anda perlu menunjukkan layar login.
        }
        supportActionBar?.hide()
        binding.textViewRegister.setOnClickListener{
            val intent = Intent(this,SignUpActivity::class.java)
            startActivity(intent)
        }
        binding.buttonSubmit.setOnClickListener{
            loginPengguna(rootView)
        }
    }

    private fun loginPengguna(rootView: ConstraintLayout) {
        val email = binding.editTextEmail.editText?.text.toString().trim()
        val password = binding.editTextPassword.editText?.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            if (isValidEmail(email)) {
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()

                }.addOnFailureListener {
                    rootView.ShowSnackBar("Username atau password yang anda masukan salah",
                        100,
                        rootView)
                }
            } else {
                rootView.ShowSnackBar("Tolong isi data email yang sesuai", 100, rootView)
            }
        } else {
            rootView.ShowSnackBar("Harap lengkapi data diatas", 100, rootView)
        }
    }

    fun isValidEmail(email: String): Boolean {
        val pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }
}
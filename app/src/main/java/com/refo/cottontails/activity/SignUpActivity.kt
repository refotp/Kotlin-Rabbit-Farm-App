package com.refo.cottontails.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.refo.cottontails.ShowSnackBar
import com.refo.cottontails.data.DataUser
import com.refo.cottontails.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        val rootView = binding.rootView
        firebaseAuth = FirebaseAuth.getInstance()

        binding.buttonSubmit.setOnClickListener{
            registerPengguna(rootView)
        }
        binding.textViewRegister.setOnClickListener{
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun registerPengguna(rootView: ConstraintLayout) {
        val email = binding.editTextEmail.editText?.text.toString().trim()
        val password = binding.editTextPassword.editText?.text.toString()
        val confirmPassword = binding.editTextConfirmPassword.editText?.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
            if (isValidEmail(email)) {
                if (password.length > 5) {
                    if (!password.contains(" ")) {
                        if (password == confirmPassword) {
                            firebaseAuth.createUserWithEmailAndPassword(email, password)
                                .addOnSuccessListener {
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                        } else {
                            rootView.ShowSnackBar("Tolong cek ulang konfirmasi password anda",
                                100,
                                rootView)
                        }
                    } else {
                        rootView.ShowSnackBar("Password tidak boleh mengandung spasi",
                            100,
                            rootView)
                    }
                } else {
                    rootView.ShowSnackBar("Password harus lebih dari 8 karakter", 100, rootView)
                }
            } else {
                rootView.ShowSnackBar("Tolong isi data email yang sesuai", 100, rootView)
            }
        } else {
            rootView.ShowSnackBar("Harap lengkapi data diatas", 100, rootView)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }
}
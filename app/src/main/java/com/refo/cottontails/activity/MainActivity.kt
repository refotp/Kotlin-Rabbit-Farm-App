package com.refo.cottontails.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.navigation.NavigationBarView
import com.refo.cottontails.databinding.ActivityMainBinding
import androidx.fragment.app.commit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.refo.cottontails.R
import com.refo.cottontails.enableFirebasePersistence
import com.refo.cottontails.fragments.DataTernakFragment
import com.refo.cottontails.fragments.HomeFragment
import com.refo.cottontails.fragments.KeuanganFragment
import com.refo.cottontails.fragments.PerawatanFragment

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener{
    private lateinit var binding : ActivityMainBinding
    private var backButtonPressedTwice = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        if (currentUser==null){
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.bottomNav.background = null
        binding.bottomNav.menu.getItem(2).isEnabled = false
        binding.bottomNav.setOnItemSelectedListener (this)
        binding.fabTambahTernak.setOnClickListener{
            tambahTernak()
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backButtonPressedTwice) {
                    finish()
                } else {
                    backButtonPressedTwice = true
                    Toast.makeText(this@MainActivity, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()

                    Handler(Looper.getMainLooper()).postDelayed({
                        backButtonPressedTwice = false
                    }, 2000)
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)

    }


    override fun onNavigationItemSelected(item: MenuItem) = when(item.itemId) {
        R.id.nav_rabbit -> dataTernakClicked()
        R.id.nav_home -> homeClicked()
        R.id.nav_accounting -> keuanganClicked()
        R.id.nav_notes -> perawatanClicked()
        else -> false
    }

    private fun perawatanClicked(): Boolean {
        supportFragmentManager.commit{
            replace(R.id.frame_layout_fragment, PerawatanFragment())
        }
        return true
    }


    private fun keuanganClicked(): Boolean {
        supportFragmentManager.commit{
            replace(R.id.frame_layout_fragment, KeuanganFragment())
        }
        return true

    }

    private fun homeClicked(): Boolean {
        supportFragmentManager.commit{
            replace(R.id.frame_layout_fragment, HomeFragment())
        }
        return true
    }

    private fun tambahTernak(){
        val intent = Intent(this, InputTernakActivity::class.java)
        startActivity(intent)
    }
    private fun dataTernakClicked() : Boolean{
        supportFragmentManager.commit{
            replace(R.id.frame_layout_fragment, DataTernakFragment())
        }
        return true
    }

}
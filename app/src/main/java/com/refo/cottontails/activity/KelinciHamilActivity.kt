package com.refo.cottontails.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.refo.cottontails.adapter.KelinciHamilRecyclerViewAdapter
import com.refo.cottontails.data.DataKawin
import com.refo.cottontails.data.DataKelinciKawin
import com.refo.cottontails.databinding.ActivityKelinciHamilBinding
import java.text.SimpleDateFormat
import java.util.*

class KelinciHamilActivity : AppCompatActivity() {
    private lateinit var binding : ActivityKelinciHamilBinding
    private lateinit var recyclerView : RecyclerView
    private var data = ArrayList<DataKawin>()
    val referenceKelinci = FirebaseDatabase.getInstance().getReference("users")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKelinciHamilBinding.inflate(layoutInflater)
        setContentView(binding.root)
        data.clear()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        getKelinciKawin(userId)
        supportActionBar?.hide()
        binding.imageViewBackButton.setOnClickListener{
            onBackPressed()
        }

    }
    private fun getKelinciKawin(userId: String?) {
        val referenceKawin = FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("kawin")
        val kelinciKawinList = mutableListOf<DataKelinciKawin>()
        recyclerView = binding.recyclerViewCardKawin
        val adapter = KelinciHamilRecyclerViewAdapter(kelinciKawinList,this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        referenceKawin.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                kelinciKawinList.clear()
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val id =
                            dataSnapshot.child("id").getValue(String::class.java)
                        val namaKelinci =
                            dataSnapshot.child("kelinciBetina").getValue(String::class.java) ?: ""
                        val tanggalKawin =
                            dataSnapshot.child("tanggal").getValue(String::class.java) ?: ""
                        val kelinciJantan =
                            dataSnapshot.child("kelinciJantan").getValue(String::class.java) ?:""

                        referenceKelinci.child(userId).child("kelinci").orderByChild("namaKelinci").equalTo(namaKelinci)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (dataSnapshot in snapshot.children) {
                                        val fotoKelinci = dataSnapshot.child("fotoKelinci")
                                            .getValue(String::class.java) ?: ""
                                        val kawinKelinci =
                                            DataKelinciKawin(id!!,namaKelinci, kelinciJantan,fotoKelinci, tanggalKawin)
                                        kelinciKawinList.add(kawinKelinci)
                                    }
                                    // Update adapter dengan data yang sudah diambil
                                    filterDataKelinciHamil(kelinciKawinList,adapter)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    // Handle error
                                }
                            })
                    }
                } else {
                    // Handle case when snapshot doesn't exist
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun filterDataKelinciHamil(kelinciKawinList: MutableList<DataKelinciKawin>, adapter : KelinciHamilRecyclerViewAdapter) {
        val currentDate = Calendar.getInstance().time // Mendapatkan tanggal saat ini
        val dataTerurut = kelinciKawinList.map { kelinci ->
            val sdfInput = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val tanggalKawinDate = sdfInput.parse(kelinci.tanggalKawin)
            val perbedaanTanggal = if (tanggalKawinDate != null) {
                val cal = Calendar.getInstance()
                cal.time = tanggalKawinDate
                cal.add(Calendar.DAY_OF_MONTH, 30) // Tambahkan 30 hari untuk menghitung HPL
                cal.time.time - currentDate.time // Hitung perbedaan
            } else {
                -1L // Tanggal tidak valid
            }
            kelinci.copy(perbedaanTanggal = perbedaanTanggal)
        }

        // Bagi data menjadi dua kelompok: satu dengan HPL setelah tanggal hari ini, dan satu dengan HPL sebelum tanggal hari ini
        val dataSetelahHariIni = dataTerurut.filter { it.perbedaanTanggal >= 0 }
            .sortedBy { it.perbedaanTanggal }
        val dataSebelumHariIni = dataTerurut.filter { it.perbedaanTanggal < 0 }
            .sortedBy { it.perbedaanTanggal }

        // Gabungkan kedua kelompok data, dengan data yang sesudah hari ini di atas
        val dataHasil = dataSetelahHariIni + dataSebelumHariIni

        // Update adapter dengan data yang sudah difilter dan diurutkan
        adapter.setData(dataHasil)


    }
}
package com.refo.cottontails.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.refo.cottontails.adapter.PeriksaKesehatanRecyclerViewAdapter
import com.refo.cottontails.data.DataKawin
import com.refo.cottontails.data.DataKelinciPeriksa
import com.refo.cottontails.databinding.ActivityPeriksaKesehatanBinding
import com.refo.cottontails.enableFirebasePersistence
import java.text.SimpleDateFormat
import java.util.*

class PeriksaKesehatanActivity : AppCompatActivity() {
    private lateinit var binding : ActivityPeriksaKesehatanBinding
    private lateinit var recyclerView : RecyclerView
    private var data = ArrayList<DataKawin>()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid
    val referenceKelinci = FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("kelinci")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPeriksaKesehatanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        data.clear()
        supportActionBar?.hide()
        val referencePeriksa = FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("periksa")

        val kelinciPeriksaList = mutableListOf<DataKelinciPeriksa>()
        recyclerView = binding.recyclerViewCardPeriksa
        val adapter = PeriksaKesehatanRecyclerViewAdapter(kelinciPeriksaList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        getPeriksaKelinci(referencePeriksa, kelinciPeriksaList, adapter)

    }

    private fun getPeriksaKelinci(
        referencePeriksa: DatabaseReference,
        kelinciPeriksaList: MutableList<DataKelinciPeriksa>,
        adapter: PeriksaKesehatanRecyclerViewAdapter,
    ) {
        referencePeriksa.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val namaKelinci =
                            dataSnapshot.child("namaKelinci").getValue(String::class.java) ?: ""
                        val keterangan =
                            dataSnapshot.child("keterangan").getValue(String::class.java) ?: ""
                        val tanggal =
                            dataSnapshot.child("tanggal").getValue(String::class.java) ?: ""
                        referenceKelinci.orderByChild("namaKelinci").equalTo(namaKelinci)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (dataSnapshot in snapshot.children) {
                                        val fotoKelinci = dataSnapshot.child("fotoKelinci")
                                            .getValue(String::class.java) ?: ""
                                        val kelinciPeriksa = DataKelinciPeriksa(namaKelinci,
                                            fotoKelinci,
                                            tanggal,
                                            keterangan)
                                        kelinciPeriksaList.add(kelinciPeriksa)
                                        kelinciPeriksaList.sortByDescending {
                                            val dateFormatter =
                                                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                            dateFormatter.parse(it.tanggalPeriksa)
                                        }

                                        adapter.notifyDataSetChanged()
                                    }


                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                            })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun filterKelinciPeriksaList(kelinciPeriksaList: MutableList<DataKelinciPeriksa>, adapter: PeriksaKesehatanRecyclerViewAdapter) {
        val currentDate = Calendar.getInstance().time
        val dataTerurut = kelinciPeriksaList.map { kelinci ->
            val sdfInput = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val tanggalPeriksa = sdfInput.parse(kelinci.tanggalPeriksa)
            val cal = Calendar.getInstance()
            cal.time = tanggalPeriksa
            cal.time.time - currentDate.time
        }
    }
}
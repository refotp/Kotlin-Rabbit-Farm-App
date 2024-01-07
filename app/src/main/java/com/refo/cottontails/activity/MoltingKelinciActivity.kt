package com.refo.cottontails.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.refo.cottontails.adapter.MoltingRecyclerViewAdapter
import com.refo.cottontails.data.DataKelinci
import com.refo.cottontails.databinding.ActivityMoltingKelinciBinding
import com.refo.cottontails.enableFirebasePersistence
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MoltingKelinciActivity : AppCompatActivity() {
    private lateinit var adapter: MoltingRecyclerViewAdapter
    private var data = ArrayList<DataKelinci>()
    var databaseReference: DatabaseReference? = null
    var eventListener: ValueEventListener? = null
    private lateinit var binding: ActivityMoltingKelinciBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoltingKelinciBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        adapter = MoltingRecyclerViewAdapter(data, this)
        databaseReference = FirebaseDatabase.getInstance().getReference("kelinci").child("aktif")

        binding.recyclerViewMolting.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewMolting.adapter = adapter

        data.clear()
        eventListener = databaseReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                data.clear() // Hapus data sebelumnya

                for (itemSnapshot in snapshot.children) {
                    val dataKelinci = itemSnapshot.getValue(DataKelinci::class.java)
                    dataKelinci?.let {
                        // Filter kelinci yang memenuhi kriteria usia molting
                        if (isWithinMoltingAge(it.tanggalLahir)) {
                            data.add(it)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase Error", "Error: ${error.message}")
            }
        })
    }

    private fun isWithinMoltingAge(tanggalLahir: String?): Boolean {
        if (tanggalLahir != null) {
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val tanggalLahirKelinci: Date = dateFormat.parse(tanggalLahir)!!
            val calendar = Calendar.getInstance()
            val today = calendar.time
            val daysPassed = ((today.time - tanggalLahirKelinci.time) / (1000 * 60 * 60 * 24)).toInt()

            return daysPassed >= 0 && daysPassed <= 120 // Ubah batasan ke 0-120 hari
        }
        return false // Jika tanggal lahir tidak valid, kembalikan false
    }
}
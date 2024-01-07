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
import com.refo.cottontails.adapter.PakanTambahanRecyclerViewAdapter
import com.refo.cottontails.data.DataPakanTambahan
import com.refo.cottontails.databinding.ActivityPakanTambahanBinding
import java.text.SimpleDateFormat
import java.util.*

class PakanTambahanActivity : AppCompatActivity() {
    private lateinit var binding : ActivityPakanTambahanBinding
    private lateinit var recyclerView : RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPakanTambahanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        getPakanTambahan(userId)
        binding.imageViewBackButton.setOnClickListener{
            onBackPressed()
        }

    }

    private fun getPakanTambahan(userId: String?) {
        val referencePakanTambahan = FirebaseDatabase.getInstance().getReference("users").child(userId!!)
            .child("pakan")
        val pakanTambahanList = mutableListOf<DataPakanTambahan>()
        val adapter = PakanTambahanRecyclerViewAdapter(pakanTambahanList)
        recyclerView = binding.recyclerViewCardPakan
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        referencePakanTambahan.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (dataSnapshot in snapshot.children){
                        val data = dataSnapshot.getValue(DataPakanTambahan::class.java)
                        pakanTambahanList.add(data!!)
                        pakanTambahanList.sortByDescending {
                            val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale("id","ID"))
                            dateFormatter.parse(it.tanggalPemberian)
                        }
                        adapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}
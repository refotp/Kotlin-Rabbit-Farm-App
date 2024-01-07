package com.refo.cottontails.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.refo.cottontails.R
import com.refo.cottontails.adapter.TernakRecyclerViewAdapter
import com.refo.cottontails.data.DataKelinci
import com.refo.cottontails.databinding.ActivityHistoryKelinciBinding
import com.refo.cottontails.enableFirebasePersistence

class HistoryKelinciActivity : AppCompatActivity() {
    private lateinit var binding : ActivityHistoryKelinciBinding
    private lateinit var adapter : TernakRecyclerViewAdapter
    private var data = ArrayList<DataKelinci>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryKelinciBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        adapter = TernakRecyclerViewAdapter(data, this)
        binding.recyclerViewDataTernak.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewDataTernak.adapter = adapter
        data.clear()


        FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("kelinci")
            .orderByChild("tipe")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val dataKelinci = snapshot.getValue(DataKelinci::class.java)

                    dataKelinci?.let {
                        // Filter dan urutkan data berdasarkan tipe
                        if (it.tipe == "Hidup") {
                            data.add(0, it)
                        } else if (it.tipe == "Terjual") {
                            val index = data.indexOfFirst { kelinci -> kelinci.tipe != "Hidup" }
                            if (index != -1) {
                                data.add(index, it)
                            } else {
                                data.add(it)
                            }
                        } else {
                            data.add(it)
                        }

                        adapter.notifyDataSetChanged()
                    }
                }

                  override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                  }

                  override fun onChildRemoved(snapshot: DataSnapshot) {
                      val removedData = snapshot.getValue(DataKelinci::class.java)
                      removedData?.let {
                          data.remove(it)
                          adapter.notifyDataSetChanged()
                      }
                  }

                  override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                      TODO("Not yet implemented")
                  }

                  override fun onCancelled(error: DatabaseError) {
                      TODO("Not yet implemented")
                  }

              })

    }
}

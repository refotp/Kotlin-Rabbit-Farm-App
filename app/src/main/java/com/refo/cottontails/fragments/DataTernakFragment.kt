package com.refo.cottontails.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.view.View.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.refo.cottontails.databinding.FragmentDataTernakBinding
import java.util.*
import androidx.appcompat.widget.SearchView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.refo.cottontails.activity.HistoryKelinciActivity
import com.refo.cottontails.adapter.TernakRecyclerViewAdapter
import com.refo.cottontails.data.DataKelinci
import com.refo.cottontails.enableFirebasePersistence
import kotlin.collections.ArrayList


class DataTernakFragment :Fragment() {
    private lateinit var binding : FragmentDataTernakBinding
    private var data = ArrayList<DataKelinci>()
    private lateinit var adapter : TernakRecyclerViewAdapter
    var databaseReference:DatabaseReference?=null
    var eventListener:ValueEventListener?=null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        binding = FragmentDataTernakBinding.inflate(layoutInflater)
        adapter = TernakRecyclerViewAdapter(data, context!!)
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("kelinci")
        binding.recyclerViewDataTernak.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewDataTernak.adapter = adapter
        data.clear()

        binding.textViewHistoryKelinci.setOnClickListener{
            val intent = Intent(context,HistoryKelinciActivity::class.java)
            startActivity(intent)
        }
        getTernak()

        binding.searchBar.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchList(newText)
                return false
            }
        })

        return binding.root
    }

    private fun getTernak() {
        eventListener = databaseReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                data.clear() // Bersihkan data sebelum mengisi ulang

                for (childSnapshot in snapshot.children) {
                    if (childSnapshot.exists()) {
                        val dataKelinci = childSnapshot.getValue(DataKelinci::class.java)
                        val tipe = dataKelinci?.tipe.toString()
                        if (tipe == "aktif") {
                            dataKelinci?.let {
                                data.add(it)
                            }
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


    fun searchList(text: String) {
            val searchList = ArrayList<DataKelinci>()
            for (dataClass in data) {
                if (dataClass.namaKelinci?.lowercase()?.contains(text.lowercase(Locale.getDefault())) == true
                ) {
                    searchList.add(dataClass)
                }
            }
            adapter.searchData(searchList)
        }
    fun View.hide(){
        this.visibility = GONE
    }
    fun View.show(){
        this.visibility = VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}
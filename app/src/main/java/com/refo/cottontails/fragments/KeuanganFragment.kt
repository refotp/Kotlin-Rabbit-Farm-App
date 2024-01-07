package com.refo.cottontails.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.refo.cottontails.activity.InputPemasukanActivity
import com.refo.cottontails.activity.InputPengeluaranActivity
import com.refo.cottontails.adapter.KeuanganKategoriRecyclerViewAdapater
import com.refo.cottontails.data.DataPemasukan
import com.refo.cottontails.data.DataPengeluaran
import com.refo.cottontails.databinding.FragmentKeuanganBinding

class KeuanganFragment : Fragment() {
    private lateinit var binding : FragmentKeuanganBinding
    var hasilPakan = 0
    var hasilPerawatan = 0
    var hasilPerlengkapan = 0
    var hasilLainnya = 0
    var pengeluaran = mutableListOf<DataPengeluaran>()
    var pemasukan = mutableListOf<DataPemasukan>()
    var dataKeluar = mutableListOf<DataPengeluaran>()
    val totalPengeluaranMap = mutableMapOf<String, Int>()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid
    val referencePengeluaran = FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("pengeluaran")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentKeuanganBinding.inflate(inflater)
        setCardView()
        return binding.root

    }

    private fun setCardView() {
        val recyclerView = binding.recyclerViewCardKeuangan
        val adapter = KeuanganKategoriRecyclerViewAdapater(pengeluaran, totalPengeluaranMap)
        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // Data baru ditambahkan, hitung ulang total pengeluaran di sini
                val item = dataSnapshot.getValue(DataPengeluaran::class.java)
                pengeluaran.add(item!!)
                val jenisPengeluaran = item.jenisPengeluaran
                val jumlahUang = item.jumlahUang
                if (jenisPengeluaran != "") {

                    val total = totalPengeluaranMap.getOrDefault(jenisPengeluaran, 0)
                    totalPengeluaranMap[jenisPengeluaran] = jumlahUang + total
                    pengeluaran.sortByDescending { it.waktuTransaksi }

                    // Batasi data hanya hingga 10 data terakhir
                    if (pengeluaran.size > 10) {
                        pengeluaran.removeAt(pengeluaran.size - 1)
//                        adapter.updateData(pengeluaran,totalPengeluaranMap)
                    }
                    adapter.notifyDataSetChanged()


                }

            }


            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // Data anak berubah (misalnya nilai uang berubah), Anda juga bisa menghitung ulang di sini jika diperlukan

            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                // Data anak dihapus, Anda mungkin ingin mengurangi total pengeluaran jika diperlukan
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // Data anak dipindahkan, tidak ada penghitungan ulang yang diperlukan dalam kasus ini biasanya
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Penanganan kesalahan jika ada
            }

        }

        // Tambahkan childEventListener ke referencePengeluaran
        referencePengeluaran.addChildEventListener(childEventListener)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        dataKategoriPengeluaran()
        getPemasukan(userId)
        getPengeluaran(userId)



        binding.buttonInputPemasukan.setOnClickListener{
            val intent = Intent(context, InputPemasukanActivity::class.java)
            startActivity(intent)
        }
        binding.buttonInputPengeluaran.setOnClickListener{
            val intent = Intent(context, InputPengeluaranActivity::class.java)
            startActivity(intent)
        }



    }

    private fun dataKategoriPengeluaran() {
        referencePengeluaran.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val item = snapshot.getValue(DataPengeluaran::class.java)
                val jenisPengeluaran = snapshot.child("jenisPengeluaran").value.toString()
                calculateJenis(item,jenisPengeluaran)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle perubahan data anak
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Handle penghapusan data anak
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle pergerakan data anak
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle pembatalan
            }
        })
    }

    private fun calculateJenis(item: DataPengeluaran?, jenisPengeluaran: String) {
        if (item!=null){
            when(jenisPengeluaran){
                "Pakan" ->{
                    val uang = item.jumlahUang
                    hasilPakan += uang
                    displayText(hasilPakan,jenisPengeluaran)
                }
                "Perlengkapan" ->{
                    val uang = item.jumlahUang
                    hasilPerlengkapan += uang
                    displayText(hasilPerlengkapan,jenisPengeluaran)
                }
                "Perawatan" -> {
                    val uang = item.jumlahUang
                    hasilPerawatan += uang
                    displayText(hasilPerawatan,jenisPengeluaran)
                }
                "Lainnya" -> {
                    val uang = item.jumlahUang
                    hasilLainnya += uang
                    displayText(hasilLainnya,jenisPengeluaran)
                }
            }
        }
    }

//    private fun calculatePakan(item: DataPengeluaran?) {
//        if (item != null){
//            val uang = item.jumlahUang
//            hasil += uang
//        }
//        else{
//            hasil = 0
//        }
//        displayText()
//    }

    private fun displayText(hasil :Int ,jenisPengeluaran: String) {
        when(jenisPengeluaran){
            "Pakan" -> binding.textViewPengeluaranPakan.text = "Rp $hasil"
            "Perlengkapan" -> binding.textViewPengeluaranPerlengkapan.text = "Rp $hasil"
            "Perawatan" -> binding.textViewPengeluaranPerawatan.text = "Rp $hasil"
            "Lainnya" -> binding.textViewPengeluaranLain.text = "Rp $hasil"
        }

    }


    private fun getPengeluaran(userId: String?) {
        referencePengeluaran
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        var total = 0
                        for (dataSnapshot in snapshot.children){
                            val item = dataSnapshot.getValue(DataPengeluaran::class.java)
                            val item2 = dataSnapshot.child("jumlahUang").getValue(Int::class.java)
                            total += item2!!

                            }
                        val infoPengeluaran = "Rp $total"
                        binding.textViewInfoPengeluaran.text = infoPengeluaran


                        }else{
                            binding.textViewInfoPengeluaran.text = "Rp 0"
                    }
                    }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun getPemasukan(userId: String?) {
        FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("pemasukan")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        var total = 0
                        for (dataSnapshot in snapshot.children){
                            val item2 = dataSnapshot.child("jumlahUang").getValue(Int::class.java)
                            total += item2!!
                        }

                        val infoPemasukan = "Rp $total"
                        binding.textViewInfoPemasukan.text = infoPemasukan
//                        adapter.notifyDataSetChanged()
                    } else{
                        binding.textViewInfoPemasukan.text = "Rp 0"
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }




}
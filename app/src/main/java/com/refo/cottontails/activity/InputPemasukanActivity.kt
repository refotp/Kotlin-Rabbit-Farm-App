package com.refo.cottontails.activity


import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.refo.cottontails.R
import com.refo.cottontails.ShowSnackBar
import com.refo.cottontails.data.DataKelinci
import com.refo.cottontails.data.DataPemasukan
import com.refo.cottontails.databinding.ActivityInputPemasukanBinding
import java.text.DateFormat
import java.util.*

class InputPemasukanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputPemasukanBinding
    private lateinit var dbKel: DatabaseReference
    private lateinit var spinner: Spinner
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputPemasukanBinding.inflate(layoutInflater)

        setContentView(binding.root)
        supportActionBar?.hide()
        val editText = binding.editTextJumlahUang.editText?.text
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        dbKel = FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("kelinci")
        setSpinner()
        buttonAction(editText,userId)

    }
    private fun setSpinner() {
        spinner = binding.spinnerNamaKelinci
        dbKel.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nameList = mutableListOf<String>()
                for (dataSnapshot in snapshot.children) {
                    val item = dataSnapshot.child("namaKelinci").value
                    val tipe = dataSnapshot.child("tipe").value.toString()
                    if (item != null) {
                        if(tipe == "aktif"){
                            nameList.add(item.toString())
                        }

                    }

                }
                val adapter = ArrayAdapter(
                    this@InputPemasukanActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    nameList
                )
                spinner.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    private fun InputPemasukanActivity.buttonAction(editText: Editable?, userId: String) {
        binding.imageViewBackButton.setOnClickListener {
            onBackPressed()
        }

        binding.buttonSumbitForm.setOnClickListener {
            val inputUang = editText.toString().toInt()
            if(inputUang < 1000){
                val rootView = findViewById<View>(com.refo.cottontails.R.id.root_view)
                rootView.ShowSnackBar("Harap masukan nominal dengan benar",100,rootView)
            }
            else{
                val dialog2 = Dialog(this)
                dialog2.setContentView(R.layout.progress_layout)
                if (dialog2.window != null) {
                    dialog2.window!!.setBackgroundDrawable(ColorDrawable(0))
                }
                dialog2.show()
                tambahPemasukan(editText,userId)
            }
        }
    }

    private fun tambahPemasukan(editText: Editable?, userId: String) {
        val dbref = FirebaseDatabase.getInstance().reference
        val namaKelinci = spinner.selectedItem.toString()
        val jumlahUang = editText.toString().toInt()
        val idPemasukan = dbref.push().key!!
        val waktuSaatIni = DateFormat.getDateTimeInstance().format(Calendar.getInstance().time)
        findKelinciIdAndMoveToNonAktif(namaKelinci,userId,dbref,jumlahUang,idPemasukan,waktuSaatIni)

    }

    private fun findKelinciIdAndMoveToNonAktif(
        namaKelinci: String,
        userId: String,
        dbref: DatabaseReference,
        jumlahUang: Int,
        idPemasukan: String,
        waktuSaatIni: String
    ) {
        val kelinciRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("kelinci")

        kelinciRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (dataSnapshotKelinci in dataSnapshot.children) {
                        val idKelinci = dataSnapshotKelinci.key
                        val namaKelinciFirebase = dataSnapshotKelinci.child("namaKelinci").value.toString()
                        val item = dataSnapshot.getValue(DataKelinci::class.java)
                        if (namaKelinci == namaKelinciFirebase) {
                            changeStatusTipeKelinci(idKelinci,userId)
                            val dataPemasukan = DataPemasukan(idPemasukan, jumlahUang,namaKelinci, waktuSaatIni)
                            dbref.child("users").child(userId).child("pemasukan").child(idPemasukan)
                                .setValue(dataPemasukan)
                                .addOnSuccessListener {
                                    // Hanya jika data pemasukan berhasil ditambahkan, kita akan mencari ID kelinci dan memindahkan data ke node "non aktif"

                                }
                                .addOnFailureListener { exception ->
                                    // Handle kesalahan jika penambahan data pemasukan gagal
                                    // Misalnya, tampilkan pesan kesalahan kepada pengguna
                                    val rootView = findViewById<View>(R.id.root_view)
                                    rootView.ShowSnackBar("Gagal menambahkan data pemasukan", 100, rootView)
                                }
                            // Jika cocok, pindahkan data kelinci ke node "non aktif"
//                            moveDataToNonAktif(idKelinci!!)

                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle kesalahan jika diperlukan
            }
        })
    }

    private fun changeStatusTipeKelinci(idKelinci: String?, userId: String) {
        val kelinciRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("kelinci").child(idKelinci!!)
        kelinciRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Ambil data kelinci dari "aktif"
                    val dataKelinci = dataSnapshot.getValue(DataKelinci::class.java)

                    // Ubah status menjadi "Terjual"
                    dataKelinci?.status = "Terjual"
                    dataKelinci?.tipe = "non_aktif"

                    // Salin data yang diperbarui ke node "non aktif" di bawah node "kelinci"
                    kelinciRef.setValue(dataKelinci)
                        .addOnSuccessListener {
                            // Jika berhasil disalin, hapus data dari node "aktif"
                            finish()
                        }
                        .addOnFailureListener { exception ->
                            // Handle kesalahan saat menyalin data ke "non aktif" jika diperlukan
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle kesalahan saat membaca data dari "aktif" jika diperlukan
            }
        })
    }

//    private fun moveDataToNonAktif(idKel: String) {
//        val database = FirebaseDatabase.getInstance()
//        val kelinciAktifRef = database.getReference("kelinci").child("aktif").child(idKel)
//        val kelinciNonAktifRef = database.getReference("kelinci").child("non_aktif").child(idKel) // Membuat child "non aktif" di bawah node "kelinci"
//        kelinciAktifRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    // Ambil data kelinci dari "aktif"
//                    val dataKelinci = dataSnapshot.getValue(DataKelinci::class.java)
//
//                    // Ubah status menjadi "Terjual"
//                    dataKelinci?.status = "Teradopt"
//                    dataKelinci?.tipe = "non_aktif"
//
//                    // Salin data yang diperbarui ke node "non aktif" di bawah node "kelinci"
//                    kelinciNonAktifRef.setValue(dataKelinci)
//                        .addOnSuccessListener {
//                            // Jika berhasil disalin, hapus data dari node "aktif"
//                            kelinciAktifRef.removeValue()
//                                .addOnSuccessListener {
//                                    finish()
//                                }
//                                .addOnFailureListener { exception ->
//                                    // Handle kesalahan saat menghapus data dari "aktif" jika diperlukan
//                                }
//                        }
//                        .addOnFailureListener { exception ->
//                            // Handle kesalahan saat menyalin data ke "non aktif" jika diperlukan
//                        }
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Handle kesalahan saat membaca data dari "aktif" jika diperlukan
//            }
//        })
//    }



//    private fun moveDataToNonAktif(idKel: String) {
//        val database = FirebaseDatabase.getInstance()
//        val kelinciRef = database.getReference("kelinci").child(idKel)
//        val nonAktifRef = kelinciRef.child("non_aktif").push() // Membuat child "non aktif" di bawah node "kelinci"
//
//        kelinciRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    // Ubah status menjadi "Terjual"
//                    val dataKelinci = dataSnapshot.getValue(DataKelinci::class.java)
//                    dataKelinci?.status = "Terjual"
//
//                    // Salin data yang diperbarui ke node "non aktif" di bawah node "kelinci"
//                    nonAktifRef.setValue(dataKelinci)
//                        .addOnSuccessListener {
//                            // Jika berhasil disalin, hapus data dari node "kelinci"
//                            kelinciRef.removeValue()
//                                .addOnSuccessListener {
//                                    finish()
//                                }
//                                .addOnFailureListener { exception ->
//                                    // Handle kesalahan saat menghapus data dari "kelinci" jika diperlukan
//                                }
//                        }
//                        .addOnFailureListener { exception ->
//                            // Handle kesalahan saat menyalin data ke "non aktif" jika diperlukan
//                        }
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Handle kesalahan saat membaca data dari "kelinci" jika diperlukan
//            }
//        })
//    }
}



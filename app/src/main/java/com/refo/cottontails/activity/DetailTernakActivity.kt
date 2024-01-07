package com.refo.cottontails.activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.refo.cottontails.data.DataKelinci
import com.refo.cottontails.databinding.ActivityDetailTernakBinding
import java.text.SimpleDateFormat
import java.util.*

class DetailTernakActivity : AppCompatActivity() {
    private lateinit var dbRef : DatabaseReference
    private lateinit var binding : ActivityDetailTernakBinding
    private var status : String = ""
    private var foto : String =""
    override fun onResume() {
        super.onResume()
        reloadDataAndRefreshUI()
    }

    private fun reloadDataAndRefreshUI() {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val kelinci = dataSnapshot.getValue(DataKelinci::class.java)

                // Update tampilan sesuai dengan data yang baru
                if (kelinci != null) {
                    val lahir = kelinci.tanggalLahir
                    val kelamin = kelinci.jenisKelaminKelinci
                    val berat = kelinci.bobotKelinci
                    val bobot = berat.toString()
                    val jenis = kelinci.jenisKelinci
                    val indukJantan = kelinci.indukJantan
                    val indukBetina = kelinci.indukBetina
                    foto = kelinci.fotoKelinci.toString()
                    status = kelinci.status.toString()
                    val bobotKG = "$bobot Kg"

                    // Update tampilan dengan data yang baru
                    binding.textViewDescUsiaKelinci.text = lahir
                    binding.textViewDescKelaminKelinci.text = kelamin
                    binding.textViewDescBobotKelinci.text = bobotKG
                    binding.textViewDescIndukJantan.text = indukJantan
                    binding.textViewDescIndukBetina.text = indukBetina
                    binding.textViewDescStatusKelinci.text = status
                    if (!isDestroyed) {
                        Glide.with(this@DetailTernakActivity).load(kelinci.fotoKelinci).into(binding.imageViewDescKelinci)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTernakBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        val bundle = intent.extras
        val kelID = bundle!!.getString("Id")
        val tipe = bundle.getString("Parent")
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("kelinci").child(kelID!!)
        if (tipe == "non_aktif"){
            binding.buttonKelinciMati.visibility = View.GONE
        }
        else{
            binding.buttonKelinciMati.visibility = View.VISIBLE
        }
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val kelinci = dataSnapshot.getValue(DataKelinci::class.java)
                val lahir = kelinci?.tanggalLahir
                val kelamin = kelinci?.jenisKelaminKelinci
                val berat = kelinci?.bobotKelinci
                val bobot = berat.toString()
                val jenis = kelinci?.jenisKelinci
                val indukJantan = kelinci?.indukJantan
                val indukBetina = kelinci?.indukBetina
                foto = kelinci?.fotoKelinci.toString()
                status = kelinci?.status.toString()
                hitungUsia(kelinci!!.kelId, tipe!!,userId)
                val bobotKG = "$bobot Kg"

                if (bundle != null) {
                    binding.textViewNamaKelinci.text = bundle.getString("Nama")
                    binding.textViewDescJenisKelinci.text = jenis
                    binding.textViewDescKelinciId.text = bundle.getString("Id")
                    binding.textViewDescUsiaKelinci.text = lahir
                    binding.textViewDescKelaminKelinci.text = kelamin
                    binding.textViewDescBobotKelinci.text = bobotKG
                    binding.textViewDescIndukJantan.text = indukJantan
                    binding.textViewDescIndukBetina.text = indukBetina
                    binding.textViewDescStatusKelinci.text = status
                    if (!isDestroyed) {
                        Glide.with(this@DetailTernakActivity).load(kelinci.fotoKelinci).into(binding.imageViewDescKelinci)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        }

        dbRef.addValueEventListener(valueEventListener)
        binding.layoutIndukBetina.setOnClickListener{
            val namaInduk = binding.textViewDescIndukBetina.text.toString()
            if (namaInduk != ""){
                betinaClicked(namaInduk,userId)
            }
            else{
                Toast.makeText(this,"Tidak ada data untuk induk kelinci ini",Toast.LENGTH_LONG).show()
            }

        }
        binding.buttonKelinciMati.setOnClickListener{
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setMessage("Apakah Kamu yakin kelinci ini sudah mati ?")
                .setCancelable(false)
                .setPositiveButton("Iya", DialogInterface.OnClickListener {
                        dialog, id ->
                    val dialog2 = Dialog(this)
                    dialog2.setContentView(com.refo.cottontails.R.layout.progress_layout)
                    if (dialog2.window != null){
                        dialog2.window!!.setBackgroundDrawable(ColorDrawable(0))
                    }
                    dialog2.show()

                    kelinciMati(kelID,userId)
                    finish()
                })
                .setNegativeButton("Tidak", DialogInterface.OnClickListener {
                        dialog, id -> dialog.cancel()
                })
            val alert = dialogBuilder.create()
            alert.setTitle("Peringatan !")
            alert.show()

        }
        binding.buttonDeleteData.setOnClickListener{
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setMessage("Apakah Kamu yakin ingin menghapus kelinci ini?")
                .setCancelable(false)
                .setPositiveButton("Iya", DialogInterface.OnClickListener {
                        dialog, id ->
                    deleteKelinci(dbRef)
                    finish()
                })
                .setNegativeButton("Tidak", DialogInterface.OnClickListener {
                        dialog, id -> dialog.cancel()
                })
            val alert = dialogBuilder.create()
            alert.setTitle("Peringatan !")
            alert.show()

        }
        binding.buttonEditData.setOnClickListener{
            val intent = Intent(this, EditActivity::class.java)
            intent.putExtra("Id",kelID)
            intent.putExtra("Status",status)
            intent.putExtra("Tipe",tipe)
            intent.putExtra("Foto Kelinci",foto)
            finish()
            startActivity(intent)
        }

        binding.layoutIndukJantan.setOnClickListener{
            val namaInduk = binding.textViewDescIndukJantan.text.toString()
            if (namaInduk != ""){
                jantanClicked(namaInduk,userId)
            }
            else{
                Toast.makeText(this,"Tidak ada data untuk induk kelinci ini",Toast.LENGTH_LONG).show()
            }
        }

        val backButton = binding.imageViewBackButton
        backButton.setOnClickListener{
            onBackPressed()
        }
    }

    private fun kelinciMati(kelID: String,  userId: String) {
        FirebaseDatabase.getInstance().getReference("users").child(userId)
            .child("kelinci").child(kelID).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                    val item = snapshot.getValue(DataKelinci::class.java)
                    item?.status = "Mati"
                    item?.tipe = "non_aktif"
                   FirebaseDatabase.getInstance().getReference("users").child(userId).child("kelinci")
                       .child(kelID).setValue(item).addOnSuccessListener {
                        finish()
                    }
                        .addOnFailureListener{

                        }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }


    private fun deleteKelinci(dbRef: DatabaseReference) {
        dbRef.removeValue().addOnSuccessListener {
            finish()
        }
            .addOnFailureListener{

            }
    }


    private fun hitungUsia(keliID: String, node: String, userId: String) {
        val dbref = FirebaseDatabase.getInstance().getReference("users").child(userId).child("kelinci").child(keliID)
        dbref.get().addOnCompleteListener{
            if(it.isSuccessful){
                val user = it.result.getValue(DataKelinci::class.java)
                val usia = user?.tanggalLahir
                if (usia != ""){
                    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale("id", "ID"))
                    val tanggalLahir = dateFormat.parse(usia!!)
                    val tanggalHariIni = Date()
                    val calLahir = Calendar.getInstance()
                    calLahir.time = tanggalLahir!!
                    val calHariIni = Calendar.getInstance()
                    calHariIni.time = tanggalHariIni
                    val tahunLahir = calLahir.get(Calendar.YEAR)
                    val tahunHariIni = calHariIni.get(Calendar.YEAR)
                    val bulanLahir = calLahir.get(Calendar.MONTH)
                    val bulanHariIni = calHariIni.get(Calendar.MONTH)
                    val selisihTahun = tahunHariIni - tahunLahir
                    val selisihBulan = bulanHariIni - bulanLahir
                    val usiaBulan = selisihTahun * 12 + selisihBulan
                    if (usiaBulan<=0){
                        binding.textViewDescUsia.text = "< 1 Bulan"
                    }
                    else if (usiaBulan%12 == 0){
                        val usiaTahun = usiaBulan/12
                        val umur = "$usiaTahun Tahun"
                        binding.textViewDescUsia.text = umur
                    }
                    else if (usiaBulan>=12){
                        val usiaTahun = usiaBulan/12
                        val bulan = usiaBulan%12
                        val umur = "$usiaTahun tahun $bulan bulan"
                        binding.textViewDescUsia.text = umur
                    }

                    else{
                        val umur ="$usiaBulan bulan"
                        binding.textViewDescUsia.text = umur
                    }
                } else{
                    binding.textViewDescUsia.text = ""
                }
            }
        }

    }

    private fun betinaClicked(namaInduk: String, userId: String) {
        val database = FirebaseDatabase.getInstance().getReference("users").child(userId).child("kelinci")
        database.addListenerForSingleValueEvent(
            object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        for (dataSnapshot in snapshot.children){
                            val item = dataSnapshot.getValue(DataKelinci::class.java)
                            val namaKelinci = item?.namaKelinci.toString()
                            if (namaInduk == namaKelinci){
                                val kelId = item?.kelId
                                val tipe = item?.tipe
                                val intent = Intent(this@DetailTernakActivity,DetailTernakActivity::class.java)
                                intent.putExtra("Id",kelId)
                                intent.putExtra("Nama", namaInduk)
                                intent.putExtra("Parent",tipe)
                                finish()
                                startActivity(intent)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            }
        )
    }

    private fun jantanClicked(namaInduk: String, userId: String) {
        val database = FirebaseDatabase.getInstance().getReference("users").child(userId).child("kelinci")
        database.addListenerForSingleValueEvent(
            object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        for (dataSnapshot in snapshot.children){
                            val item = dataSnapshot.getValue(DataKelinci::class.java)
                            val namaKelinci = item?.namaKelinci.toString()
                            if (namaInduk == namaKelinci){
                                val kelId = item?.kelId
                                val tipe = item?.tipe
                                val intent = Intent(this@DetailTernakActivity,DetailTernakActivity::class.java)
                                intent.putExtra("Id",kelId)
                                intent.putExtra("Nama", namaInduk)
                                intent.putExtra("Parent",tipe)
                                finish()
                                startActivity(intent)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            }
        )
    }

}
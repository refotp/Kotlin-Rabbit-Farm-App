package com.refo.cottontails.activity

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.refo.cottontails.ShowSnackBar
import com.refo.cottontails.data.DataKelinci
import com.refo.cottontails.data.DataPeriksa
import com.refo.cottontails.databinding.ActivityInputPeriksaKesehatanBinding
import java.text.SimpleDateFormat
import java.util.*

class InputPeriksaKesehatanActivity : AppCompatActivity() {
    private lateinit var binding : ActivityInputPeriksaKesehatanBinding
    private lateinit var db : DatabaseReference
    private lateinit var spinner : Spinner
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputPeriksaKesehatanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        binding.imageViewBackButton.setOnClickListener{
            onBackPressed()
        }
        val rootView = binding.rootView
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        getKelinci(userId)
        getDate()
        binding.buttonSumbitForm.setOnClickListener{
            val cekTanggal = checkDate()
            val tanggal = binding.editTextTanggalPeriksa.editText?.text
            if (tanggal!!.isNotEmpty()){
                if (!cekTanggal){
                    rootView.ShowSnackBar("Harap masukan tanggal dengan benar!",100,rootView)
                }else{
                    tambahPeriksa(userId)
                }
            }
            else{
                rootView.ShowSnackBar("Harap masukan tanggal periksa terlebih dahulu", 100, rootView)
            }
        }
    }

    private fun checkDate() : Boolean{
        val cal = Calendar.getInstance()
        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(cal.time)
        val inputDateStr = binding.editTextTanggalPeriksa.editText?.text.toString()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val inputDate = dateFormat.parse(inputDateStr)
        val tanggalSaatIni = dateFormat.parse(currentDate)

        // Bandingkan tanggal yang diinputkan dengan tanggal saat ini
        return !(inputDate != null && inputDate.after(tanggalSaatIni))
    }

    private fun tambahPeriksa(userId: String?) {
        db = FirebaseDatabase.getInstance().reference
        val namaKelinci = binding.spinnerNamaKelinci.selectedItem.toString()
        FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("kelinci")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        for (dataSnapshot in snapshot.children){
                            val item = dataSnapshot.getValue(DataKelinci::class.java)
                            val namaKel = item?.namaKelinci
                            if (namaKelinci == namaKel){
                                val tanggal = binding.editTextTanggalPeriksa.editText?.text.toString()
                                val keterangan = binding.editTextKeterangan.editText?.text?.toString()?.trim()
                                val idPeriksa = db.push().key!!
                                val dataPeriksa = DataPeriksa(idPeriksa,namaKelinci,tanggal,keterangan)
                                FirebaseDatabase.getInstance().getReference("users").child(userId).child("periksa").child(idPeriksa)
                                    .setValue(dataPeriksa).addOnCompleteListener{
                                            periksa ->
                                        if (periksa.isSuccessful){
                                            finish()
                                        }
                                    }
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })


    }

    private fun getDate() {
        binding.buttonTanggalPicker.setOnClickListener{
                val c = Calendar.getInstance()
                val cal = Calendar.getInstance()
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)
                val dpd = DatePickerDialog (this, DatePickerDialog.OnDateSetListener{
                        view : DatePicker, mYear : Int, mMonth: Int, mDay : Int ->
                    cal.set(Calendar.YEAR, mYear)
                    cal.set(Calendar.MONTH, mMonth)
                    cal.set(Calendar.DAY_OF_MONTH, mDay)
                    val dateFormat = "dd-MM-yyyy"
                    val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
                    binding.editTextTanggalPeriksa.editText?.setText(simpleDateFormat.format(cal.time))

                },year,month,day)

                dpd.show()
            }
    }

    private fun getKelinci(userId: String?) {
        spinner = binding.spinnerNamaKelinci
        db = FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("kelinci")
        db.addValueEventListener( object : ValueEventListener{
            var nameList = mutableListOf<String>()
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (dataSnapshot in snapshot.children){
                        val tipe = dataSnapshot.child("tipe").value
                        if (tipe == "aktif"){
                            val item = dataSnapshot.child("namaKelinci").value
                            if (item!=null){
                                nameList.add(item.toString())
                            }
                        }
                    }
                    val adapter = ArrayAdapter(
                        this@InputPeriksaKesehatanActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        nameList
                    )
                    spinner.adapter = adapter

                } else{
                    nameList.add("Tidak ada data kelinci")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }
}
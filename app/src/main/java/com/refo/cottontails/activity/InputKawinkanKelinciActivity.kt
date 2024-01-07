package com.refo.cottontails.activity

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.core.view.isEmpty
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.refo.cottontails.ShowSnackBar
import com.refo.cottontails.data.DataKawin
import com.refo.cottontails.data.DataKelinci
import com.refo.cottontails.databinding.ActivityInputKawinkanKelinciBinding
import java.text.SimpleDateFormat
import java.util.*

class InputKawinkanKelinciActivity : AppCompatActivity() {
    private lateinit var binding : ActivityInputKawinkanKelinciBinding

    private lateinit var db : DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputKawinkanKelinciBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        binding.imageViewBackButton.setOnClickListener{
            onBackPressed()
        }
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        val rootView = binding.rootView
        getKelinciBetina(userId)
        getDate()
        getKelinciJantan(userId)
        binding.checkboxKawinKelinciSendiri.setOnCheckedChangeListener{ buttonView, isChecked ->
            if (isChecked){
                binding.spinnerKelinciJantan.visibility = View.VISIBLE
                binding.textViewKelinciJantan.visibility = View.VISIBLE
            }
            else{
                binding.spinnerKelinciJantan.visibility = View.GONE
                binding.textViewKelinciJantan.visibility = View.GONE
            }
        }
        binding.buttonSumbitForm.setOnClickListener{
            val cekTanggal = checkDate()
            when{
                !cekTanggal -> {
                    rootView.ShowSnackBar("Harap masukan tanggal dengan benar!",100,rootView)
                }
                binding.spinnerNamaKelinci.isEmpty() ->{
                    rootView.ShowSnackBar("Nama Kelinci masih kosong, harap isi dahulu",100,rootView)
                }
                binding.editTextTanggalKawin.editText?.text.isNullOrEmpty() ->{
                    rootView.ShowSnackBar("Tanggal kawin masih kosong, harap isi dahulu",100,rootView)
                }
                else ->{
                    tambahKawin(userId)
                }
            }
        }

    }

    private fun checkDate() : Boolean{
        val cal = Calendar.getInstance()
        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(cal.time)
        val inputDateStr = binding.editTextTanggalKawin.editText?.text.toString()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        val inputDate = dateFormat.parse(inputDateStr)
        val tanggalSaatIni = dateFormat.parse(currentDate)

        // Bandingkan tanggal yang diinputkan dengan tanggal saat ini
        return !(inputDate != null && inputDate.after(tanggalSaatIni))
    }

    private fun getKelinciJantan(userId: String?) {
        db = FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("kelinci")
        db.addValueEventListener(object : ValueEventListener{
            var nameList = mutableListOf<String>()
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    nameList.clear() // Bersihkan list sebelum mengisi ulang
                    for (dataSnapshot in snapshot.children){
                        val item = dataSnapshot.getValue(DataKelinci::class.java)
                        if (isUsiaKawin(item!!.tanggalLahir)){
                            val nama = item.namaKelinci.toString()
                            val tipe = item.tipe
                            val jenisKelaminKelinci = dataSnapshot.child("jenisKelaminKelinci").value
                            if (jenisKelaminKelinci != "Betina"){
                                if (tipe!="non_aktif"){
                                    nameList.add(nama)
                                }
                            }
                        }
                    }
                    val adapterJantan = ArrayAdapter(
                        this@InputKawinkanKelinciActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        nameList
                    )
                    binding.spinnerKelinciJantan.adapter = adapterJantan
                } else {
                    nameList.clear()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun tambahKawin(userId: String?) {
            val namaKelinci = binding.spinnerNamaKelinci.selectedItem.toString()
            findKelinciBetina(namaKelinci,userId)
    }

    private fun findKelinciBetina(namaKelinci: String, userId: String?) {
        FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("kelinci")
            .addListenerForSingleValueEvent(
                object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()){
                            for (dataSnapshot in snapshot.children){
                                val kelinciBetina = dataSnapshot.child("namaKelinci").value
                                if (kelinciBetina == namaKelinci){
                                    db = FirebaseDatabase.getInstance().reference
                                    val tanggal = binding.editTextTanggalKawin.editText?.text.toString()
                                    val idKawin = db.push().key!!
                                    val kelinciJantan = binding.spinnerKelinciJantan.selectedItem?.toString()
                                    val dataKawin = DataKawin(idKawin,namaKelinci,tanggal,kelinciJantan)
                                    FirebaseDatabase.getInstance().getReference("users").child(userId).child("kawin").child(idKawin)
                                        .setValue(dataKawin).addOnCompleteListener{
                                                kawin ->
                                            if (kawin.isSuccessful){
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

                }
            )
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
                binding.editTextTanggalKawin.editText?.setText(simpleDateFormat.format(cal.time))

            },year,month,day)

            dpd.show()
        }
    }

    private fun getKelinciBetina(userId: String?) {
        db = FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("kelinci")
        db.addValueEventListener(object : ValueEventListener {
            var nameList = mutableListOf<String>()
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    nameList.clear()
                    for (dataSnapshot in snapshot.children){
                        val item = dataSnapshot.getValue(DataKelinci::class.java)
                        if (isUsiaKawin(item!!.tanggalLahir)){
                                val nama = item.namaKelinci.toString()
                                val tipe = item.tipe
                                val jenisKelaminKelinci = dataSnapshot.child("jenisKelaminKelinci").value
                                if (jenisKelaminKelinci != "Jantan"){
                                    if (tipe!="non_aktif"){
                                        nameList.add(nama)
                                    }
                                }
                        }

                    }
                    val adapterBetina = ArrayAdapter(
                        this@InputKawinkanKelinciActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        nameList
                    )
                    binding.spinnerNamaKelinci.adapter = adapterBetina
                } else {
                    nameList.clear()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun isUsiaKawin(tanggalLahir: String?): Boolean {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val tanggalLahirKelinci: Date = dateFormat.parse(tanggalLahir)!!
        val calendar = Calendar.getInstance()
        val today = calendar.time
        val daysPassed =
            ((today.time - tanggalLahirKelinci.time) / (1000 * 60 * 60 * 24)).toInt()
        return daysPassed>=89

    }





}
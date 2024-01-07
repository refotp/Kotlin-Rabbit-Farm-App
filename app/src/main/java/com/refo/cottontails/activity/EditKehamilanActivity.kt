package com.refo.cottontails.activity

import android.R
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.view.View
import android.widget.ArrayAdapter
import android.widget.DatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.refo.cottontails.ShowSnackBar
import com.refo.cottontails.data.DataKawin
import com.refo.cottontails.data.DataKelinci
import com.refo.cottontails.databinding.ActivityEditKehamilanBinding
import java.text.SimpleDateFormat
import java.util.*

class EditKehamilanActivity : AppCompatActivity() {
    private lateinit var binding : ActivityEditKehamilanBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditKehamilanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val rootView = binding.rootView
        supportActionBar?.hide()
        val bundle = intent.extras
        val id = bundle!!.getString("Id")
        val kelinciBetina = bundle.getString("Nama")
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        getDate()
        buttonAction(id, kelinciBetina,rootView,userId)
        setForm(id,userId)



    }

    private fun EditKehamilanActivity.buttonAction(
        id: String?,
        kelinciBetina: String?,
        rootView: View,
        userId: String?
    ) {
        binding.imageViewBackButton.setOnClickListener {
            onBackPressed()
        }
        binding.checkboxKawinKelinciSendiri.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.textViewKelinciJantan.visibility = View.VISIBLE
                binding.spinnerNamaKelinciJantan.visibility = View.VISIBLE
                setSpinner(userId)
            } else {
                binding.textViewKelinciJantan.visibility = View.GONE
                binding.spinnerNamaKelinciJantan.visibility = View.GONE
            }

        }
        binding.buttonSumbitForm.setOnClickListener {
            val cekTanggal = checkDate()
            if (!cekTanggal){
                rootView.ShowSnackBar("Harap masukan tanggal dengan benar!",100,rootView)
            }
            else{
                val dialog2 = Dialog(this)
                dialog2.setContentView(com.refo.cottontails.R.layout.progress_layout)
                if (dialog2.window != null) {
                    dialog2.window!!.setBackgroundDrawable(ColorDrawable(0))
                }
                dialog2.show()
                updateData(id, kelinciBetina,userId)
            }

        }
    }

    private fun checkDate(): Boolean {
        val cal = Calendar.getInstance()
        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(cal.time)
        val inputDateStr = binding.editTextTanggalKawin.editText?.text.toString()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        val inputDate = dateFormat.parse(inputDateStr)
        val tanggalSaatIni = dateFormat.parse(currentDate)

        // Bandingkan tanggal yang diinputkan dengan tanggal saat ini
        return !(inputDate != null && inputDate.after(tanggalSaatIni))
    }

    private fun updateData(id: String?, kelinciBetina: String?, userId: String?) {
        val kawinRef = FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("kawin")

        kawinRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val dataKawin = dataSnapshot.getValue(DataKawin::class.java)
                        if (dataKawin != null && dataKawin.kelinciBetina == kelinciBetina) {
                            val kelinciJantan = binding.spinnerNamaKelinciJantan.selectedItem?.toString()
                            val tanggalKawin = binding.editTextTanggalKawin.editText?.text.toString()
                            val keterangan = binding.editTextKeterangan.editText?.text.toString()

                            // Buat peta untuk menyimpan perubahan yang akan di-update
                            val updatedData = hashMapOf<String, Any?>(
                                "tanggal" to tanggalKawin,
                                "keterangan" to keterangan,
                                "kelinciJantan" to kelinciJantan
                            )

                            // Update data kawin pada Firebase Database
                            kawinRef.child(id!!).updateChildren(updatedData)
                                .addOnSuccessListener {
                                    finish()


                                }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event, if needed
            }
        })
    }

    private fun getDate() {
        binding.buttonTanggalPicker.setOnClickListener {
            val c = Calendar.getInstance()
            val cal = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            val dpd = DatePickerDialog(this,
                DatePickerDialog.OnDateSetListener { view: DatePicker, mYear: Int, mMonth: Int, mDay: Int ->
                    cal.set(Calendar.YEAR, mYear)
                    cal.set(Calendar.MONTH, mMonth)
                    cal.set(Calendar.DAY_OF_MONTH, mDay)
                    val dateFormat = "dd-MM-yyyy"
                    val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
                    binding.editTextTanggalKawin.editText?.setText(simpleDateFormat.format(cal.time))

                },
                year,
                month,
                day)

            dpd.show()
        }
    }

    private fun setForm(id: String?, userId: String?) {
        FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("kawin").child(id!!)
            .addListenerForSingleValueEvent( object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val item = snapshot.getValue(DataKawin::class.java)
                    val tanggalKawin = item?.tanggal
                    val keterangan = item?.keterangan
                    binding.editTextTanggalKawin.editText?.setText(tanggalKawin)
                    binding.editTextKeterangan.editText?.setText(keterangan)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun setSpinner(userId: String?) {
        FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("kelinci")
            .addListenerForSingleValueEvent( object : ValueEventListener{
                val kelinciList = mutableListOf<String>()
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        for (dataSnapshot in snapshot.children){
                            val item = dataSnapshot.getValue(DataKelinci::class.java)
                            val tipe = dataSnapshot.child("tipe").value
                            if (item != null && item.jenisKelaminKelinci != "Betina"){
                                if (tipe!="non_aktif"){
                                    val namaKelinci = item.namaKelinci
                                    kelinciList.add(namaKelinci!!)
                                    val adapterJantan = ArrayAdapter(
                                        this@EditKehamilanActivity,
                                        R.layout.simple_spinner_dropdown_item,
                                        kelinciList
                                    )
                                    binding.spinnerNamaKelinciJantan.adapter = adapterJantan
                                }

                            }
                        }
                    }
                    else{
                        kelinciList.clear()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }
}
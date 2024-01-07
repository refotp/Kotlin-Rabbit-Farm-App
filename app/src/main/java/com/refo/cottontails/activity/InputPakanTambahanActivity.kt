package com.refo.cottontails.activity

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.DatePicker
import android.widget.ScrollView
import androidx.core.view.isEmpty
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.refo.cottontails.ShowSnackBar
import com.refo.cottontails.data.DataKelinci
import com.refo.cottontails.data.DataPakanTambahan
import com.refo.cottontails.data.DataPeriksa
import com.refo.cottontails.databinding.ActivityInputPakanTambahanBinding
import java.text.SimpleDateFormat
import java.util.*

class InputPakanTambahanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputPakanTambahanBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputPakanTambahanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        val rootView = binding.rootView
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        getDate()
        binding.imageViewBackButton.setOnClickListener{
            onBackPressed()
        }
        binding.buttonSumbitForm.setOnClickListener{
            val cekTanggal = checkDate()
            when{
                !cekTanggal -> {
                    rootView.ShowSnackBar("Harap masukan tanggal dengan benar!",100,rootView)
                }
                binding.editTextKeterangan.editText?.text.isNullOrEmpty() ->{
                    rootView.ShowSnackBar("Harap masukan pakan tambahan yang diberikan terlebih dahulu",100,rootView)
                }
                else ->{
                    tambahPakan(userId)
                }
            }
        }
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

    private fun tambahPakan(userId: String?) {
        val tanggal = binding.editTextTanggalPeriksa.editText?.text.toString()
        val pakanTambahan = binding.editTextKeterangan.editText?.text.toString()
        val db = FirebaseDatabase.getInstance().reference
        val key = db.push().key!!
        val data = DataPakanTambahan(tanggal,pakanTambahan)
        FirebaseDatabase.getInstance().getReference("users").child(userId!!)
            .child("pakan").child(key).setValue(data).addOnSuccessListener {
                finish()
            }.addOnFailureListener{
            }
    }
    private fun checkDate(): Boolean {
        return if (binding.editTextTanggalPeriksa.editText?.text!!.isNotEmpty()) {
            val cal = Calendar.getInstance()
            val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(cal.time)
            val inputDateStr = binding.editTextTanggalPeriksa.editText?.text.toString()
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

            val inputDate = dateFormat.parse(inputDateStr)
            val tanggalSaatIni = dateFormat.parse(currentDate)

            // Bandingkan tanggal yang diinputkan dengan tanggal saat ini
            !(inputDate != null && inputDate.after(tanggalSaatIni))
        } else {
            false
        }
    }
}
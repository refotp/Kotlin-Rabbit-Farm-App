package com.refo.cottontails.activity

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.refo.cottontails.R
import com.refo.cottontails.data.DataPengeluaran
import com.refo.cottontails.databinding.ActivityInputPengeluaranBinding
import java.text.DateFormat
import java.util.*

class InputPengeluaranActivity : AppCompatActivity() {
    private lateinit var binding : ActivityInputPengeluaranBinding
    private lateinit var spinner : Spinner
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputPengeluaranBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        spinner = binding.spinnerJenisPengeluaran
        val jenisPengeluaran = arrayOf(
            "Pakan",
            "Perlengkapan",
            "Perawatan",
            "Lainnya"
        )
        val adapter = ArrayAdapter(
            this@InputPengeluaranActivity,
            android.R.layout.simple_spinner_dropdown_item,
            jenisPengeluaran
        )
        spinner.adapter = adapter

        binding.buttonSumbitForm.setOnClickListener{
            tambahPengeluaran(userId)
        }
        binding.imageViewBackButton.setOnClickListener{
            onBackPressed()
        }


    }

    private fun tambahPengeluaran(userId: String?) {

        val dialog2 = Dialog(this)
        dialog2.setContentView(R.layout.progress_layout)
        if (dialog2.window != null){
            dialog2!!.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        dialog2.show()
        val db = FirebaseDatabase.getInstance().reference
        val jenisPengeluaran = spinner.selectedItem.toString()
        val jumlahUang = binding.editTextJumlahUang.editText?.text.toString().toInt()
        val keterangan = binding.editTextKeterangan.editText?.text.toString()
        val idTransaksi = db.push().key!!
        val waktuSaatIni = DateFormat.getDateTimeInstance().format(Calendar.getInstance().time)
        val dataPengeluran = DataPengeluaran(idTransaksi,jenisPengeluaran,jumlahUang,keterangan,waktuSaatIni)

        FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("pengeluaran").child(idTransaksi)
            .setValue(dataPengeluran).addOnCompleteListener{
                pengeluaran ->
                if(pengeluaran.isSuccessful){
                    finish()
                }
            }
    }
}
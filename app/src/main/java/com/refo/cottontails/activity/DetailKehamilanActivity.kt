package com.refo.cottontails.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.refo.cottontails.data.DataKawin
import com.refo.cottontails.databinding.ActivityDetailKehamilanBinding
import java.text.SimpleDateFormat
import java.util.*

class DetailKehamilanActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDetailKehamilanBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailKehamilanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        supportActionBar?.hide()
        val bundle = intent.extras
        val kawinId = bundle!!.getString("Id")
        val fotoKelinci = bundle.getString("Foto")
        val kelinciBetina = bundle.getString("Nama")
        val imageViewKelinci = binding.imageViewDescKelinci
        Glide.with(this)
            .load(fotoKelinci)
            .into(imageViewKelinci)
        binding.imageViewBackButton.setOnClickListener{
            onBackPressed()
        }
        binding.buttonEditData.setOnClickListener{
            val intent = Intent(this,EditKehamilanActivity::class.java)
            intent.putExtra("Id",kawinId)
            intent.putExtra("Nama",kelinciBetina)
            startActivity(intent)
        }
        binding.buttonHapusData.setOnClickListener{
            deleteDataKawin(kawinId,userId)
        }
        FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("kawin").child(kawinId!!)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val item = snapshot.getValue(DataKawin::class.java)
                        val tanggalKawin = item?.tanggal
                        val tanggalKelinciKawin = formatTanggalKawin(tanggalKawin)
                        calculateTanggalMelahirkan(tanggalKawin)
                        val kelinciHamil = item?.kelinciBetina
                        val kelinciJantan = item?.kelinciJantan
                        val keterangan = item?.keterangan
                        binding.textViewTanggalKawin.text = tanggalKelinciKawin
                        binding.textViewNamaKelinciHamil.text = kelinciHamil
                        binding.textViewKelinciJantan.text = kelinciJantan
                        binding.textViewKeteranganHamil.text = keterangan


                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

    }

    private fun deleteDataKawin(kawinId: String?, userId: String?) {
        FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("kawin").child(kawinId!!)
            .removeValue().addOnSuccessListener {
                finish()
            }
    }

    private fun formatTanggalKawin(tanggalKawin: String?): String {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())

        try {
            val date = inputFormat.parse(tanggalKawin)
            if (date != null) {
                return outputFormat.format(date)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return tanggalKawin!!
    }

    private fun calculateTanggalMelahirkan(tanggalKawin: String?){
        val currentDate = Calendar.getInstance().time
        if (tanggalKawin!!.isNotBlank()) { // Memastikan tanggal tidak kosong
            val sdfInput = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val tanggalKawinDate = sdfInput.parse(tanggalKawin)
            if (tanggalKawinDate != null) {
                val cal = Calendar.getInstance()
                cal.time = tanggalKawinDate
                cal.add(Calendar.DAY_OF_MONTH, 30)
                if (cal.time.after(currentDate)) {
                    // HPL masih setelah tanggal saat ini, tampilkan data kelinci
                    val sdfOutput = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
                    val estimasiTanggalKelahiranStr = sdfOutput.format(cal.time)
                    binding.textViewEstimasiMelahirkan.text = estimasiTanggalKelahiranStr

                } else if (cal.time.before(currentDate)) {
                    val sdfOutput = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
                    val estimasiTanggalKelahiranStr = sdfOutput.format(cal.time)
                    binding.textViewEstimasiMelahirkan.text = estimasiTanggalKelahiranStr
                    binding.textViewTitleEstimasiMelahirkan.text = "Melahirkan Pada"
                    // HPL sudah berlalu, jangan tampilkan data kelinci ini
                }

            }
        }
    }
}

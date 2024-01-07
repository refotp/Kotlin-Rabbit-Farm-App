package com.refo.cottontails.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.refo.cottontails.activity.*
import com.refo.cottontails.adapter.KawinRecyclerViewAdapter
import com.refo.cottontails.adapter.PakanTambahanRecyclerViewAdapter
import com.refo.cottontails.adapter.PeriksaKesehatanRecyclerViewAdapter
import com.refo.cottontails.data.*
import com.refo.cottontails.databinding.FragmentPerawatanBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PerawatanFragment : Fragment() {
    private var dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    private lateinit var binding: FragmentPerawatanBinding
    private var data = ArrayList<DataKawin>()
    private lateinit var recyclerView : RecyclerView
    private var closestEstimation: Pair<String?, String?>? = null
    val referenceKelinci = FirebaseDatabase.getInstance().getReference("users")
    private lateinit var timer: CountDownTimer
    private lateinit var sharedPreferences: SharedPreferences
    private val TIMER_PREFS = "timer_prefs"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPerawatanBinding.inflate(layoutInflater)
        data.clear()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        setKelinciHamilRecyclerView(userId)
        setPeriksaKesehatanRecyclerView(userId)
        setPakanTambahanRecyclerView(userId)
        getPembersihanKandang(userId)
        binding.textViewLihatKelinciHamil.setOnClickListener{
            val intent = Intent(context,KelinciHamilActivity::class.java)
            startActivity(intent)
        }
        binding.textViewLihatPeriksaKesehatan.setOnClickListener{
            val intent = Intent(context,PeriksaKesehatanActivity::class.java)
            startActivity(intent)

        }
        binding.layoutCardViewBersihkanKandang.setOnClickListener{

            val dialogBuilder = AlertDialog.Builder(context)
            dialogBuilder.setMessage("Apakah kamu baru saja membersihkan kandang kelinci ?")
                .setCancelable(false)
                .setPositiveButton("Iya", DialogInterface.OnClickListener {
                        dialog, id ->
                    tambahBersihkanKandang(userId)
                })
                .setNegativeButton("Tidak", DialogInterface.OnClickListener {
                        dialog, id -> dialog.cancel()
                })
            val alert = dialogBuilder.create()
            alert.setTitle("Peringatan!")
            alert.show()
        }
        binding.cardViewBeriPakanTambahan.setOnClickListener{
            val intent = Intent(context,InputPakanTambahanActivity::class.java)
            startActivity(intent)
        }
        binding.textViewLihatPakanTambahan.setOnClickListener{
            val intent = Intent(context,PakanTambahanActivity::class.java)
            startActivity(intent)
        }
        sharedPreferences = requireContext().getSharedPreferences(TIMER_PREFS, Context.MODE_PRIVATE)
        return binding.root
    }

    private fun setPakanTambahanRecyclerView(userId: String?) {
        val referencePakanTambahan = FirebaseDatabase.getInstance().getReference("users").child(userId!!)
            .child("pakan")
        val pakanTambahanList = mutableListOf<DataPakanTambahan>()
        val adapter = PakanTambahanRecyclerViewAdapter(pakanTambahanList)
        recyclerView = binding.recyclerViewPakanTambahan
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        referencePakanTambahan.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (dataSnapshot in snapshot.children){
                        val data = dataSnapshot.getValue(DataPakanTambahan::class.java)
                        pakanTambahanList.add(data!!)
                        pakanTambahanList.sortByDescending {
                            val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale("id","ID"))
                            dateFormatter.parse(it.tanggalPemberian)
                        }
                        val limitedList = if (pakanTambahanList.size > 5) {
                            pakanTambahanList.takeLast(5)
                        } else {
                            pakanTambahanList
                        }
                        adapter.setData(limitedList)

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


    }

    private fun getPembersihanKandang(userId: String?) {
        val dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("cleaning")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (childSnapshot in dataSnapshot.children) {
                        if (childSnapshot.hasChild("waktuBersih")) {
                            val tanggal = childSnapshot.child("waktuBersih").value.toString()
                            hitungEstimasiPembersihan(tanggal)
                            return
                        }
                    }
                } else {

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

                println("Database error: ${databaseError.message}")
            }
        })
    }

    private fun tambahBersihkanKandang(userId: String?) {
        val currentDate = Date()
        val dateFormat = "dd-MM-yyyy"
        val tanggalFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
        val tanggal = tanggalFormat.format(currentDate)

        val cleaningRef = FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("cleaning")

        cleaningRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (childSnapshot in dataSnapshot.children) {
                        if (childSnapshot.hasChild("waktuBersih")) {
                            val key = childSnapshot.key!!
                            cleaningRef.child(key).child("waktuBersih").setValue(tanggal).addOnSuccessListener {

                                hitungEstimasiPembersihan(tanggal)
                            }
                            return
                        }
                    }
                } else {
                    val key = cleaningRef.push().key!!
                    val data = HashMap<String, Any>()
                    data["waktuBersih"] = tanggal
                    cleaningRef.child(key).setValue(data).addOnSuccessListener {
                        hitungEstimasiPembersihan(tanggal)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors here
                println("Database error: ${databaseError.message}")
            }
        })
    }

    private fun hitungEstimasiPembersihan(tanggal: String) {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val tanggalTerakhir: Date = dateFormat.parse(tanggal)!!
        val calendar = Calendar.getInstance()
        val today = calendar.time
        calendar.time = tanggalTerakhir
        calendar.add(Calendar.DAY_OF_YEAR, 14)
        if (today.before(calendar.time)) {
            val sdfOutput = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            val formattedDate = sdfOutput.format(calendar.time)
            binding.textViewEstimasiBerishkanKandang.text = "Bersih - bersih pada"
            binding.textViewHitungMundurHari.text = formattedDate
        } else if (today == calendar.time) {
            // Current date is equal to the cleaning date + 14 days
            binding.textViewEstimasiBerishkanKandang.text = ""
            binding.textViewHitungMundurHari.text = "Saatnya membersihkan kandang!"
        } else {
            val sdfOutput = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            val formattedDate = sdfOutput.format(tanggalTerakhir)
            binding.textViewHitungMundurHari.text = "Terkahir dibersihkan pada : $formattedDate"
            binding.textViewEstimasiBerishkanKandang.text = "Ayo bersih - bersih kandang"
        }
    }


    private fun setPeriksaKesehatanRecyclerView(userId: String?) {
        val referencePeriksa =
            FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("periksa")
        val kelinciPeriksaList = mutableListOf<DataKelinciPeriksa>()
        recyclerView = binding.recyclerViewPeriksaKesehatan
        val adapter = PeriksaKesehatanRecyclerViewAdapter(kelinciPeriksaList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        referencePeriksa.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val namaKelinci = snapshot.child("namaKelinci").getValue(String::class.java) ?: ""
                val tanggalPeriksa = snapshot.child("tanggal").getValue(String::class.java) ?: ""
                val keterangan = snapshot.child("keterangan").getValue(String::class.java) ?: ""

                referenceKelinci.child(userId).child("kelinci").orderByChild("namaKelinci").equalTo(namaKelinci)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(innerSnapshot: DataSnapshot) {
                            if (innerSnapshot.exists()) {
                                for (dataSnapshot in innerSnapshot.children) {
                                    val fotoKelinci = dataSnapshot.child("fotoKelinci")
                                        .getValue(String::class.java) ?: ""
                                    val periksaKelinci = DataKelinciPeriksa(
                                        namaKelinci,
                                        fotoKelinci,
                                        tanggalPeriksa,
                                        keterangan
                                    )
                                    kelinciPeriksaList.add(periksaKelinci)
                                }
                                kelinciPeriksaList.sortByDescending {
                                    val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                    dateFormatter.parse(it.tanggalPeriksa)
                                }
                                val limitedList = if (kelinciPeriksaList.size > 5) {
                                    kelinciPeriksaList.takeLast(5)
                                } else {
                                    kelinciPeriksaList
                                }
                                adapter.setData(limitedList)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle pembatalan jika diperlukan
                        }
                    })
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle perubahan data anak jika diperlukan
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Handle penghapusan data anak jika diperlukan
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle pergerakan data anak jika diperlukan
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle pembatalan jika diperlukan
            }
        })
    }

    private fun setKelinciHamilRecyclerView(userId: String?) {
        val referenceKawin = FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("kawin")
        val kelinciKawinList = mutableListOf<DataKelinciKawin>()
        recyclerView = binding.recyclerViewCardKawin
        val adapter = KawinRecyclerViewAdapter(kelinciKawinList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        referenceKawin.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                kelinciKawinList.clear()
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val id =
                            dataSnapshot.child("id").getValue(String::class.java)
                        val namaKelinci =
                            dataSnapshot.child("kelinciBetina").getValue(String::class.java) ?: ""
                        val tanggalKawin =
                            dataSnapshot.child("tanggal").getValue(String::class.java) ?: ""
                        val kelinciJantan =
                            dataSnapshot.child("kelinciJantan").getValue(String::class.java) ?:""

                        referenceKelinci.child(userId).child("kelinci").orderByChild("namaKelinci").equalTo(namaKelinci)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (dataSnapshot in snapshot.children) {
                                        val fotoKelinci = dataSnapshot.child("fotoKelinci")
                                            .getValue(String::class.java) ?: ""
                                        val kawinKelinci =
                                            DataKelinciKawin(id!!,namaKelinci,kelinciJantan,fotoKelinci,tanggalKawin)
                                        kelinciKawinList.add(kawinKelinci)

                                    }
                                    val currentDate = Calendar.getInstance().time // Mendapatkan tanggal saat ini
                                    val dataTerurut = kelinciKawinList.map { kelinci ->
                                        val sdfInput = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                        val tanggalKawinDate = sdfInput.parse(kelinci.tanggalKawin)

                                        if (tanggalKawinDate != null) {
                                            val cal = Calendar.getInstance()
                                            cal.time = tanggalKawinDate
                                            cal.add(Calendar.DAY_OF_MONTH, 30) // Tambahkan 30 hari untuk menghitung HPL
                                            val perbedaanTanggal =
                                                cal.time.time - currentDate.time // Hitung perbedaan
                                            kelinci.copy(perbedaanTanggal = perbedaanTanggal)
                                        } else {
                                            kelinci.copy(perbedaanTanggal = -1) // Tanggal tidak valid
                                        }
                                    }.sortedBy { it.perbedaanTanggal }

                                    val limiter = 5 // Batasan jumlah data yang ingin ditampilkan
                                    val limiterData = dataTerurut.take(limiter)

                                    // Update adapter dengan data yang sudah difilter
                                    adapter.setData(limiterData)

//                                    filterDataKelinciHamil(kelinciKawinList,adapter)

                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                            })
                    }
                } else {

                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getLatestKelinciMolting()
        periksaKesehatanClicked()
        kawinkanKelinciClicked()
        binding.cardViewMoltingKelinci.setOnClickListener{
            val intent = Intent(context,MoltingKelinciActivity::class.java)
            startActivity(intent)
        }
    }

    private fun kawinkanKelinciClicked() {
        binding.cardViewPeriksaKesehatan.setOnClickListener{
            val intent = Intent(context,InputPeriksaKesehatanActivity::class.java)
            startActivity(intent)
        }
    }

    private fun periksaKesehatanClicked() {
        binding.cardViewKawinkanKelinci.setOnClickListener{
            val intent = Intent(context,InputKawinkanKelinciActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getLatestKelinciMolting() {
        val db = FirebaseDatabase.getInstance()
        val dbRef = db.getReference("kelinci").child("aktif")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    closestEstimation = null // Reset estimasi terdekat
                    for (dataSnapshot in snapshot.children) {
                        if (dataSnapshot.exists()) {
                            val item = dataSnapshot.getValue(DataKelinci::class.java)
                            val tanggalLahir = item?.tanggalLahir
                            val nama = item?.namaKelinci
                            startChecking(tanggalLahir, nama)
                        }
                    }

                    // Set TextView dengan estimasi terdekat (jika ada)
                    val namaKelinci = binding.textNamaKelinci
                    val hitungMundur = binding.textViewHitungMundur
                    if (closestEstimation != null) {
                        namaKelinci.text = closestEstimation?.first
                        hitungMundur.text = closestEstimation?.second
                    } else {
                        namaKelinci.text = "Tidak ada kelinci dalam rentang usia molting"
                        namaKelinci.textSize = 14f
                        hitungMundur.text = ""
                    }
                } else {
                    binding.textNamaKelinci.text = "Belum ada data kelinci"
                    binding.textNamaKelinci.textSize = 18f
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun startChecking(tanggalLahir: String?, nama: String?) {

        val tanggalLahirKelinci: Date = dateFormat.parse(tanggalLahir)!!
        val calendar = Calendar.getInstance()
        val today = calendar.time
        val daysPassed = ((today.time - tanggalLahirKelinci.time) / (1000 * 60 * 60 * 24)).toInt()

        if (daysPassed < 90) {
            // Kelinci berusia kurang dari 90 hari
            // Hitung tanggal estimasi awal molting (90 hari setelah tanggal lahir)
            val calendarEstimasiAwal = Calendar.getInstance()
            calendarEstimasiAwal.time = tanggalLahirKelinci
            calendarEstimasiAwal.add(Calendar.DAY_OF_YEAR, 90)
            val estimasiAwalMolting = calendarEstimasiAwal.time

            // Hitung tanggal estimasi akhir molting (120 hari setelah tanggal lahir)
            val calendarEstimasiAkhir = Calendar.getInstance()
            calendarEstimasiAkhir.time = tanggalLahirKelinci
            calendarEstimasiAkhir.add(Calendar.DAY_OF_YEAR, 120)
            val estimasiAkhirMolting = calendarEstimasiAkhir.time

            // Cek apakah estimasi ini lebih mendekati dari yang sebelumnya
            if (closestEstimation == null || isCloser(estimasiAwalMolting, estimasiAkhirMolting)) {
                closestEstimation = Pair(nama, "Estimasi:\n${dateFormat.format(estimasiAwalMolting)} - ${dateFormat.format(estimasiAkhirMolting)}")
            }
        }
    }

    private fun isCloser(date1: Date, date2: Date): Boolean {
        val calendar = Calendar.getInstance()
        val today = calendar.time
        val diff1 = Math.abs(date1.time - today.time)
        val diff2 = Math.abs(date2.time - today.time)
        return diff1 < diff2
    }

    }
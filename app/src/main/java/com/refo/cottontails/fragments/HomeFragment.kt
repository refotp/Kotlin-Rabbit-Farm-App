package com.refo.cottontails.fragments
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.refo.cottontails.*
import com.refo.cottontails.R
import com.refo.cottontails.activity.LoginActivity
import com.refo.cottontails.adapter.HomeRecyclerViewAdapter
import com.refo.cottontails.data.DataKelinci
import com.refo.cottontails.data.DataPemasukan
import com.refo.cottontails.data.DataPengeluaran
import com.refo.cottontails.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        val recyclerView: RecyclerView = binding.recyclerViewCard
        val data = listOf(
            ItemPerawatan(R.drawable.persilangan, "Kelinci Hamil", R.color.yellow),
            ItemPerawatan(R.drawable.bunny2,"Catatan Kesehatan", R.color.light_red)
        )
        val adapter = HomeRecyclerViewAdapter(data,context!!)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        getJumlahTernak(userId)
        val tanggalSekarang = Date()
        val formatter = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        val tanggalFormat = formatter.format(tanggalSekarang)
        binding.textViewDate.text = tanggalFormat
        getUntungRugi(userId)
        binding.buttonLogout.setOnClickListener{
            val auth = FirebaseAuth.getInstance()
            auth.signOut()
            val intent = Intent(context,LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getUntungRugi(userId: String?) {
        val referencePemasukan = FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("pemasukan")
        val referencePengeluaran = FirebaseDatabase.getInstance().getReference("users").child(userId).child("pengeluaran")
        referencePengeluaran.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dataPengeluaran = mutableListOf<DataPengeluaran>()
                for (dataSnapshot in snapshot.children) {
                    val item = dataSnapshot.getValue(DataPengeluaran::class.java)
                    dataPengeluaran.add(item!!)
                }
                referencePemasukan.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val dataPemasukan = mutableListOf<DataPemasukan>()
                        for (dataSnapshot in snapshot.children) {
                            val item = dataSnapshot.getValue(DataPemasukan::class.java)
                            dataPemasukan.add(item!!)
                        }
                        val uangMasuk = dataPemasukan.sumOf { it.jumlahUang }
                        val uangKeluar = dataPengeluaran.sumOf { it.jumlahUang }
                        hitungUntungRugi(uangMasuk, uangKeluar)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        handleDatabaseError(error)
                    }
                })
            }
            override fun onCancelled(error: DatabaseError) {
                handleDatabaseError(error)
            }
        })
    }
    private fun hitungUntungRugi(uangMasuk: Int, uangKeluar: Int) {
        var hasil = 0
        var statusText = ""
        var imageResource = 0
        var textColor = 0

        if (uangMasuk > uangKeluar) {
            hasil = uangMasuk - uangKeluar
            statusText = "Untung"
            imageResource = R.drawable.icon_growth
            textColor = Color.parseColor("#298604")
        } else if (uangKeluar > uangMasuk) {
            hasil = uangKeluar - uangMasuk
            statusText = "Rugi"
            imageResource = R.drawable.icon_loss
            textColor = Color.parseColor("#F2786D")
        } else {
            statusText = "Balance"
            imageResource = R.drawable.icon_money
        }
        binding.textViewStatusUntungRugi.text = statusText
        binding.textViewUntungRugi.text = "Rp $hasil"
        binding.imageViewUntungRugi.setImageResource(imageResource)
        if (textColor != 0) {
            binding.textViewUntungRugi.setTextColor(textColor)
        }
    }
    private fun handleDatabaseError(error: DatabaseError) {
        // Tambahkan penanganan kesalahan sesuai kebutuhan Anda.
    }

    private fun getJumlahTernak(userId: String?) {
        FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("kelinci").addChildEventListener(
            object : ChildEventListener {
                var counter = 0
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if (snapshot.exists()) {
                        val item = snapshot.getValue(DataKelinci::class.java)
                        val tipe = item?.tipe
                        if (tipe == "aktif"){
                            counter++
                            displayTotalTernak(counter)
                        }

                    }
                }
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                }
                override fun onChildRemoved(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        counter--
                        displayTotalTernak(counter)
                    }
                }
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    // Handle pergerakan data anak jika diperlukan
                }
                override fun onCancelled(error: DatabaseError) {
                    // Handle pembatalan jika diperlukan
                }
            }
        )
    }

    private fun displayTotalTernak(counter: Int) {
        val totalTernak = "$counter ekor"
        binding.textViewJumlahKelinci.text = totalTernak
    }
}

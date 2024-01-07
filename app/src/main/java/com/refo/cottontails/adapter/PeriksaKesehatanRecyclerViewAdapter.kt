package com.refo.cottontails.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.refo.cottontails.R
import com.refo.cottontails.data.DataKelinciKawin
import com.refo.cottontails.data.DataKelinciPeriksa
import java.text.SimpleDateFormat
import java.util.*

class PeriksaKesehatanRecyclerViewAdapter (private var data : List<DataKelinciPeriksa>) :
    RecyclerView.Adapter<PeriksaKesehatanRecyclerViewAdapter.ViewHolder>(){
    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val imageView : ImageView = itemView.findViewById(R.id.image_view_kelinci)
        val textViewNamaKelinci : TextView = itemView.findViewById(R.id.text_view_nama_kelinci)
        val textViewTanggalPeriksa : TextView = itemView.findViewById(R.id.text_view_tanggal_periksa)
        val textViewKeterangan : TextView = itemView.findViewById(R.id.text_view_keterangan)

        fun bind(kelinci: DataKelinciPeriksa) {
            // Tampilkan data pada tampilan
            textViewNamaKelinci.text = kelinci.namaKelinci

            // Menggunakan Glide untuk memuat gambar dari URL ke ImageView
            Glide.with(itemView.context)
                .load(kelinci.fotoKelinci)
                .into(imageView)
        }

    }
    fun setData(newData: List<DataKelinciPeriksa>) {
        data = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_item_periksa_kesehatan, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        val nama = item.namaKelinci
        val tanggal = item.tanggalPeriksa
        val keterangan = item.keterangan
        val sdfInput = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val tanggalPeriksa = sdfInput.parse(tanggal)
        val cal = Calendar.getInstance()
        cal.time = tanggalPeriksa
        val sdfOutput = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
        val tanggalPeriksaStr = sdfOutput.format(cal.time)
        holder.textViewNamaKelinci.text = nama
        holder.textViewTanggalPeriksa.text = tanggalPeriksaStr
        holder.textViewKeterangan.text = keterangan
        holder.bind(item)

    }

    override fun getItemCount() = data.size
}
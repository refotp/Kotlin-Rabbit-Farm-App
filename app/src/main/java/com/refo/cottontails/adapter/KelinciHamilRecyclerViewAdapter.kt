package com.refo.cottontails.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.refo.cottontails.R
import com.refo.cottontails.activity.DetailKehamilanActivity
import com.refo.cottontails.data.DataKelinciKawin
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class KelinciHamilRecyclerViewAdapter (private var data: List<DataKelinciKawin>,
private var context: Context) :
    RecyclerView.Adapter<KelinciHamilRecyclerViewAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.card_view_kawin_hpl)
        val imageView: ImageView = itemView.findViewById(R.id.image_view_kelinci)
        val textViewNamaKelinci: TextView = itemView.findViewById(R.id.text_view_nama_kelinci)
        val textViewHPL: TextView = itemView.findViewById(R.id.text_view_hpl)
        val textViewEstimasi : TextView = itemView.findViewById(R.id.text_view_tanggal_kawin)
        val idKawinKelinci : TextView = itemView.findViewById(R.id.text_view_id_kehamilan)

        fun bind(kelinci: DataKelinciKawin) {
            // Tampilkan data pada tampilan
            textViewNamaKelinci.text = kelinci.namaKelinci

            // Menggunakan Glide untuk memuat gambar dari URL ke ImageView
            Glide.with(itemView.context)
                .load(kelinci.fotoKelinci)
                .into(imageView)
        }
    }
    fun setData(newData: List<DataKelinciKawin>) {
        data = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_item_kawin, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        val id = item.id
        val fotoKelinci = item.fotoKelinci
        val kelinciBetina = item.namaKelinci
        val currentDate = Calendar.getInstance().time
        val tanggalKawin = item.tanggalKawin
        if (tanggalKawin!!.isNotBlank()) { // Memastikan tanggal tidak kosong
            val sdfInput = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            try {
                val tanggalKawinDate = sdfInput.parse(tanggalKawin)
                if (tanggalKawinDate != null) {
                    val cal = Calendar.getInstance()
                    cal.time = tanggalKawinDate
                    cal.add(Calendar.DAY_OF_MONTH, 30)
                    if (cal.time.after(currentDate)) {
                        // HPL masih setelah tanggal saat ini, tampilkan data kelinci
                        val sdfOutput = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
                        val estimasiTanggalKelahiranStr = sdfOutput.format(cal.time)
                        holder.textViewEstimasi.text = "Estimasi Melahirkan :"
                        holder.textViewHPL.text = estimasiTanggalKelahiranStr
                        holder.idKawinKelinci.text = id
                    } else if (cal.time.before(currentDate)){
                        val sdfOutput = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
                        val estimasiTanggalKelahiranStr = sdfOutput.format(cal.time)
                        holder.textViewEstimasi.text = "Melahirkan Pada:"
                        holder.textViewHPL.text = estimasiTanggalKelahiranStr
                        holder.idKawinKelinci.text = id
                        // HPL sudah berlalu, jangan tampilkan data kelinci ini
                    }

                }
            } catch (e: ParseException) {
                holder.textViewHPL.text = "Tanggal tidak valid" // Atur teks default jika parsing gagal
            }
        } else {
            holder.textViewHPL.text = "Tanggal tidak tersedia" // Atur teks default jika tanggal kosong
        }
        holder.cardView.setOnClickListener{
            val intent = Intent(context,DetailKehamilanActivity::class.java)
            intent.putExtra("Id", id)
            intent.putExtra("Foto", fotoKelinci)
            intent.putExtra("Nama",kelinciBetina)
            context.startActivity(intent)

        }
        holder.bind(item)



    }




    override fun getItemCount()=data.size

}
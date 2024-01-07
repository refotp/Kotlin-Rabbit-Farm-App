package com.refo.cottontails.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.refo.cottontails.CardItemKeuangan
import com.refo.cottontails.R
import com.refo.cottontails.data.DataPengeluaran

class KeuanganKategoriRecyclerViewAdapater(private val data: MutableList<DataPengeluaran>,
                                           private val totalPengeluaranMap: MutableMap<String, Int>):
    RecyclerView.Adapter<KeuanganKategoriRecyclerViewAdapater.ViewHolder>(){
    private val gambarKartu = arrayOf(
        "gambar1", // Gambar untuk jenis "Pakan"
        "gambar2", // Gambar untuk jenis "Perawatan"
        "gambar3", // Gambar untuk jenis "Perlengkapan"
        "gambar4"  // Gambar untuk jenis "Lainnya"
    )


    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val textViewNamaKartu : TextView = itemView.findViewById(R.id.text_view_nama_kartu)
        val imageViewGambarKartu : ImageView = itemView.findViewById(R.id.image_view_gambar_kartu)
        val textViewJumlah : TextView = itemView.findViewById(R.id.text_view_jumlah_kartu)
        val cardViewJenisPengeluaran : CardView = itemView.findViewById(R.id.card_view_keuangan)
        val keterangan : TextView = itemView.findViewById(R.id.text_view_keterangan)
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_item_keuangan,parent,false)
        return ViewHolder(itemView)
    }
    fun updateData(newData: List<DataPengeluaran>, newTotalPengeluaranMap: Map<String, Int>) {
        data.clear()
        data.addAll(newData)
        totalPengeluaranMap.clear()
        totalPengeluaranMap.putAll(newTotalPengeluaranMap)
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        val jenisPengeluaran = item.jenisPengeluaran
        val uang = item.jumlahUang
        // Menampilkan jenis pengeluaran pada CardView
        holder.textViewNamaKartu.text = jenisPengeluaran
        holder.keterangan.text = item.keterangan
        // Menampilkan total pengeluaran pada CardView
        val totalPengeluaran = totalPengeluaranMap[jenisPengeluaran]
        holder.textViewJumlah.text = "Rp $uang"
        val gambarResource = getGambarResource(jenisPengeluaran)
        holder.imageViewGambarKartu.setImageResource(gambarResource)
        val backgroundColor = getCardBackgroundColor(jenisPengeluaran)
        holder.cardViewJenisPengeluaran.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, backgroundColor))

    }

    private fun getCardBackgroundColor(jenisPengeluaran: String): Int {
        return when(jenisPengeluaran){
            "Pakan" -> R.color.yellow
            "Perawatan" -> R.color.light_pink
            "Perlengkapan" -> R.color.blue
            "Lainnya" -> R.color.light_red
            else -> R.color.black
        }
    }


    private fun getGambarResource(jenisPengeluaran: String): Int {
        when (jenisPengeluaran) {
            "Pakan" -> return R.drawable.image_card_view_keuangan_pakan
            "Perawatan" -> return R.drawable.image_card_view_keuangan_perawatan
            "Perlengkapan" -> return R.drawable.image_card_view_keuangan_peralatan
            "Lainnya" -> return R.drawable.image_card_view_keuangan_lain
            else -> return R.drawable.ic_launcher_background // Gambar default jika jenis tidak dikenali
        }
    }


    override fun getItemCount() = data.size

    }


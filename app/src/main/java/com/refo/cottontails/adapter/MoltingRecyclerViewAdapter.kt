package com.refo.cottontails.adapter
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.provider.Settings.Global.getString
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.color
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.refo.cottontails.R
import com.refo.cottontails.data.DataKelinci
import java.text.SimpleDateFormat
import java.util.*

class MoltingRecyclerViewAdapter(private var data: List<DataKelinci>, private val context: Context) :
    RecyclerView.Adapter<MoltingRecyclerViewAdapter.ViewHolder>() {
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view_kelinci)
        val textViewNamaKelinci: TextView = itemView.findViewById(R.id.text_view_nama_kelinci)
        val textViewMoltingKelinci: TextView = itemView.findViewById(R.id.text_view_molting)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_item_molting_kelinci, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        val nama = item.namaKelinci
        val tanggalLahir = item.tanggalLahir

        // Memeriksa apakah kelinci berusia dalam rentang usia molting atau kurang dari 90 hari
        if (isWithinMoltingAge(tanggalLahir)) {
            holder.textViewNamaKelinci.text = nama
            Glide.with(context).load(data[position].fotoKelinci).into(holder.imageView)
            startChecking(holder, tanggalLahir, nama)
        } else {
            // Jika kelinci tidak dalam rentang usia molting atau kurang dari 90 hari, sembunyikan item
            holder.itemView.visibility = View.GONE
            holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
        }
    }

    private fun isWithinMoltingAge(tanggalLahir: String?): Boolean {
        if (tanggalLahir != null) {
            val tanggalLahirKelinci: Date = dateFormat.parse(tanggalLahir)!!
            val calendar = Calendar.getInstance()
            val today = calendar.time
            val daysPassed = ((today.time - tanggalLahirKelinci.time) / (1000 * 60 * 60 * 24)).toInt()

            return daysPassed >= 0 && daysPassed <= 120 // Ubah batasan ke 0-120 hari
        }
        return false // Jika tanggal lahir tidak valid, kembalikan false
    }

    private fun startChecking(holder: ViewHolder, tanggalLahir: String?, nama: String?) {
        val handler = Handler()
        val tanggalLahirKelinci: Date = dateFormat.parse(tanggalLahir)!!
        val calendar = Calendar.getInstance()
        val today = calendar.time
        val daysPassed = ((today.time - tanggalLahirKelinci.time) / (1000 * 60 * 60 * 24)).toInt()

        // Sudah 90 hari atau kelinci berusia lebih dari 90 hari
        if (daysPassed >= 90) {
            // Hitung hari yang tersisa hingga mencapai 120 hari
            val daysRemaining = 120 - daysPassed

            // Format tanggal mundur ke dalam format yang sesuai
            val tanggalMundurFormatted = dateFormat.format(today)

            // Tampilkan hasilnya pada TextView
            val estimasi = daysRemaining
            val text = "Estimasi Molting: "
            val endText = " hari lagi"
            val fullText = text + estimasi + endText
            val estimasiCodeColor = ContextCompat.getColor(context, R.color.green)
            val text2 = SpannableStringBuilder()
                .append(text)
                .color(estimasiCodeColor) { append("$estimasi").append(endText) }

            holder.textViewMoltingKelinci.text = text2
        } else {
            // Belum mencapai 90 hari, lanjutkan hitungan mundur
            val daysRemaining = 90 - daysPassed

            // Format tanggal mundur ke dalam format yang sesuai
            val tanggalMundurFormatted = dateFormat.format(today)

            // Tampilkan hasilnya pada TextView
            holder.textViewMoltingKelinci.text = "Estimasi Molting dalam $daysRemaining hari"
        }

        // Periksa tanggal setiap hari
        handler.postDelayed({
            startChecking(holder, tanggalLahir, nama)
        }, 24 * 60 * 60 * 1000)
    }

    override fun getItemCount() = data.size
}


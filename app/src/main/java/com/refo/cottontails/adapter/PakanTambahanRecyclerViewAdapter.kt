package com.refo.cottontails.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.refo.cottontails.R
import com.refo.cottontails.data.DataKelinciKawin
import com.refo.cottontails.data.DataPakanTambahan
import java.text.SimpleDateFormat
import java.util.*

class PakanTambahanRecyclerViewAdapter(private var dataList : List<DataPakanTambahan>) :
RecyclerView.Adapter<PakanTambahanRecyclerViewAdapter.ViewHolder>(){
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewKeterangan: TextView = itemView.findViewById(R.id.text_view_keterangan_pakan)
        val textViewTanggalPemberian: TextView = itemView.findViewById(R.id.text_view_tanggal_pemberian)

    }
    fun setData(newData: List<DataPakanTambahan>) {
        dataList = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_item_pakan_tambahan, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        val keterangan = item.keterangan
        val tanggal = item.tanggalPemberian
        val sdfInput = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val tanggalPemberian = sdfInput.parse(tanggal)
        val cal = Calendar.getInstance()
        cal.time = tanggalPemberian
        val sdfOutput = SimpleDateFormat("dd MMMM, yyyy", Locale("id","ID"))
        val tanggalPemberianStr = sdfOutput.format(cal.time)
        holder.textViewKeterangan.text = keterangan
        holder.textViewTanggalPemberian.text = tanggalPemberianStr

    }

    override fun getItemCount(): Int = dataList.size
}
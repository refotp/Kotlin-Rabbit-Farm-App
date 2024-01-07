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
import com.refo.cottontails.activity.DetailTernakActivity
import com.refo.cottontails.data.DataKelinci

class TernakRecyclerViewAdapter (private var data: List<DataKelinci>, private val context: Context):
    RecyclerView.Adapter<TernakRecyclerViewAdapter.ViewHolder2>(){
    class ViewHolder2(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView : CardView = itemView.findViewById(R.id.card_view_kelinci)
        val imageView: ImageView = itemView.findViewById(R.id.image_view_kelinci)
        val textViewNamaKelinci: TextView = itemView.findViewById(R.id.text_view_nama_kelinci)
        val textViewJenisKelinci: TextView = itemView.findViewById(R.id.text_view_jenis_kelinci)
        val textViewIdKelinci: TextView = itemView.findViewById(R.id.text_view_kelinci_id)
        val imageStatus : ImageView = itemView.findViewById(R.id.image_view_status_kelinci)
        val textViewStatus : TextView = itemView.findViewById(R.id.text_view_status_kelinci)
        val textViewParentNode : TextView = itemView.findViewById(R.id.text_view_parent_node)
        val imageViewJenisKelamin : ImageView = itemView.findViewById(R.id.image_view_jenis_kelamin)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder2 {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_item_data_ternak, parent, false)
        return ViewHolder2(itemView)
    }
    override fun onBindViewHolder(holder: ViewHolder2, position: Int) {
        Glide.with(context).load(data[position].fotoKelinci).into(holder.imageView)
        val item = data[position]
        holder.textViewNamaKelinci.text = item.namaKelinci
        holder.textViewJenisKelinci.text = item.jenisKelinci
        holder.textViewIdKelinci.text = item.kelId
        holder.textViewParentNode.text = item.tipe
        when(item.status){
            "Hidup" -> {
                holder.imageStatus.setImageResource(R.drawable.icon_hidup)
                holder.textViewStatus.text = "Hidup"
            }
            "Terjual" ->{
                holder.imageStatus.setImageResource(R.drawable.icon_terjual)
                holder.textViewStatus.text ="Terjual"
            }
            "Mati" ->{
                holder.imageStatus.setImageResource(R.drawable.icon_mati2)
                holder.textViewStatus.text = "Mati"
            }
        }
        when(item.jenisKelaminKelinci){
            "Jantan" -> holder.imageViewJenisKelamin.setImageResource(R.drawable.icon_male2)
            "Betina" -> holder.imageViewJenisKelamin.setImageResource(R.drawable.icon_female2)
        }

        holder.cardView.setOnClickListener{
            val intent = Intent(context, DetailTernakActivity::class.java)
//            intent.putExtra("Image", data[holder.adapterPosition].fotoKelinci)
            val nama = intent.putExtra("Nama", data[holder.adapterPosition].namaKelinci)
            val nama2 = nama.toString()
            intent.putExtra("Id",data[holder.adapterPosition].kelId)
            intent.putExtra("Parent", data[holder.adapterPosition].tipe)
            intent.putExtra("Status", data[holder.adapterPosition].status)
            notifyDataSetChanged()
            context.startActivity(intent)
        }
    }


    override fun getItemCount() = data.size

    fun searchData(searchList: List<DataKelinci>){
        data = searchList
        notifyDataSetChanged()
    }

}


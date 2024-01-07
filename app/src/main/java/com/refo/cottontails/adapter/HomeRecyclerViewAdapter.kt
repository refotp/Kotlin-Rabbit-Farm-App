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
import com.refo.cottontails.ItemPerawatan
import com.refo.cottontails.R
import com.refo.cottontails.activity.InputKawinkanKelinciActivity
import com.refo.cottontails.activity.InputPeriksaKesehatanActivity
import com.refo.cottontails.activity.MoltingKelinciActivity

class HomeRecyclerViewAdapter(private val data: List<ItemPerawatan>,
                              private val context: Context
) :
    RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view_card_image)
        val textView: TextView = itemView.findViewById(R.id.text_view_title)
        val cardView : CardView = itemView.findViewById(R.id.card_view_perawatan)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_item_dashboard_pemeliharaan_ternak, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.textView.setBackgroundResource(item.textBackgroundResId)
        holder.imageView.setImageResource(item.imageResource)
        holder.textView.text = item.text
        holder.cardView.setOnClickListener {
            when(position){
                0 -> {
                    val intent = Intent(context,InputKawinkanKelinciActivity::class.java)
                    context.startActivity(intent)

                }
                1 ->{
                    val intent = Intent(context,MoltingKelinciActivity::class.java)
                    context.startActivity(intent)
                }
                2->{
                    val intent = Intent(context,InputPeriksaKesehatanActivity::class.java)
                    context.startActivity(intent)
                }
            }
        }

    }

    override fun getItemCount() = data.size
}
package com.surya.yashoranews.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.surya.yashoranews.DataModels.RadioStation
import com.surya.yashoranews.MainActivity
import com.surya.yashoranews.R

class RadioAdapter(private val stations: List<RadioStation>) :
    RecyclerView.Adapter<RadioAdapter.RadioViewHolder>() {

    class RadioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgRadio: ImageView = view.findViewById(R.id.imgRadioItem)
        val txtName: TextView = view.findViewById(R.id.txtRadioName)
        val txtCategory: TextView = view.findViewById(R.id.txtRadioCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_radio, parent, false)
        return RadioViewHolder(view)
    }

    override fun onBindViewHolder(holder: RadioViewHolder, position: Int) {
        val station = stations[position]

        holder.txtName.text = station.name
        holder.txtCategory.text = station.category.uppercase()

        // 🖼️ Glide for Radio Logo
        Glide.with(holder.itemView.context)
            .load(station.image_url)
            .placeholder(R.drawable.placeholder_radio)
            .error(R.drawable.placeholder_radio)
            .centerCrop()
            .into(holder.imgRadio)

        // 🖱️ Click to Play in Mini Player (YouTube Style)
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            if (context is MainActivity) {
                // 🔥 ये सीधा MainActivity के playRadio फंक्शन को कॉल करेगा
                context.playRadio(station)
            }
        }
    }

    override fun getItemCount() = stations.size
}
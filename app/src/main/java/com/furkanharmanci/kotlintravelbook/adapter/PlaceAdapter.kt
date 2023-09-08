package com.furkanharmanci.kotlintravelbook.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.furkanharmanci.kotlintravelbook.databinding.RecyclerItemBinding
import com.furkanharmanci.kotlintravelbook.model.Place
import com.furkanharmanci.kotlintravelbook.view.MapsActivity

class PlaceAdapter(val placeList : List<Place>) : RecyclerView.Adapter<PlaceAdapter.PlaceHolder>() {

    class PlaceHolder(val binding: RecyclerItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {
        val binding = RecyclerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceHolder(binding)
    }

    override fun getItemCount(): Int {
        return placeList.size
    }

    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        holder.binding.recyclerItem.text = placeList[position].name
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, MapsActivity::class.java)
            intent.putExtra("selectedPlace", placeList[position])
            intent.putExtra("info", "oldPosition")
            it.context.startActivity(intent)
        }
    }
}
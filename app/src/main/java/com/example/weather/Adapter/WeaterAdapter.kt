package com.example.weather.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.databinding.ItemlistBinding
import androidx.recyclerview.widget.ListAdapter
import com.example.weather.R
import com.squareup.picasso.Picasso

class WeaterAdapter(val listener: Listener?) : ListAdapter<Model, WeaterAdapter.Holder>(Comparator()) {
    class Holder(view: View, val listener: Listener?) : RecyclerView.ViewHolder(view) {
        private val binding = ItemlistBinding.bind(view)
       var itemTemp:Model? = null
        init {
            itemView.setOnClickListener{
                itemTemp?.let { it1 -> listener?.onClick(it1) }
            }
        }
        fun bind(item: Model) {
            itemTemp = item
            binding.textItemList.text = item.time
            binding.textWeater.text = item.condition
            binding.centertexttemp.text =
                item.currentTime.ifEmpty { "${item.maxTemp}°c / ${item.minTemp}°c" }
            Picasso.get().load("http:" + item.imageUrl).into(binding.image)
        }
    }

    class Comparator : DiffUtil.ItemCallback<Model>() {
        override fun areItemsTheSame(oldItem: Model, newItem: Model): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Model, newItem: Model): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.itemlist, parent, false)
        return Holder(view,listener)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    interface Listener {
        fun onClick(item: Model)
    }

}
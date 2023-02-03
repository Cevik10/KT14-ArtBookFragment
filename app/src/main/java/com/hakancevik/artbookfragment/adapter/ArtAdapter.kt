package com.hakancevik.artbookfragment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.hakancevik.artbookfragment.databinding.RecyclerRowBinding
import com.hakancevik.artbookfragment.model.Art
import com.hakancevik.artbookfragment.view.HomeFragmentDirections

class ArtAdapter(val artList: List<Art>) : RecyclerView.Adapter<ArtAdapter.ArtHolder>() {

    class ArtHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtHolder(binding)
    }

    override fun getItemCount(): Int {
        return  artList.size
    }

    override fun onBindViewHolder(holder: ArtHolder, position: Int) {
        holder.binding.recyclerViewTextView.text = artList[position].artName

        holder.itemView.setOnClickListener(View.OnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToAddArtFragment(artList[position],"old")
            Navigation.findNavController(it).navigate(action)

        })


    }


}
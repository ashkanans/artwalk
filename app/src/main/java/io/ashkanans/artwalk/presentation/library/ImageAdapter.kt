package io.ashkanans.artwalk.presentation.library

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import io.ashkanans.artwalk.R

class ImageAdapter(
    private var imageUris: Map<String, List<Bitmap>>,
    private val onItemClicked: (String) -> Unit,
    private val context: Context
) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewPager: ViewPager2 = itemView.findViewById(R.id.imageViewPager)
        val caption: TextView = itemView.findViewById(R.id.recyclerCaption)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = imageUris.entries.elementAtOrNull(position)

        if (entry == null || entry.value.isEmpty()) {
            // Handle empty state if needed
            holder.caption.text = ""
            return
        }

        val caption = entry.key
        val uriList = entry.value

        val adapter = ImageSliderAdapter(uriList, onItemClicked, caption)
        holder.viewPager.adapter = adapter

        holder.caption.text = caption
    }

    override fun getItemCount(): Int = imageUris.size
}

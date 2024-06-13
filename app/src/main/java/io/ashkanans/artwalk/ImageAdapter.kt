package io.ashkanans.artwalk

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ImageAdapter(private val imageUris: Map<String, List<Bitmap>>) :
    RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.recyclerImage)
        val caption: TextView = itemView.findViewById(R.id.recyclerCaption)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = imageUris.entries.elementAtOrNull(position)

        // Check if entry is null or empty
        if (entry == null || entry.value.isEmpty()) {
            // Handle case where entry is null or empty (no URIs to display)
            // Clear the image view and caption
            Glide.with(holder.imageView.context).clear(holder.imageView)
            holder.caption.text = ""
            return
        }

        val caption = entry.key
        val uriList = entry.value

        // Load the first image URI from the list
        Glide.with(holder.imageView.context).load(uriList[0]).into(holder.imageView)

        // Set caption
        holder.caption.text = caption
    }

    override fun getItemCount(): Int = imageUris.size
}

package io.ashkanans.artwalk.presentation.library

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.ashkanans.artwalk.R

class ImageAdapter(
    private var imageUris: Map<String, List<Bitmap>>,
    private val onItemClicked: (String) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.recyclerImage)
        val caption: TextView = itemView.findViewById(R.id.recyclerCaption)
    }

    fun getImageUris(): Map<String, List<Bitmap>> {
        return imageUris
    }

    // Setter for imageUris
    fun setImageUris(uris: Map<String, List<Bitmap>>) {
        imageUris = uris
        notifyDataSetChanged() // Notify RecyclerView of data change
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = imageUris.entries.elementAtOrNull(position)

        if (entry == null || entry.value.isEmpty()) {
            Glide.with(holder.imageView.context).clear(holder.imageView)
            holder.caption.text = ""
            return
        }

        val caption = entry.key
        val uriList = entry.value

        Glide.with(holder.imageView.context).load(uriList[0]).into(holder.imageView)
        holder.caption.text = caption

        // Set the click listener on the itemView
        holder.itemView.setOnClickListener {
            onItemClicked(caption)
        }
    }

    override fun getItemCount(): Int = imageUris.size
}

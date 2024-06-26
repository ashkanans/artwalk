package io.ashkanans.artwalk.presentation.library

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import io.ashkanans.artwalk.R

class ImageSliderAdapter(
    private val images: List<Bitmap>,
    private val onItemClicked: (String) -> Unit,
    private val caption: String
) : RecyclerView.Adapter<ImageSliderAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.sliderImage)
        val dotsContainer: LinearLayout = itemView.findViewById(R.id.dotsContainer)
        val openButton: ImageButton = itemView.findViewById(R.id.openButton_lib)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.slider_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image = images[position]
        holder.imageView.setImageBitmap(image)

        holder.openButton.setOnClickListener {
            onItemClicked(caption) // Pass caption to the click listener
        }

        // Update dots indicator
        setupDots(holder.dotsContainer, position, images.size)
    }

    override fun getItemCount(): Int = images.size

    private fun setupDots(dotsContainer: LinearLayout, currentPosition: Int, size: Int) {
        dotsContainer.removeAllViews() // Clear existing dots

        if (size <= 1) return // No need for dots if only one image

        val context = dotsContainer.context
        val dots = arrayOfNulls<ImageView>(size)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(8, 0, 8, 0)

        for (i in dots.indices) {
            dots[i] = ImageView(context)
            dots[i]?.setImageResource(if (i == currentPosition) R.drawable.dot_active else R.drawable.dot_inactive)
            dots[i]?.layoutParams = params
            dotsContainer.addView(dots[i])
        }
    }
}

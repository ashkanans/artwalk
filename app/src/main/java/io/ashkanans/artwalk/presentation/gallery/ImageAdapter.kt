package io.ashkanans.artwalk.presentation.gallery

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.makeramen.roundedimageview.RoundedImageView
import io.ashkanans.artwalk.R
import io.ashkanans.artwalk.presentation.viewmodel.SharedViewModel

class ImageAdapter(
    private val context: Context,
    private val sharedViewModel: SharedViewModel
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    private var images: MutableList<String> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ImageViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        Glide.with(context)
            .load(images[position])
            .into(holder.imageView)

        holder.imageView.setOnTouchListener(object : View.OnTouchListener {
            private var lastTouchDownTime: Long = 0

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event?.action == MotionEvent.ACTION_DOWN) {
                    if (System.currentTimeMillis() - lastTouchDownTime < 300) {
                        removeImage(position)
                        return true
                    }
                    lastTouchDownTime = System.currentTimeMillis()
                }
                return false
            }
        })
    }

    override fun getItemCount() = images.size

    fun updateImages(newImages: List<String>) {
        images.clear()
        images.addAll(newImages)
        notifyDataSetChanged()
    }

    private fun removeImage(position: Int) {
        val removedImage = images.removeAt(position)
        val uri = Uri.parse(removedImage)

        sharedViewModel.getBitmapFromUri(uri)
            ?.let { sharedViewModel.removeBitmapFromAllValues(it) }
        sharedViewModel.removeImageUri(uri)
        sharedViewModel.uriToBitmapMap.remove(uri)

        notifyDataSetChanged()
        Toast.makeText(
            context,
            "Image removed successfully",
            Toast.LENGTH_SHORT
        ).show()
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: RoundedImageView = itemView.findViewById(R.id.roundedImageView)
    }

    fun getImages(): List<String> {
        return images
    }
}

package io.ashkanans.artwalk

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.makeramen.roundedimageview.RoundedImageView


class GalleryFragment : Fragment() {
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var imageAdapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.staggeredImages)
        recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        imageAdapter = ImageAdapter(requireActivity(), sharedViewModel)
        recyclerView.adapter = imageAdapter

        sharedViewModel.imageUris.observe(viewLifecycleOwner, Observer { images ->
            images?.let {
                imageAdapter.updateImages(it.map { uri -> uri.toString() })
            }
        })
    }

    private class ImageAdapter(
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
            sharedViewModel.removeImageUri(Uri.parse(removedImage))
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
    }
}

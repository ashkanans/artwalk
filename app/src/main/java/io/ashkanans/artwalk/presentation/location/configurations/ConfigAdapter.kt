package io.ashkanans.artwalk.presentation.location.configurations

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.viewpager.widget.PagerAdapter
import io.ashkanans.artwalk.R


class ConfigAdapter(
    private val context: Context,
    private val modelArrayList: ArrayList<ConfigModel>
) : PagerAdapter() {
    override fun getCount(): Int {
        return modelArrayList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.card_item, container, false)

        val model = modelArrayList[position]
        val title = model.title
        val description = model.description
        val date = model.date
        val image = model.image

        val bannerIv: ImageView = view.findViewById(R.id.bannerIv)
        val titleTv: TextView = view.findViewById(R.id.titleTv)
        val descriptionTv: TextView = view.findViewById(R.id.descriptionTv)
        val dateTv: TextView = view.findViewById(R.id.dateTv)

        bannerIv.setImageResource(image)
        titleTv.text = title
        descriptionTv.text = description
        dateTv.text = date

        view.setOnClickListener {
            Toast.makeText(context, "$title \n $description \n $date", Toast.LENGTH_SHORT).show()
        }

        container.addView(view, position)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}
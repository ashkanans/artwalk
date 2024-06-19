package io.ashkanans.artwalk.presentation.location.configurations

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.viewpager.widget.PagerAdapter
import kotlinx.android.synthetic.main.adapter_card.view.*

abstract class CardPagerAdapter(private val context: Context) : PagerAdapter(), CardAdapter {

    private val mViews: MutableList<CardView> = mutableListOf()
    private val mData: MutableList<CardItem> = mutableListOf()
    private var mBaseElevation: Float = 0f

    override fun getBaseElevation(): Float {
        return mBaseElevation
    }

    override fun getCardViewAt(position: Int): CardView {
        return mViews[position]
    }

    override fun getCount(): Int {
        return mData.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.adapter_card, container, false)
        container.addView(view)
        bind(mData[position], view)
        val cardView = view.findViewById<CardView>(R.id.cardView)

        if (mBaseElevation == 0f) {
            mBaseElevation = cardView.cardElevation
        }

        cardView.maxCardElevation = mBaseElevation * MAX_ELEVATION_FACTOR
        mViews.add(cardView)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
        mViews.removeAt(position)
    }

    fun addCardItem(item: CardItem) {
        mData.add(item)
        mViews.add(null) // Placeholder for view, actual view added in instantiateItem
    }

    private fun bind(item: CardItem, view: View) {
        view.titleTextView.text = context.getString(item.titleResId)
        view.contentTextView.text = context.getString(item.textResId)
    }

    companion object {
        const val MAX_ELEVATION_FACTOR = 8
    }
}

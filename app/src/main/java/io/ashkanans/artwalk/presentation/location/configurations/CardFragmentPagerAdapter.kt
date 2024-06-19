package io.ashkanans.artwalk.presentation.location.configurations

import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class CardFragmentPagerAdapter(fm: FragmentManager, override val baseElevation: Float) :
    FragmentStatePagerAdapter(fm), CardAdapter {

    private val mFragments = mutableListOf<CardFragment>()

    init {
        for (i in 0 until 5) {
            addCardFragment(CardFragment())
        }
    }

    fun getBaseElevation(): Float {
        return baseElevation
    }

    override fun getCardViewAt(position: Int): CardView {
        return mFragments[position].getCardView()
    }

    override fun getCount(): Int {
        return mFragments.size
    }

    override fun getItem(position: Int): Fragment {
        return mFragments[position]
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as CardFragment
        mFragments[position] = fragment
        return fragment
    }

    fun addCardFragment(fragment: CardFragment) {
        mFragments.add(fragment)
    }
}

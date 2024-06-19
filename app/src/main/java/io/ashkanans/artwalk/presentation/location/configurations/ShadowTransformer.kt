package io.ashkanans.artwalk.presentation.location.configurations

import android.view.View
import androidx.viewpager.widget.ViewPager

class ShadowTransformer(private val mViewPager: ViewPager, private val mAdapter: CardAdapter) :
    ViewPager.OnPageChangeListener, ViewPager.PageTransformer {

    private var mLastOffset: Float = 0f
    private var mScalingEnabled: Boolean = false

    init {
        mViewPager.addOnPageChangeListener(this)
    }

    fun enableScaling(enable: Boolean) {
        mScalingEnabled = enable
    }

    override fun transformPage(page: View, position: Float) {
        // Transformation of pages here if needed
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        val realCurrentPosition: Int
        val nextPosition: Int
        val baseElevation = mAdapter.getBaseElevation()
        val realOffset: Float
        val goingLeft = mLastOffset > positionOffset

        if (goingLeft) {
            realCurrentPosition = position + 1
            nextPosition = position
            realOffset = 1 - positionOffset
        } else {
            nextPosition = position + 1
            realCurrentPosition = position
            realOffset = positionOffset
        }

        if (nextPosition > mAdapter.count - 1 || realCurrentPosition > mAdapter.count - 1) {
            return
        }

        val currentCard = mAdapter.getCardViewAt(realCurrentPosition)
        if (currentCard != null) {
            if (mScalingEnabled) {
                currentCard.scaleX = (1 + 0.1 * (1 - realOffset)).toFloat()
                currentCard.scaleY = (1 + 0.1 * (1 - realOffset)).toFloat()
            }
            currentCard.cardElevation = (baseElevation + baseElevation
                    * (CardAdapter.MAX_ELEVATION_FACTOR - 1) * (1 - realOffset))
        }

        val nextCard = mAdapter.getCardViewAt(nextPosition)
        if (nextCard != null) {
            if (mScalingEnabled) {
                nextCard.scaleX = (1 + 0.1 * realOffset).toFloat()
                nextCard.scaleY = (1 + 0.1 * realOffset).toFloat()
            }
            nextCard.cardElevation = (baseElevation + baseElevation
                    * (CardAdapter.MAX_ELEVATION_FACTOR - 1) * realOffset)
        }

        mLastOffset = positionOffset
    }

    override fun onPageSelected(position: Int) {}

    override fun onPageScrollStateChanged(state: Int) {}
}

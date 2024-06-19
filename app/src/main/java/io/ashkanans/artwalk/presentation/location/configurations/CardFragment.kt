package io.ashkanans.artwalk.presentation.location.configurations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import io.ashkanans.artwalk.R
import kotlinx.android.synthetic.main.fragment_adapter.view.*

class CardFragment : Fragment() {

    private lateinit var mCardView: CardView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_adapter, container, false)
        mCardView = view.cardView
        mCardView.maxCardElevation = mCardView.cardElevation * CardAdapter.MAX_ELEVATION_FACTOR
        return view
    }

    fun getCardView(): CardView {
        return mCardView
    }
}

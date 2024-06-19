package io.ashkanans.artwalk.presentation.location.configurations

import androidx.cardview.widget.CardView

interface CardAdapter {
    val MAX_ELEVATION_FACTOR: Int
        get() = 8

    val baseElevation: Float

    fun getCardViewAt(position: Int): CardView

    fun getCount(): Int
}
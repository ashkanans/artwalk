package io.ashkanans.artwalk

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import io.ashkanans.artwalk.databinding.FragmentLibraryDashboardBinding

class LibraryDashboardFragment : Fragment() {
    private var _binding: FragmentLibraryDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLibraryDashboardBinding.inflate(inflater, container, false)
        binding.googlePlacesCard.background = null
        binding.youtubeCard.background = null
        binding.spotifyCard.background = null
        binding.wikiCard.background = null
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val caption = arguments?.getString("CAPTION")
        binding.landmarkName.text = caption

        // Animate the cards
        animateCard(binding.googlePlacesCard, -binding.googlePlacesCard.width.toFloat(), 0f)
        animateCard(binding.spotifyCard, -binding.spotifyCard.width.toFloat(), 0f)
        animateCard(binding.youtubeCard, binding.youtubeCard.width.toFloat(), 0f)
        animateCard(binding.wikiCard, binding.wikiCard.width.toFloat(), 0f)
    }

    private fun animateCard(view: View, fromX: Float, toX: Float) {
        view.translationX = fromX
        view.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(view, "translationX", fromX, toX).apply {
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

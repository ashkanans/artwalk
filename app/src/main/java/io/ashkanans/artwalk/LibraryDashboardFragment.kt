package io.ashkanans.artwalk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val caption = arguments?.getString("CAPTION")
        binding.landmarkName.text = arguments?.getString("CAPTION")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package io.ashkanans.artwalk.presentation.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import io.ashkanans.artwalk.R
import io.ashkanans.artwalk.domain.model.DataModel
import io.ashkanans.artwalk.presentation.library.dashboard.LibraryDashboardFragment
import io.ashkanans.artwalk.presentation.viewmodel.SharedViewModel

class LibraryFragment : Fragment() {
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageAdapter

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
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = StaggeredGridLayoutManager(1, RecyclerView.VERTICAL)

        DataModel.mapStringToImageUris.observe(viewLifecycleOwner) { uris ->
            adapter = ImageAdapter(uris, { caption ->
                openLibraryDashboardFragment(caption)
            }, requireContext()) // Pass context to adapter
            recyclerView.adapter = adapter
        }
    }

    private fun openLibraryDashboardFragment(caption: String) {
        val fragment = LibraryDashboardFragment().apply {
            arguments = Bundle().apply {
                putString("CAPTION", caption)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}

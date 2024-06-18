package io.ashkanans.artwalk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

class SettingsFragment : Fragment() {
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Navigate to AboutFragment on About button click
        val aboutButton: RelativeLayout = view.findViewById(R.id.layout_about)
        aboutButton.setOnClickListener {
            sharedViewModel.navigateTo(AboutFragment::class.java)
        }

        // Clear Cache button setup
        val clearCacheButton: RelativeLayout = view.findViewById(R.id.layout_clear_cache)
        clearCacheButton.setOnClickListener {
            sharedViewModel.removeAll()
            sharedViewModel.clearUriToBitmapMap(requireContext())
            Toast.makeText(requireContext(), "Cache cleared!", Toast.LENGTH_SHORT).show()
        }

        // Setup theme spinner
        val themeSpinner: Spinner = view.findViewById(R.id.spinner_theme)
        val themes = listOf("System Default", "Light", "Dark")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, themes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        themeSpinner.adapter = adapter

        // Set the spinner to the current theme
        val currentTheme = ThemeUtils.getThemePreference(requireContext())
        themeSpinner.setSelection(currentTheme)

        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                ThemeUtils.saveThemePreference(requireContext(), position)
                ThemeUtils.applyTheme(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        return view
    }
}

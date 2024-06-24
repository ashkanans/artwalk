package io.ashkanans.artwalk

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.ashkanans.artwalk.presentation.gallery.GalleryFragment
import io.ashkanans.artwalk.presentation.imageDetection.ImageDetection
import io.ashkanans.artwalk.presentation.library.LibraryFragment
import io.ashkanans.artwalk.presentation.location.MapsFragment

class BottomNavigationHandler(
    private val bottomNavigationView: BottomNavigationView,
    private val fragmentManager: FragmentManager
) {
    init {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.map -> replaceFragment(MapsFragment())
                R.id.library -> replaceFragment(LibraryFragment())
                R.id.gallery -> replaceFragment(GalleryFragment())
                R.id.image_detection -> replaceFragment(ImageDetection())
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }
}

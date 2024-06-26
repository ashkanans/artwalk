package io.ashkanans.artwalk

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.ashkanans.artwalk.presentation.gallery.GalleryFragment
import io.ashkanans.artwalk.presentation.imageDetection.ImageDetectionFragment
import io.ashkanans.artwalk.presentation.library.LibraryFragment
import io.ashkanans.artwalk.presentation.location.MapsFragment

class BottomNavigationHandler(
    private val bottomNavigationView: BottomNavigationView,
    private val fragmentManager: FragmentManager,
    private val token: String
) {
    init {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.map -> replaceFragment(MapsFragment())
                R.id.library -> {
                    if (checkToken()) {
                        replaceFragment(LibraryFragment())
                    } else {
                        showLoginToast()
                    }
                }

                R.id.gallery -> {
                    if (checkToken()) {
                        replaceFragment(GalleryFragment())
                    } else {
                        showLoginToast()
                    }
                }

                R.id.image_detection -> {
                    if (checkToken()) {
                        replaceFragment(ImageDetectionFragment())
                    } else {
                        showLoginToast()
                    }
                }
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    private fun checkToken(): Boolean {
        // Check if token is not empty and not null
        return token.isNotBlank()
    }

    private fun showLoginToast() {
        Toast.makeText(
            bottomNavigationView.context,
            "Please log in with Google to access this feature.",
            Toast.LENGTH_SHORT
        ).show()
    }
}

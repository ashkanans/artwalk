package io.ashkanans.artwalk

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import com.google.android.material.navigation.NavigationView
import io.ashkanans.artwalk.presentation.about.AboutFragment
import io.ashkanans.artwalk.presentation.home.HomeFragment
import io.ashkanans.artwalk.presentation.login.LoginActivity
import io.ashkanans.artwalk.presentation.settings.SettingsFragment
import io.ashkanans.artwalk.presentation.share.ShareFragment
import io.ashkanans.artwalk.presentation.viewmodel.SharedViewModel

class NavigationDrawerHandler(
    private val activity: Activity,
    private val drawerLayout: DrawerLayout,
    private val toolbar: Toolbar,
    private val sharedViewModel: SharedViewModel,
    private val sharedPreferences: SharedPreferences,
    private val fragmentManager: FragmentManager
) : NavigationView.OnNavigationItemSelectedListener {

    init {
        val toggle = ActionBarDrawerToggle(
            activity, drawerLayout, toolbar,
            R.string.open_nav, R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> sharedViewModel.navigateTo(HomeFragment::class.java)
            R.id.nav_settings -> sharedViewModel.navigateTo(SettingsFragment::class.java)
            R.id.nav_share -> sharedViewModel.navigateTo(ShareFragment::class.java)
            R.id.nav_about -> sharedViewModel.navigateTo(AboutFragment::class.java)
            R.id.nav_logout -> logoutUser()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun logoutUser() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("is_logged_in", false)
        editor.apply()
        activity.startActivity(Intent(activity, LoginActivity::class.java))
        activity.finish()
    }
}

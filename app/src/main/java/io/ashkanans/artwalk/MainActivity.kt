package io.ashkanans.artwalk

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import io.ashkanans.artwalk.domain.model.DataModel
import io.ashkanans.artwalk.presentation.PingWorker
import io.ashkanans.artwalk.presentation.home.HomeFragment
import io.ashkanans.artwalk.presentation.login.LoginActivity
import io.ashkanans.artwalk.presentation.viewmodel.SharedViewModel
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNavigationMenu: Menu
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var fab: FloatingActionButton
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var navigationDrawerHandler: NavigationDrawerHandler
    private lateinit var bottomNavigationHandler: BottomNavigationHandler
    private lateinit var fabHandler: FabHandler
    private lateinit var permissionHandler: PermissionHandler
    private lateinit var imageHandler: ImageHandler
    private lateinit var pingIndicator: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIfLoggedIn()) {
            navigateToLogin()
            return
        }


        applyAppTheme()
        setContentView(R.layout.activity_main)

        setupViewComponents()
        setupHandlers()
        setupObservers()

        pingServer()

        if (savedInstanceState == null) {
            initializeHomeFragment()
        }

        permissionHandler.requestAllPermissions()
    }

    private fun pingServer() {
        pingIndicator = findViewById(R.id.ping_indicator)

        // Apply fade animation to ping indicator
        val fadeAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_animation)
        pingIndicator.startAnimation(fadeAnimation)


        // One-time ping request
        val oneTimePingWorkRequest: WorkRequest = OneTimeWorkRequest.Builder(PingWorker::class.java)
            .build()

        WorkManager.getInstance(this).enqueue(oneTimePingWorkRequest)

        DataModel.pingSuccessful.observe(this, Observer { success ->
            if (success) {
                pingIndicator.setBackgroundResource(R.drawable.circle_green)
                pingIndicator.setOnClickListener {
                    showPingMessage()
                }
            } else {
                pingIndicator.setBackgroundResource(R.drawable.circle_red)
            }

            // After the initial ping, set up periodic ping requests
            setupPeriodicPing()
        })

    }

    private fun setupPeriodicPing() {
        // Periodic ping request every 15 minutes
        val periodicPingWorkRequest: WorkRequest =
            PeriodicWorkRequest.Builder(PingWorker::class.java, 15, TimeUnit.MINUTES)
                .build()

        WorkManager.getInstance(this).enqueue(periodicPingWorkRequest)
    }

    private fun showPingMessage() {
        // Display a Snackbar or Toast message
        val message = "Server ping successful!"
        Snackbar.make(pingIndicator, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun setupViewComponents() {
        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.background = null

        fab = findViewById(R.id.fab)

        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
    }

    private fun setupHandlers() {
        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)
        imageHandler = ImageHandler(this, sharedViewModel)

        navigationDrawerHandler = NavigationDrawerHandler(
            this,
            drawerLayout,
            findViewById(R.id.toolbar),
            sharedViewModel,
            sharedPreferences,
            supportFragmentManager
        )
        bottomNavigationHandler =
            BottomNavigationHandler(
                bottomNavigationView,
                supportFragmentManager,
                sharedViewModel.getToken(this) ?: ""
            )
        fabHandler = FabHandler(this, fab, imageHandler)
        permissionHandler = PermissionHandler(this)

        findViewById<NavigationView>(R.id.nav_view).setNavigationItemSelectedListener(
            navigationDrawerHandler
        )

        sharedViewModel.navigateToFragment.observe(this, Observer { fragmentClass ->
            fragmentClass?.let {
                replaceFragment(it.newInstance())
            }
        })
    }

    private fun setupObservers() {
        sharedViewModel.loadImageUris(this)
        sharedViewModel.loadMapStringToImageUris(this)
        sharedViewModel.loadUriToBitmapMap(this)
    }

    private fun initializeHomeFragment() {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment())
            .commit()
        findViewById<NavigationView>(R.id.nav_view).setCheckedItem(R.id.nav_home)
    }

    private fun applyAppTheme() {
        val theme = ThemeUtils.getThemePreference(this)
        ThemeUtils.applyTheme(theme)
    }

    private fun checkIfLoggedIn(): Boolean {
        return getSharedPreferences("app_prefs", MODE_PRIVATE).getBoolean("is_logged_in", false)
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionHandler.handleRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imageHandler.handleActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onStop() {
        super.onStop()
        sharedViewModel.removeAll(this)
        sharedViewModel.saveImageUris(this)
        sharedViewModel.saveMapStringToImageUris(this)
        sharedViewModel.saveUriToBitmapMap(this)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) // Optionally add to back stack
            .commit()
    }
}

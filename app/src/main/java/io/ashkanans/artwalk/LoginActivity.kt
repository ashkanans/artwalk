package io.ashkanans.artwalk

import LoginAdapter
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class LoginActivity : AppCompatActivity() {
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var google: FloatingActionButton
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.view_pager)
        google = findViewById(R.id.fab_fb)
        val v: Float = 0f

        val adapter = LoginAdapter(this, this, tabLayout.tabCount)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Login"
                1 -> "Signup"
                else -> null
            }
        }.attach()

        google.translationY = 300f
        tabLayout.translationY = 300f

        google.alpha = v
        tabLayout.alpha = v

        google.animate().translationY(0f).alpha(1f).setDuration(1000).setStartDelay(400).start()

//        loginButton = findViewById(R.id.login)
//        loginButton.setOnClickListener {
//            if (validateLogin()) {
//                val intent = Intent(this@LoginActivity, MapsActivity::class.java)
//                startActivity(intent)
//                finish()
//            } else {
//                Toast.makeText(this@LoginActivity, "Invalid login credentials", Toast.LENGTH_SHORT).show()
//            }
//        }
    }

    private fun validateLogin(): Boolean {
        // Implement your login validation logic here
        // Return true if valid, false otherwise
        return true // This is a placeholder
    }
}

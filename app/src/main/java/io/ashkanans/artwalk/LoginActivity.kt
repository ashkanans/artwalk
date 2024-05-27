package io.ashkanans.artwalk

import LoginAdapter
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout

class LoginActivity : AppCompatActivity() {
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var google: FloatingActionButton
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.view_pager)
        google = findViewById(R.id.fab_fb)
        val v: Float = 0f

        val adapter = LoginAdapter(supportFragmentManager, this, 2)
        viewPager.adapter = adapter

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        google.translationY = 300f
        tabLayout.translationY = 300f

        google.alpha = v
        tabLayout.alpha = v

        google.animate().translationY(0f).alpha(1f).setDuration(1000).setStartDelay(400).start()
        tabLayout.animate().translationY(0f).alpha(1f).setDuration(1000).setStartDelay(400).start()

    }

}

package io.ashkanans.artwalk.presentation.login

import android.Manifest
import android.accounts.Account
import android.accounts.AccountManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.common.AccountPicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import io.ashkanans.artwalk.MainActivity
import io.ashkanans.artwalk.R
import io.ashkanans.artwalk.presentation.viewmodel.SharedViewModel
import services.api.google.cloudvision.GetOAuthToken

class LoginActivity : AppCompatActivity() {
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var google: FloatingActionButton
    private lateinit var sharedPreferences: SharedPreferences
    private val REQUEST_PERMISSIONS = 13
    private val REQUEST_ACCOUNT_AUTHORIZATION = 102
    private val REQUEST_CODE_PICK_ACCOUNT = 101
    private var mAccount: Account? = null

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

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

        // Set up Google Sign-In
        google.setOnClickListener {
            requestPermissions(
                arrayOf(
                    Manifest.permission.GET_ACCOUNTS,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                REQUEST_PERMISSIONS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSIONS -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getAuthToken()
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getAuthToken() {
        val SCOPE = "oauth2:https://www.googleapis.com/auth/cloud-platform"
        if (mAccount == null) {
            pickUserAccount()
        } else {
            GetOAuthToken(this, mAccount!!, SCOPE, REQUEST_ACCOUNT_AUTHORIZATION).execute()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

            REQUEST_CODE_PICK_ACCOUNT -> if (resultCode == RESULT_OK) {
                val email = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                val am = AccountManager.get(this)
                val accounts = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)
                for (account in accounts) {
                    if (account.name == email) {
                        mAccount = account
                        break
                    }
                }
                getAuthToken()
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "No Account Selected", Toast.LENGTH_SHORT).show()
            }

            REQUEST_ACCOUNT_AUTHORIZATION -> if (resultCode == RESULT_OK) {
                val extra = data?.extras
                extra?.getString("authtoken")?.let { onTokenReceived(it) }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Authorization Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickUserAccount() {
        val accountTypes = arrayOf(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)
        val intent = AccountPicker.newChooseAccountIntent(
            null,
            null,
            accountTypes,
            false,
            null,
            null,
            null,
            null
        )
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT)
    }

    private fun saveLoginState(isLoggedIn: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("is_logged_in", isLoggedIn)
        editor.apply()
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun handleFailure(e: Exception) {
        Log.e(TAG, "Failed to get credentials", e)
    }

    fun onTokenReceived(token: String) {
        sharedViewModel.saveToken(this, token)
        saveLoginState(true)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        this.finish()
    }
}
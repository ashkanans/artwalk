package io.ashkanans.artwalk

import LoginAdapter
import android.content.ContentValues.TAG
import android.content.Intent
import android.credentials.GetCredentialException
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import java.util.UUID

class LoginActivity : AppCompatActivity() {
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var google: FloatingActionButton
    private lateinit var loginButton: Button

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
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

        // Set up Google Sign-In
        google.setOnClickListener {
            val WEB_CLIENT_ID =
                "904815380413-g5jfhmjbflmv4d24ejnbdo0qqtmrnjbp.apps.googleusercontent.com"
            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(true)
                .setNonce(generateNonce())
                .build()

            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(this@LoginActivity)
            lifecycleScope.launch {
                try {
                    Log.e("request.. ===", request.toString())
                    val result = credentialManager.getCredential(
                        request = request,
                        context = this@LoginActivity,
                    )
                    Log.e("result.. ===", result.toString())
                    handleSignIn(result)
                } catch (e: GetCredentialCancellationException) {
                    Log.e("Google Sign-In", "Sign-In was cancelled by the user", e)
                    handleFailure(e)
                } catch (e: GetCredentialException) {
                    Log.e("Google Sign-In", "An error occurred during sign-in", e)
                    handleFailure(e)
                } catch (e: Exception) {
                    Log.e("Google Sign-In", "An unexpected error occurred", e)
                    handleFailure(e)
                }
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        val credential = result.credential

        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken

                        startActivity(Intent(this, MapsActivity::class.java))
                        finish()
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                } else {
                    Log.e(TAG, "Unexpected type of credential")
                }
            }

            else -> {
                Log.e(TAG, "Unexpected type of credential")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun handleFailure(e: Exception) {
        Log.e(TAG, "Failed to get credentials", e)
    }

    private fun generateNonce(): String {
        return UUID.randomUUID().toString()
    }

//    fun signOut() {
//        CredentialManager.clearCredentialState(this@LoginActivity, WEB_CLIENT_ID)
//    }
}
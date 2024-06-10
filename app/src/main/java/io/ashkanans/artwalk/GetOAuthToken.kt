package io.ashkanans.artwalk

import android.Manifest
import android.accounts.Account

import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import java.io.IOException

class GetOAuthToken(
    private val mContext: Context,
    private val mFragment: SubscriptionFragment,
    private val mAccount: Account,
    private val mScope: String,
    private val mRequestCode: Int
) : AsyncTask<Void, Void, Void>() {

    override fun doInBackground(vararg params: Void?): Void? {
        // Check and request permissions
        if (mFragment.shouldRequestPermissions()) {
            mFragment.requestPermissions(arrayOf(Manifest.permission.GET_ACCOUNTS), mRequestCode)
            return null
        }

        // Execute background task
        try {
            val token = fetchToken()
            token?.let {
                (mFragment).onTokenReceived(it)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun fetchToken(): String? {
        return try {
            var accessToken = GoogleAuthUtil.getToken(mContext, mAccount, mScope)
            GoogleAuthUtil.clearToken(mContext, accessToken)
            accessToken = GoogleAuthUtil.getToken(mContext, mAccount, mScope)
            accessToken
        } catch (userRecoverableException: UserRecoverableAuthException) {
            userRecoverableException.intent?.let {
                mFragment.startActivityForResult(
                    it,
                    mRequestCode
                )
            }
            null
        } catch (fatalException: GoogleAuthException) {
            fatalException.printStackTrace()
            null
        }
    }

    // Helper method to check if permissions need to be requested
    private fun Fragment.shouldRequestPermissions(): Boolean {
        return context?.let {
            ActivityCompat.checkSelfPermission(it, Manifest.permission.GET_ACCOUNTS)
        } != PackageManager.PERMISSION_GRANTED
    }
}

package services.api.google.cloudvision

import android.accounts.Account
import android.app.Activity
import android.os.AsyncTask
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import io.ashkanans.artwalk.presentation.login.LoginActivity
import java.io.IOException

class GetOAuthToken(
    private val mActivity: Activity,
    private val mAccount: Account,
    private val mScope: String,
    private val mRequestCode: Int
) : AsyncTask<Void, Void, Void>() {

    override fun doInBackground(vararg params: Void?): Void? {
        try {
            val token = fetchToken()
            token?.let {
                (mActivity as LoginActivity).onTokenReceived(it)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(IOException::class)
    private fun fetchToken(): String? {
        return try {
            var accessToken = GoogleAuthUtil.getToken(mActivity, mAccount, mScope)
            GoogleAuthUtil.clearToken(mActivity, accessToken)
            accessToken = GoogleAuthUtil.getToken(mActivity, mAccount, mScope)
            accessToken
        } catch (userRecoverableException: UserRecoverableAuthException) {
            mActivity.startActivityForResult(userRecoverableException.intent, mRequestCode)
            null
        } catch (fatalException: GoogleAuthException) {
            fatalException.printStackTrace()
            null
        }
    }
}

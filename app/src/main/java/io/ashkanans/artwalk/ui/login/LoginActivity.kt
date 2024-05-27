package io.ashkanans.artwalk.ui.login

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.ashkanans.artwalk.R
import io.ashkanans.artwalk.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer { loginState ->
            loginState ?: return@Observer

            // Disable login button unless both username and password are valid
            login?.isEnabled = loginState.isDataValid

            loginState.usernameError?.let {
                username?.error = getString(it)
            }
            loginState.passwordError?.let {
                password?.error = getString(it)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer { loginResult ->
            loginResult ?: return@Observer

            loading?.visibility = View.GONE
            loginResult.error?.let {
                showLoginFailed(it)
            }
            loginResult.success?.let {
                updateUiWithUser(it)
            }
            setResult(Activity.RESULT_OK)

            // Complete and destroy login activity once successful
            finish()
        })

        username?.afterTextChanged { text ->
            loginViewModel.loginDataChanged(
                text,
                password?.text.toString()
            )
        }

        password?.apply {
            afterTextChanged { text ->
                loginViewModel.loginDataChanged(
                    username?.text.toString(),
                    text
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(
                        username?.text.toString(),
                        text.toString()
                    )
                    true
                } else {
                    false
                }
            }

            login?.setOnClickListener {
                loading?.visibility = View.VISIBLE
                loginViewModel.login(
                    username?.text.toString(),
                    text.toString()
                )
            }
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}
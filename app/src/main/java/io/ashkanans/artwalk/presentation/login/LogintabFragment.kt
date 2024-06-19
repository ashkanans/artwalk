package io.ashkanans.artwalk.presentation.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import io.ashkanans.artwalk.MainActivity
import io.ashkanans.artwalk.R
import services.api.authentication.signin.SignIn

class LoginTabFragment : Fragment() {
    private lateinit var username: EditText
    private lateinit var pass: EditText
    private lateinit var forgetPass: View
    private lateinit var login: View
    private var loginClickListener: OnLoginClickListener? = null
    private lateinit var showPasswordButton: Button
    private var passwordVisible = false
    private lateinit var sharedPreferences: SharedPreferences
    private val signInService = SignIn("http://46.100.50.100:63938/")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.login_tab_fragment, container, false)

        sharedPreferences =
            requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        showPasswordButton = root.findViewById(R.id.show_password)
        username = root.findViewById(R.id.username)
        pass = root.findViewById(R.id.password)
        forgetPass = root.findViewById(R.id.forget_pass)
        login = root.findViewById(R.id.login)

        val v = 0f

        username.translationX = 800f
        pass.translationX = 800f
        forgetPass.translationX = 800f
        login.translationX = 800f
        showPasswordButton.translationX = 800f

        username.alpha = v
        pass.alpha = v
        forgetPass.alpha = v
        login.alpha = v
        showPasswordButton.alpha = v

        username.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(300).start()
        pass.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(500).start()
        forgetPass.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(500).start()
        login.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(700).start()
        showPasswordButton.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(700)
            .start()

        showPasswordButton.setOnClickListener {
            togglePasswordVisibility()
        }

        pass.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        login.setOnClickListener {
            validateLogin { isValid ->
                if (isValid) {
                    // If valid, update login state and start MainActivity
                    saveLoginState(true)
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish() // Finish LoginActivity
                } else {
                    // If invalid, show toast
                    Toast.makeText(
                        requireContext(),
                        "Invalid login credentials",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

    private fun togglePasswordVisibility() {
        if (passwordVisible) {
            pass.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            showPasswordButton.text = "Show"
        } else {
            pass.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            showPasswordButton.text = "Hide"
        }
        pass.setSelection(pass.text.length) // Move cursor to the end
        passwordVisible = !passwordVisible
    }

    private fun validateLogin(callback: (Boolean) -> Unit) {
        val usernameInput = username.text.toString()
        val passwordInput = pass.text.toString()

        signInService.signIn(usernameInput, passwordInput) { response ->
            if (response != null) {
                Log.d("SignInResponse", "Response: $response")
                val isValid = response.message == "Authentication successful"
                callback(isValid)
            } else {
                Log.d("SignInResponse", "Response is null")
                callback(false)
            }
        }
    }

    private fun saveLoginState(isLoggedIn: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("is_logged_in", isLoggedIn)
        editor.apply()
    }
}

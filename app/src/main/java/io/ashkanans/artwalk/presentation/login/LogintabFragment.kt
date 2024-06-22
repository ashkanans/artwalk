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
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.ashkanans.artwalk.MainActivity
import io.ashkanans.artwalk.R
import services.api.authentication.signin.SignIn

class LoginTabFragment : Fragment() {

    private lateinit var usernameLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var forgetPassword: View
    private lateinit var loginButton: View

    private lateinit var sharedPreferences: SharedPreferences
    private val signInService =
        SignIn("https://artwalk-1-d74f115da834.herokuapp.com/") // Replace with your service URL

    private var passwordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.login_tab_fragment, container, false)

        sharedPreferences =
            requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        usernameLayout = root.findViewById(R.id.username)
        passwordLayout = root.findViewById(R.id.password)
        usernameEditText = root.findViewById(R.id.editTextUsername)
        passwordEditText = root.findViewById(R.id.editTextPass)
        forgetPassword = root.findViewById(R.id.forget_pass)
        loginButton = root.findViewById(R.id.login)

        // Animate views if needed
        animateViews()

        // Set click listeners
        forgetPassword.setOnClickListener {
            // Implement forget password logic here
            Toast.makeText(requireContext(), "Reset password clicked", Toast.LENGTH_SHORT).show()
        }

        loginButton.setOnClickListener {
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
                    ).show()
                }
            }
        }

        // Set click listener for password visibility toggle
        passwordLayout.setStartIconOnClickListener {
            togglePasswordVisibility()
        }

        // Set initial state for password input type
        passwordEditText.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        return root
    }

    private fun animateViews() {
        val v = 0f
        usernameLayout.translationX = 800f
        passwordLayout.translationX = 800f
        forgetPassword.translationX = 800f
        loginButton.translationX = 800f

        usernameLayout.alpha = v
        passwordLayout.alpha = v
        forgetPassword.alpha = v
        loginButton.alpha = v

        usernameLayout.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(300)
            .start()
        passwordLayout.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(500)
            .start()
        forgetPassword.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(500)
            .start()
        loginButton.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(700).start()

    }

    private fun togglePasswordVisibility() {
        if (passwordVisible) {
            // Hide the password
            passwordEditText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordLayout.setStartIconDrawable(R.drawable.baseline_key_off_24)
        } else {
            // Show the password
            passwordEditText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordLayout.setStartIconDrawable(R.drawable.baseline_key_24)
        }
        passwordEditText.text?.let { passwordEditText.setSelection(it.length) } // Move cursor to the end
        passwordVisible = !passwordVisible // Toggle flag
    }

    private fun validateLogin(callback: (Boolean) -> Unit) {
        val usernameInput = usernameEditText.text.toString()
        val passwordInput = passwordEditText.text.toString()

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

package io.ashkanans.artwalk.presentation.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import io.ashkanans.artwalk.R
import services.api.authentication.signup.SignUp

class SignuptabFragment : Fragment() {
    private lateinit var username: EditText
    private lateinit var pass: EditText
    private lateinit var confirmPass: EditText
    private lateinit var number: EditText
    private lateinit var signUp: View
    private val baseUrl = "http://46.100.50.100:63938/"
    private val signUpService = SignUp(baseUrl)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.signup_tab_fragment, container, false)

        username = root.findViewById(R.id.username)
        pass = root.findViewById(R.id.password)
        confirmPass = root.findViewById(R.id.confirm_password)
        number = root.findViewById(R.id.number)
        signUp = root.findViewById(R.id.signup)

        val v = 0f

        username.translationX = 800f
        pass.translationX = 800f
        confirmPass.translationX = 800f
        number.translationX = 800f
        signUp.translationX = 800f

        username.alpha = v
        pass.alpha = v
        confirmPass.alpha = v
        number.alpha = v
        signUp.alpha = v

        username.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(300).start()
        pass.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(500).start()
        confirmPass.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(500).start()
        number.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(700).start()
        signUp.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(700).start()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        signUp.setOnClickListener {
            validateSignUp { isValid ->
                if (isValid) {
                    // If valid, start MapsActivity
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish() // Finish SignUpActivity
                } else {
                    // If invalid, show toast
                    Toast.makeText(
                        requireContext(),
                        "Invalid sign-up credentials",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

    private fun validateSignUp(callback: (Boolean) -> Unit) {
        val usernameInput = username.text.toString()
        val passwordInput = pass.text.toString()
        val confirmPasswordInput = confirmPass.text.toString()
        val numberInput = number.text.toString()

        if (passwordInput != confirmPasswordInput) {
            callback(false)
            return
        }

        // Assuming SignUpService is a mock service for demonstration
        signUpService.signUp(usernameInput, passwordInput, numberInput) { response ->
            if (response != null) {
                Log.d("SignUpResponse", "Response: $response")
                val isValid = response.message == "User registered successfully"
                callback(isValid)
            } else {
                Log.d("SignUpResponse", "Response is null")
                callback(false)
            }
        }
    }
}

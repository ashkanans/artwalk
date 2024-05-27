package io.ashkanans.artwalk


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize your login button and set its onClickListener
        val loginButton = findViewById<Button>(R.id.login)
        loginButton.setOnClickListener {
            // Handle login logic
            if (validateLogin()) {
                // If login is successful, start the MapsActivity
                val intent = Intent(
                    this@LoginActivity,
                    MapsActivity::class.java
                )
                startActivity(intent)
                finish() // Finish LoginActivity so user can't go back to it
            } else {
                // Show error message
                Toast.makeText(this@LoginActivity, "Invalid login credentials", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun validateLogin(): Boolean {
        // Implement your login validation logic here
        // Return true if valid, false otherwise
        return true // This is a placeholder
    }
}
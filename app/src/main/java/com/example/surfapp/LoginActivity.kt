package com.example.surfapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton = findViewById<ImageView>(R.id.loginButton)


        loginButton.setOnClickListener {
            // Open the main activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
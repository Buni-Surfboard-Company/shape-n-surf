package com.example.surfapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.surfapp.R

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
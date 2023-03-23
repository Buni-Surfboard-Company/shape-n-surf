package com.example.surfapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.surfapp.R
import androidx.navigation.fragment.findNavController

class LoginFragment : Fragment(R.layout.login_fragment) {

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val loginButton = view.findViewById<ImageView>(R.id.loginButton)

        loginButton.setOnClickListener {
            //later we can be passing user data (profile picture/name) when opening homescreen from login page
            val directions = LoginFragmentDirections.navigateToHomescreen()
            findNavController().navigate(directions)
        }
    }
}
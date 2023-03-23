package com.example.surfapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import com.example.surfapp.R
import org.opencv.android.OpenCVLoader

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        OpenCVLoader.initDebug()

        // Disable swipe back so that the users won't be able go back to the log in page by accident
        val callback = object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() {
                // do nothing
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        val surfForecastViewButton: LinearLayout = findViewById(R.id.surfForecastViewButton)
        surfForecastViewButton.setOnClickListener {
            val intent = Intent(this, ForecastActivity::class.java)
            startActivity(intent)
        }

        val uploadButton: LinearLayout = findViewById(R.id.uploadBoardsViewButton)
        uploadButton.setOnClickListener {
            val intent = Intent(this, ScanBoardActivity::class.java)
            startActivity(intent)
        }
    }
}

//    private fun onLoginClick(repo: GitHubRepo) {
//        val intent = Intent(this, RepoDetailActivity::class.java)
//        intent.putExtra(EXTRA_GITHUB_REPO, repo)
//        startActivity(intent)
//    }
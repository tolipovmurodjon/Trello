package com.mcompany.trello.m.t.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import com.mcompany.trello.R
import com.mcompany.trello.m.t.firebase.Firestore

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({

            var currentUserID = Firestore().getCurrentUserID()

            if (!currentUserID.isNullOrEmpty()){
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else{
                startActivity(Intent(this, IntroActivity::class.java))
                finish()

            }

            // Start the next activity

        }, 4000) //

    }
}
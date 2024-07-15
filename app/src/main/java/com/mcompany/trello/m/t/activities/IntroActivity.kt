package com.mcompany.trello.m.t.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.mcompany.trello.databinding.ActivityIntroBinding

class IntroActivity : BaseActivity() {

    private var binding: ActivityIntroBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        blackIcon()

        binding?.btnSignInIntro?.setOnClickListener{
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }


        binding?.btnSignUpIntro?.setOnClickListener{
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)

        }




    }

    private fun blackIcon() {
        val window = window
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }
}
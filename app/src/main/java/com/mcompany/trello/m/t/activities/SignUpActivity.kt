package com.mcompany.trello.m.t.activities

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mcompany.trello.R
import com.mcompany.trello.databinding.ActivitySignUpBinding
import com.mcompany.trello.m.t.firebase.Firestore
import com.mcompany.trello.m.t.model.User

class SignUpActivity : BaseActivity() {

    private var binding: ActivitySignUpBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        toolbar()
        blackIcon()

        binding?.btnSignUp?.setOnClickListener{
            registerUser()
        }

    }

    fun userRegisteredSuccess() {

        Toast.makeText(
            this@SignUpActivity,
            "You have successfully registered.",
            Toast.LENGTH_SHORT
        ).show()

        // Hide the progress dialog
        hideProgressDialog()

        /**
         * Here the new user registered is automatically signed-in so we just sign-out the user from firebase
         * and send him to Intro Screen for Sign-In
         */
        FirebaseAuth.getInstance().signOut()
        // Finish the Sign-Up Screen
        finish()
    }

    private fun registerUser(){
        val name: String = binding?.etName?.text.toString().trim { it <= ' ' }
        val email: String = binding?.etEmail?.text.toString().trim { it <= ' ' }
        val password: String = binding?.etPassword?.text.toString().trim { it <= ' ' }

        if (validateForm(name, email, password)) {

            showProgressDialog(resources.getString(R.string.please_wait))

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener{
                task ->
                hideProgressDialog()

                if (task.isSuccessful){

                    val firebaseUser : FirebaseUser = task.result!!.user!!
                    val registeredUser = firebaseUser.email!!

                    val user = User(firebaseUser.uid, name, registeredUser)

                    Firestore().registerUser(this, user)

                } else {
                    Toast.makeText(this@SignUpActivity, task.exception!!.message, Toast.LENGTH_LONG).show()
                }
            }


        }
    }

    private fun validateForm(name: String, email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter name.")
                false
            }
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter email.")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter password.")
                false
            }
            else -> {
                true
            }
        }
    }

    private fun blackIcon() {
        val window = window
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }

    private fun toolbar(){
        setSupportActionBar(binding?.toolbarSignUpActivity)

        //Navigate Up

        if (supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24)
        }

        binding?.toolbarSignUpActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
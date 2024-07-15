package com.mcompany.trello.m.t.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.mcompany.trello.R
import com.mcompany.trello.databinding.ActivityMainBinding
import com.mcompany.trello.databinding.MainContentBinding
import com.mcompany.trello.databinding.NavHeaderMainBinding
import com.mcompany.trello.m.t.adapters.BoardItemsAdapter
import com.mcompany.trello.m.t.firebase.Firestore
import com.mcompany.trello.m.t.model.Board
import com.mcompany.trello.m.t.model.User
import com.mcompany.trello.m.t.utils.Constants
import com.squareup.picasso.Picasso

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener{

    private var binding: ActivityMainBinding? = null
    private var binding2: MainContentBinding? = null
    private var doubleBackToExitPressedOnce = false
    private lateinit var mSharedPreferences: SharedPreferences

    private lateinit var mUserName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding2 = MainContentBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        binding?.navView?.setNavigationItemSelectedListener(this)

        mSharedPreferences =
            this.getSharedPreferences(Constants.PROGEMANAG_PREFERENCES, Context.MODE_PRIVATE)


        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        // Here if the token is already updated than we don't need to update it every time.
        if (tokenUpdated) {
            // Get the current logged in user details.
            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))
            Firestore().loadUserData(this@MainActivity, true)
        } else {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener(this@MainActivity) {
                    updateFCMToken(it)
                }
        }


//        Firestore().loadUserData(this@MainActivity, true)

        val fab : FloatingActionButton = findViewById(R.id.fab_create_board)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, BoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            boardLauncher.launch(intent)
//            startActivity(intent)
        }

    }

    private val startUpdateActivityAndGetResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Firestore().loadUserData(this)
            } else {
                Log.e("onActivityResult()", "Profile update cancelled by user")
            }

            binding?.navView?.menu?.findItem(R.id.nav_my_profile)?.isChecked = false
        }

    private val boardLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == Activity.RESULT_OK){
            Firestore().getBoardsList(this)
        }
    }

    fun updateNavigationUserDetails(user : User, isToReadBoardsList: Boolean){

        hideProgressDialog()

        // The instance of the header view of the navigation view.
        val viewHeader = binding?.navView?.getHeaderView(0)
        val headerBinding = viewHeader?.let { NavHeaderMainBinding.bind(it) }
        headerBinding?.ivUserImageMain?.let {
            image ->
            val imageUrl = user.image
            mUserName = user.name
            Log.d("MainActivity", "User image URL: $imageUrl")

            if (user.image.isNotEmpty()) {
                Picasso.get()
                    .load(user.image)
                    .placeholder(R.drawable.ic_user_place_holder)
                    .error(R.drawable.ic_user_place_holder)
                    .into(image)
            } else {
                // Set a placeholder image if the URL is empty
                image.setImageResource(R.drawable.ic_user_place_holder)
            }

//            Picasso.get()
//                .load(user.image)
//                .placeholder(R.drawable.ic_user_place_holder)
//                .error(R.drawable.ic_user_place_holder)
//                .into(headerBinding.ivUserImageMain);

//            Glide
//                .with(this)
//                .load(imageUrl) // URL of the image
//                .centerCrop() // Scale type of the image.
//                .placeholder(R.drawable.ic_user_place_holder) // A default place holder
//                .skipMemoryCache(true) // Skip memory cache
//                .diskCacheStrategy(DiskCacheStrategy.NONE) // Skip disk cache
//                .into(it)



        } // the view in which the image will be loaded.

        if (isToReadBoardsList) {
            Log.d("MainActivity", "Working")
            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))
            Firestore().getBoardsList(this@MainActivity)
        }

        headerBinding?.tvUsername?.text = user.name
    }

    fun populateBoardsListToUI(boardsList: ArrayList<Board>) {

        hideProgressDialog()

        if (boardsList.size > 0) {
            Log.d("MainActivity", "Working RV")


            findViewById<RecyclerView>(R.id.rv_boards_list).visibility = View.VISIBLE
            findViewById<TextView>(R.id.tv_no_boards_available).visibility = View.GONE

            findViewById<RecyclerView>(R.id.rv_boards_list).layoutManager = LinearLayoutManager(this@MainActivity)
            findViewById<RecyclerView>(R.id.rv_boards_list).setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this@MainActivity, boardsList)
            findViewById<RecyclerView>(R.id.rv_boards_list).adapter = adapter

            adapter.setOnClickListener(object : BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {

                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)

                }
            })


            Log.i("POPUI:", "Board adapter size: ${adapter.itemCount}")
        } else {
            findViewById<RecyclerView>(R.id.rv_boards_list).visibility = View.INVISIBLE
            findViewById<TextView>(R.id.tv_no_boards_available).visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        // Check if the drawer is open
        if (binding?.drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            // Close the drawer if it is open
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        } else {
            // Handle double back press to exit
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }

            this.doubleBackToExitPressedOnce = true
            Toast.makeText(
                this,
                resources.getString(R.string.please_click_back_again_to_exit),
                Toast.LENGTH_SHORT
            ).show()

            // Reset the flag after 2 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                doubleBackToExitPressedOnce = false
            }, 2000)
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {

        when (menuItem.itemId) {
            R.id.nav_my_profile -> {

                startUpdateActivityAndGetResult.launch(Intent(this, ProfileActivity::class.java))

            }

            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                mSharedPreferences.edit().clear().apply()

                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
                menuItem.isChecked = false
            }
        }
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        return true
    }

    private fun setupActionBar() {
        val toolbarMainActivity: Toolbar = findViewById(R.id.toolbar_main_activity)
        setSupportActionBar(toolbarMainActivity)

        toolbarMainActivity.setNavigationIcon(R.drawable.baseline_menu_24)

        toolbarMainActivity.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer() {

        if (binding?.drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            binding?.drawerLayout!!.closeDrawer(GravityCompat.START)
        } else {
            binding?.drawerLayout!!.openDrawer(GravityCompat.START)
        }
    }

    fun tokenUpdateSuccess() {

        hideProgressDialog()

        // Here we have added a another value in shared preference that the token is updated in the database successfully.
        // So we don't need to update it every time.
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()

        // Get the current logged in user details.
        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        Firestore().loadUserData(this@MainActivity, true)
    }

    private fun updateFCMToken(token: String) {

        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token

        // Update the data in the database.
        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        Firestore().updateUserProfileData(this@MainActivity, userHashMap)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }




}
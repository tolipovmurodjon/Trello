package com.mcompany.trello.m.t.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mcompany.trello.R
import com.mcompany.trello.databinding.ActivityBoardBinding
import com.mcompany.trello.m.t.firebase.Firestore
import com.mcompany.trello.m.t.model.Board
import com.mcompany.trello.m.t.utils.Constants
import java.io.IOException

class BoardActivity : BaseActivity() {

    private var binding: ActivityBoardBinding? = null

    private var mSelectedImageFileUri : Uri? = null
    private var mBoardImageURL: String = ""
    private lateinit var mUserName: String


    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            showImageChooser()
        } else {
            Toast.makeText(
                this,
                "Oops, you just denied the permission for storage. You can also allow it from settings.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null && data.data != null) {
                mSelectedImageFileUri = data.data
                try {
                    Glide.with(this@BoardActivity)
                        .load(Uri.parse(mSelectedImageFileUri.toString()))
                        .centerCrop()
                        .placeholder(R.drawable.ic_user_place_holder)
                        .into(binding?.ivBoardImage!!)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        toolbar()

        if (intent.hasExtra(Constants.NAME)) {
            mUserName = intent.getStringExtra(Constants.NAME)!!
        }

        binding?.ivBoardImage?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                showImageChooser()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        binding?.btnCreate?.setOnClickListener {

            // Here if the image is not selected then update the other details of user.
            if (mSelectedImageFileUri != null) {

                uploadBoardImage()
            } else {

                showProgressDialog(resources.getString(R.string.please_wait))

                // Call a function to update create a board.
                createBoard()
            }
        }

    }

    private fun createBoard() {

        //  A list is created to add the assigned members.
        //  This can be modified later on as of now the user itself will be the member of the board.
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserID())

        val mUserID = getCurrentUserID()

        // Creating the instance of the Board and adding the values as per parameters.
        val board = Board(
            binding?.etBoardName?.text.toString(),
            mBoardImageURL,
            mUserName,
            assignedUsersArrayList
        )

        Firestore().createBoard(this@BoardActivity, board)
    }

    private fun uploadBoardImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        //getting the storage reference
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
            "BOARD_IMAGE" + System.currentTimeMillis() + "."
                    + getFileExtension(mSelectedImageFileUri)
        )

        //adding the file to reference
        sRef.putFile(mSelectedImageFileUri!!)
            .addOnSuccessListener { taskSnapshot ->
                // The image upload is success
                Log.e(
                    "Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                // Get the downloadable url from the task snapshot
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        Log.e("Downloadable Image URL", uri.toString())

                        // assign the image url to the variable.
                        mBoardImageURL = uri.toString()

                        // Call a function to create the board.
                        createBoard()
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this@BoardActivity,
                    exception.message,
                    Toast.LENGTH_LONG
                ).show()

                hideProgressDialog()
            }
    }

    fun boardCreatedSuccessfully() {

        hideProgressDialog()

        setResult(Activity.RESULT_OK)

        finish()
    }

    private fun getFileExtension(uri: Uri?): String? {

        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    private fun showImageChooser() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickImageLauncher.launch(galleryIntent)
    }

    private fun toolbar(){
        setSupportActionBar(binding?.toolbarCreateBoardActivity)
        if (supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24_white)
            supportActionBar?.title = resources.getString(R.string.create_board_title)
        }

        binding?.toolbarCreateBoardActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
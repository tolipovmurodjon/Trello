package com.mcompany.trello.m.t.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mcompany.trello.R
import com.mcompany.trello.databinding.ActivityProfileBinding
import com.mcompany.trello.m.t.firebase.Firestore
import com.mcompany.trello.m.t.model.User
import com.mcompany.trello.m.t.utils.Constants
import com.squareup.picasso.Picasso
import java.io.IOException

class ProfileActivity : BaseActivity() {

    private var binding: ActivityProfileBinding? = null

    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageURL: String = ""
    private lateinit var mUserDetails: User

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
                        Glide.with(this@ProfileActivity)
                            .load(Uri.parse(mSelectedImageFileUri.toString()))
                            .centerCrop()
                            .placeholder(R.drawable.ic_user_place_holder)
                            .into(binding?.ivUserImageProfile!!)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        toolbar()

        Firestore().loadUserData(this@ProfileActivity)

        binding?.ivUserImageProfile?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                showImageChooser()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        binding?.btnUpdate?.setOnClickListener {
            if (mSelectedImageFileUri !=  null){
                uploadUserImage()
            } else {

                showProgressDialog(resources.getString(R.string.please_wait))

                updateUserProfileData()
            }
        }

    }

    fun profileUpdateSuccess() {

        hideProgressDialog()

        setResult(Activity.RESULT_OK)

        finish()
    }

    private fun updateUserProfileData() {

        val userHashMap = HashMap<String, Any>()

        if (mProfileImageURL.isNotEmpty() && mProfileImageURL!= mUserDetails.image) {
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }

        if (binding?.etName?.text.toString()!= mUserDetails.name) {
            userHashMap[Constants.NAME] = binding?.etName?.text.toString()
        }

        if (binding?.etMobile?.text.toString()!= mUserDetails.mobile.toString()) {
            if (binding?.etMobile?.text.toString() == "") {
                userHashMap[Constants.MOBILE] = 0L
            } else {
                userHashMap[Constants.MOBILE] = binding?.etMobile?.text.toString().toLong()
            }
        }

        Firestore().updateUserProfileData(this@ProfileActivity, userHashMap)
    }

    private fun showImageChooser() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickImageLauncher.launch(galleryIntent)
    }

    private fun uploadUserImage() {

        showProgressDialog(resources.getString(R.string.please_wait))

        if (mSelectedImageFileUri != null) {

            //getting the storage reference
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE" + System.currentTimeMillis() + "." + getFileExtension(
                    mSelectedImageFileUri
                )
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
                            mProfileImageURL = uri.toString()



                            updateUserProfileData()
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this@ProfileActivity,
                        exception.message,
                        Toast.LENGTH_LONG
                    ).show()

                    hideProgressDialog()
                }
        }
    }

    private fun getFileExtension(uri: Uri?): String? {

        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    private fun toolbar(){
        setSupportActionBar(binding?.toolbarMyProfileActivity)
        if (supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24_white)
            supportActionBar?.title = resources.getString(R.string.my_profile)
        }

        binding?.toolbarMyProfileActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    fun setUserDataInUI(user: User) {

        mUserDetails = user

        if (user.image.isNotEmpty()) {
            Picasso.get()
                .load(user.image)
                .placeholder(R.drawable.ic_user_place_holder)
                .error(R.drawable.ic_user_place_holder)
                .into(binding?.ivUserImageProfile)
        } else {
            // Set a placeholder image if the URL is empty
            binding?.ivUserImageProfile?.setImageResource(R.drawable.ic_user_place_holder)
        }



//        Picasso.get()
//            .load(user.image)
//            .placeholder(R.drawable.ic_user_place_holder)
//            .error(R.drawable.ic_user_place_holder)
//            .into(binding?.ivUserImageProfile);



//        Glide
//            .with(this@ProfileActivity)
//            .load(user.image)
//            .centerCrop()
//            .placeholder(R.drawable.ic_user_place_holder)
//            .into(binding?.ivUserImageProfile!!)


        binding?.etName?.setText(user.name)
        binding?.etEmail?.setText(user.email)
        if (user.mobile != 0L) {
            binding?.etMobile?.setText(user.mobile.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}
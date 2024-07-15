package com.mcompany.trello.m.t.activities
import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.mcompany.trello.R
import com.mcompany.trello.databinding.ActivityMembersBinding
import com.mcompany.trello.m.t.adapters.MemberListItemsAdapter
import com.mcompany.trello.m.t.firebase.Firestore
import com.mcompany.trello.m.t.model.Board
import com.mcompany.trello.m.t.model.User
import com.mcompany.trello.m.t.utils.Constants

class MembersActivity : BaseActivity() {

    private var binding : ActivityMembersBinding? = null

    private lateinit var mBoardDetails: Board
    private lateinit var mAssignedMembersList:ArrayList<User>
    private var anyChangesDone: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding?.root)

       toolbar()

        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        Firestore().getAssignedMembersListDetails(
            this@MembersActivity,
            mBoardDetails.assignedTo)


    }

    private fun toolbar() {

        setSupportActionBar(binding?.toolbarMembersActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24_white)
            actionBar.title = resources.getString(R.string.members)
        }

        binding?.toolbarMembersActivity?.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu to use in the action bar
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.action_add_member -> {

                // TODO (Step 7: Call the dialogSearchMember function here.)
                // START
                dialogSearchMember()
                // END
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogSearchMember() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.findViewById<TextView>(R.id.tv_add).setOnClickListener(View.OnClickListener {

            val email = dialog.findViewById<AppCompatEditText>(R.id.et_email_search_member).text.toString()

            if (email.isNotEmpty()) {
                showProgressDialog(resources.getString(R.string.please_wait))
                Firestore().getMemberDetails(this@MembersActivity, email)
                dialog.dismiss()

            } else {
                Toast.makeText(
                    this@MembersActivity,
                    "Please enter members email address.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        dialog.findViewById<TextView>(R.id.tv_cancel).setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })
        //Start the dialog and display it on screen.
        dialog.show()
    }

    fun setupMembersList(list: ArrayList<User>) {

        mAssignedMembersList = list

        hideProgressDialog()

        binding?.rvMembersList?.layoutManager = LinearLayoutManager(this@MembersActivity)
        binding?.rvMembersList?.setHasFixedSize(true)

        val adapter = MemberListItemsAdapter(this@MembersActivity, list)
        binding?.rvMembersList?.adapter = adapter
    }

    fun memberDetails(user: User) {

        // TODO (Step 6: Here add the user id to the existing assigned members list of the board.)
        // START
        mBoardDetails.assignedTo.add(user.id)

        // TODO (Step 9: Finally assign the member to the board.)
        // START
        Firestore().assignMemberToBoard(this@MembersActivity, mBoardDetails, user)
        // ENDss
    }

    fun memberAssignSuccess(user: User) {

        hideProgressDialog()

        mAssignedMembersList.add(user)
        anyChangesDone = true
        setupMembersList(mAssignedMembersList)

//        SendNotificationToUserAsyncTask(mBoardDetails.name, user.fcmToken).execute()

    }

    override fun onBackPressed() {
        super.onBackPressed()

        if (anyChangesDone) {
            setResult(Activity.RESULT_OK)
        } else{

        }
    }

}
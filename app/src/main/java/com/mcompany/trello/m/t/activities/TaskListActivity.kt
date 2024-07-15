package com.mcompany.trello.m.t.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.mcompany.trello.R
import com.mcompany.trello.databinding.ActivityTaskListBinding
import com.mcompany.trello.m.t.adapters.TaskListItemsAdapter
import com.mcompany.trello.m.t.firebase.Firestore
import com.mcompany.trello.m.t.model.Board
import com.mcompany.trello.m.t.model.Card
import com.mcompany.trello.m.t.model.Task
import com.mcompany.trello.m.t.model.User
import com.mcompany.trello.m.t.utils.Constants

class TaskListActivity : BaseActivity() {

    private var binding : ActivityTaskListBinding? = null
    private lateinit var mBoardDetails: Board
    private lateinit var mBoardDocumentId: String
    lateinit var mAssignedMembersDetailList: ArrayList<User>


    private var membersActivityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Handle the result here
            val data = result.data
            showProgressDialog(resources.getString(R.string.please_wait))
            Firestore().getBoardDetails(this@TaskListActivity, mBoardDocumentId)

        } else{
            Log.e("Cancelled", "Cancelled")
        }
    }

    private var cardActivityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data

            showProgressDialog(resources.getString(R.string.please_wait))
            Firestore().getBoardDetails(this@TaskListActivity, mBoardDocumentId)

        } else{
            Log.e("Cancelled", "Cancelled")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding?.root)

//        var boardDocumentId = ""
        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            mBoardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        Firestore().getBoardDetails(this@TaskListActivity, mBoardDocumentId)

    }

    private fun toolbar() {

        setSupportActionBar(binding?.toolbarTaskListActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24_white)
            actionBar.title = mBoardDetails.name
        }

        binding?.toolbarTaskListActivity?.setNavigationOnClickListener { onBackPressed() }
    }

    fun boardDetails(board: Board) {

        hideProgressDialog()
        mBoardDetails = board
        toolbar()

//        binding?.rvTaskList?.layoutManager =
//            LinearLayoutManager(this@TaskListActivity, LinearLayoutManager.HORIZONTAL, false)
//        binding?.rvTaskList?.setHasFixedSize(true)
//
//        val addTaskList = Task(resources.getString(R.string.add_list))
//        board.taskList.add(addTaskList)
//
//        val adapter = TaskListItemsAdapter(this@TaskListActivity, board.taskList)
//        binding?.rvTaskList?.adapter = adapter // Attach the adapter to the recyclerView.


        showProgressDialog(resources.getString(R.string.please_wait))
        Firestore().getAssignedMembersListDetails(
            this@TaskListActivity,
            mBoardDetails.assignedTo
        )


    }

    fun boardMembersDetailList(list: ArrayList<User>) {

        mAssignedMembersDetailList = list

        binding?.rvTaskList?.layoutManager =
            LinearLayoutManager(this@TaskListActivity, LinearLayoutManager.HORIZONTAL, false)
        binding?.rvTaskList?.setHasFixedSize(true)

        val addTaskList = Task(resources.getString(R.string.add_list))
        mBoardDetails.taskList.add(addTaskList)

        val adapter = TaskListItemsAdapter(this@TaskListActivity, mBoardDetails.taskList)
        binding?.rvTaskList?.adapter = adapter // Attach the adapter to the recyclerView.

        hideProgressDialog()
    }

    fun addUpdateTaskListSuccess() {

        hideProgressDialog()

        // Here get the updated board details.
        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        Firestore().getBoardDetails(this@TaskListActivity, mBoardDetails.documentId)
    }

    fun createTaskList(taskListName: String) {

        Log.e("Task List Name", taskListName)

        // Create and Assign the task details
        val task = Task(taskListName, Firestore().getCurrentUserID())

        mBoardDetails.taskList.add(0, task) // Add task to the first position of ArrayList
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1) // Remove the last position as we have added the item manually for adding the TaskList.

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))

        Firestore().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    fun updateTaskList(position: Int, listName: String, model: Task) {

        val task = Task(listName, model.createdBy)

        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))
        Firestore().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    fun deleteTaskList(position: Int){

        mBoardDetails.taskList.removeAt(position)

        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        Firestore().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    fun addCardToTaskList(position: Int, cardName: String) {

        // Remove the last item
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(Firestore().getCurrentUserID())

        val card = Card(cardName, Firestore().getCurrentUserID(), cardAssignedUsersList)

        val cardsList = mBoardDetails.taskList[position].cards
        cardsList.add(card)

        val task = Task(
            mBoardDetails.taskList[position].title,
            mBoardDetails.taskList[position].createdBy,
            cardsList
        )

        mBoardDetails.taskList[position] = task

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        Firestore().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_members -> {

                val intent = Intent(this@TaskListActivity, MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
                membersActivityResultLauncher.launch(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun cardDetails(taskListPosition: Int, cardPosition: Int) {

        val intent = Intent(this@TaskListActivity, CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST, mAssignedMembersDetailList)
        cardActivityResultLauncher.launch(intent)
    }

    fun updateCardsInTaskList(taskListPosition: Int, cards: ArrayList<Card>) {

        // Remove the last item
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        mBoardDetails.taskList[taskListPosition].cards = cards

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        Firestore().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }


//    override fun onResume() {
//        super.onResume()
//
//        showProgressDialog(resources.getString(R.string.please_wait))
//        Firestore().getBoardDetails(this@TaskListActivity, mBoardDocumentId)
//    }

    companion object {
        const val MEMBERS_REQUEST_CODE: Int = 13
    }


}



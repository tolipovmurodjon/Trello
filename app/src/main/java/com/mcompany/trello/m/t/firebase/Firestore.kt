package com.mcompany.trello.m.t.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mcompany.trello.m.t.activities.BoardActivity
import com.mcompany.trello.m.t.activities.CardDetailsActivity
import com.mcompany.trello.m.t.activities.MainActivity
import com.mcompany.trello.m.t.activities.MembersActivity
import com.mcompany.trello.m.t.activities.ProfileActivity
import com.mcompany.trello.m.t.activities.SignInActivity
import com.mcompany.trello.m.t.activities.SignUpActivity
import com.mcompany.trello.m.t.activities.TaskListActivity
import com.mcompany.trello.m.t.model.Board
import com.mcompany.trello.m.t.model.User
import com.mcompany.trello.m.t.utils.Constants

class Firestore {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) {

        mFireStore.collection(Constants.USERS)
            // Document ID for users fields. Here the document it is the User ID.
            .document(getCurrentUserID())
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(
                    activity.javaClass.simpleName,
                    "Error writing document",
                    e
                )
            }
    }

    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>) {
        if (userHashMap.isNotEmpty()) {
            mFireStore.collection(Constants.USERS) // Collection Name
                .document(getCurrentUserID()) // Document ID
                .update(userHashMap) // A hashmap of fields which are to be updated.
                .addOnSuccessListener {
                    // Profile data is updated successfully.
                    Log.e(activity.javaClass.simpleName, "Profile Data updated successfully!")



                    when (activity) {
                        is MainActivity -> {
                            activity.tokenUpdateSuccess()
                        }
                        is ProfileActivity -> {

                            Toast.makeText(activity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                            activity.profileUpdateSuccess()
                        }
                    }

                }
                .addOnFailureListener { e ->
                    when (activity) {
                        is MainActivity -> {
                            activity.hideProgressDialog()
                        }
                        is ProfileActivity -> {
                            activity.hideProgressDialog()
                        }
                    }

                }
        } else {
            Log.e(
                activity.javaClass.simpleName,
                "Error while creating a board."
            )

        }
    }

    fun loadUserData(activity: Activity, isToReadBoardsList: Boolean = false) {

        // Here we pass the collection name from which we wants the data.
        mFireStore.collection(Constants.USERS)
            // The document id to get the Fields of user.
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.e(
                    activity.javaClass.simpleName, document.toString()
                )

                // TODO (STEP 3: Pass the result to base activity.)
                // START
                // Here we have received the document snapshot which is converted into the User Data model object.
                val loggedInUser = document.toObject(User::class.java)!!

                // Here call a function of base activity for transferring the result to it.
                when (activity) {
                    is SignInActivity -> {
                        activity.signInSuccess(loggedInUser)
                    }
                    is MainActivity -> {
                        activity.updateNavigationUserDetails(loggedInUser, isToReadBoardsList)
                    }
                    is ProfileActivity -> {
                        activity.setUserDataInUI(loggedInUser)
                    }
                    // END
                }

            }
            .addOnFailureListener { e ->
                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }

                    is ProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                    // END
                }

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting loggedIn user details",
                    e
                )
            }
    }

    fun createBoard(activity: BoardActivity, board: Board) {

        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Board created successfully.")

                Toast.makeText(activity, "Board created successfully.", Toast.LENGTH_SHORT).show()

                activity.boardCreatedSuccessfully()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board.",
                    e
                )
            }
    }

    fun getBoardsList(activity: MainActivity) {

        // The collection name for BOARDS
        mFireStore.collection(Constants.BOARDS)
            // A where array query as we want the list of the board in which the user is assigned. So here you can pass the current user id.
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserID())
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->
                // Here we get the list of boards in the form of documents.
                Log.e(activity.javaClass.simpleName, document.documents.toString())
                // Here we have created a new instance for Boards ArrayList.
                val boardsList: ArrayList<Board> = ArrayList()

                // A for loop as per the list of documents to convert them into Boards ArrayList.
                for (i in document.documents) {

                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id

                    boardsList.add(board)
                }

                // Here pass the result to the base activity.
                activity.populateBoardsListToUI(boardsList)
            }
            .addOnFailureListener { e ->

                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }

    fun getBoardDetails(activity: TaskListActivity, documentId: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.toString())

                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id

                // Send the result of board to the base activity.
                activity.boardDetails(board)

            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }

    fun addUpdateTaskList(activity: Activity, board: Board) {

        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList updated successfully.")

                if (activity is TaskListActivity) {
                    activity.addUpdateTaskListSuccess()
                } else if (activity is CardDetailsActivity) {
                    activity.addUpdateTaskListSuccess()
                }

            }
            .addOnFailureListener { e ->
                if (activity is TaskListActivity) {
                    activity.hideProgressDialog()
                } else if (activity is TaskListActivity) {
                    activity.hideProgressDialog()

                    Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
                }
            }
    }

    fun getAssignedMembersListDetails(
        activity: Activity,
        assignedTo: ArrayList<String>
    ) {

        mFireStore.collection(Constants.USERS) // Collection Name
            .whereIn(
                Constants.ID,
                assignedTo
            )
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                val usersList: ArrayList<User> = ArrayList()

                for (i in document.documents) {
                    // Convert all the document snapshot to the object using the data model class.
                    val user = i.toObject(User::class.java)!!
                    usersList.add(user)
                }

                if(activity is MembersActivity) {
                    activity.setupMembersList(usersList)
                }else if(activity is TaskListActivity) {
                    activity.boardMembersDetailList(usersList)
                }

            }
            .addOnFailureListener { e ->
                if(activity is MembersActivity) {
                    activity.hideProgressDialog()
                }else if(activity is TaskListActivity){
                    activity.hideProgressDialog()
                }

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board.",
                    e
                )
            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String) {

        // Here we pass the collection name from which we wants the data.
        mFireStore.collection(Constants.USERS)
            // A where array query as we want the list of the board in which the user is assigned. So here you can pass the current user id.
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                if (document.documents.size > 0) {
                    val user = document.documents[0].toObject(User::class.java)!!
                    // Here call a function of base activity for transferring the result to it.
                    activity.memberDetails(user)
                } else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found.")
                }

            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting user details",
                    e
                )
            }
    }

    fun assignMemberToBoard(activity: MembersActivity, board: Board, user: User) {

        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList updated successfully.")
                activity.memberAssignSuccess(user)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }

    fun getCurrentUserID(): String {

        var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID

    }

}
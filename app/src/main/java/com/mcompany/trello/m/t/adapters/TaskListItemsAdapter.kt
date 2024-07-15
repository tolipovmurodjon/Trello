package com.mcompany.trello.m.t.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mcompany.trello.m.t.model.Task
import com.mcompany.trello.databinding.ItemTaskBinding
import com.mcompany.trello.m.t.activities.TaskListActivity
import java.util.Collections

class TaskListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Task>
) : RecyclerView.Adapter<TaskListItemsAdapter.MyViewHolder>() {

    private var mPositionDraggedFrom = -1
    private var mPositionDraggedTo = -1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(context), parent, false)
        val layoutParams = binding.root.layoutParams
        layoutParams.width = (parent.width * 0.7).toInt()
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        binding.root.layoutParams = layoutParams
        binding.root.apply {
            (layoutParams as ViewGroup.MarginLayoutParams).apply {
                setMargins(dpToPx(15), 0, dpToPx(40), 0)
            }
        }
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val model = list[position]
        with(holder.binding) {
            if (position == list.size - 1) {
                tvAddTaskList.visibility = View.VISIBLE
                llTaskItem.visibility = View.GONE
            } else {
                tvAddTaskList.visibility = View.GONE
                llTaskItem.visibility = View.VISIBLE
            }

            tvTaskListTitle.text = model.title

            tvAddTaskList.setOnClickListener {
                tvAddTaskList.visibility = View.GONE
                cvAddTaskListName.visibility = View.VISIBLE
            }

            ibCloseListName.setOnClickListener {
                tvAddTaskList.visibility = View.VISIBLE
                cvAddTaskListName.visibility = View.GONE
            }

            ibDoneListName.setOnClickListener {
                val listName = etTaskListName.text.toString()

                if (listName.isNotEmpty()) {
                    // Here we check the context is an instance of the TaskListActivity.
                    if (context is TaskListActivity) {
                        context.createTaskList(listName)
                    }
                } else {
                    Toast.makeText(context, "Please Enter List Name.", Toast.LENGTH_SHORT).show()
                }

            }

            ibEditListName.setOnClickListener {

                etEditTaskListName.setText(model.title) // Set the existing title
                llTitleView.visibility = View.GONE
                cvEditTaskListName.visibility = View.VISIBLE
            }
            // END

            // TODO (Step 2: Add a click event for iv_close_editable_view for hiding the editable view.)
            // START
            ibCloseEditableView.setOnClickListener {
                llTitleView.visibility = View.VISIBLE
                cvEditTaskListName.visibility = View.GONE
            }
            // END

            // TODO (Step 4: Add a click event for iv_edit_list for showing thr editable view.)
            // START
            ibDoneEditListName.setOnClickListener {
                val listName = etEditTaskListName.text.toString()

                if (listName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.updateTaskList(position, listName, model)
                    }
                } else {
                    Toast.makeText(context, "Please Enter List Name.", Toast.LENGTH_SHORT).show()
                }
            }
            // END

            // TODO (Step 7: Add a click event for ib_delete_list for deleting the task list.)
            // START
            ibDeleteList.setOnClickListener {

                alertDialogForDeleteList(position, model.title)
            }

            tvAddCard.setOnClickListener {

                tvAddCard.visibility = View.GONE
                cvAddCard.visibility = View.VISIBLE

                // TODO (Step 4: Add a click event for closing the view for card add in the task list.)
                // START
                ibCloseCardName.setOnClickListener {
                    tvAddCard.visibility = View.VISIBLE
                    cvAddCard.visibility = View.GONE
                }
                // END

                // TODO (Step 6: Add a click event for adding a card in the task list.)
                // START
                ibDoneCardName.setOnClickListener {

                    val cardName = etCardName.text.toString()

                    if (cardName.isNotEmpty()) {
                        if (context is TaskListActivity) {
                            context.addCardToTaskList(position, cardName)
                        }
                    }else{
                        Toast.makeText(context, "Please Enter Card Detail.", Toast.LENGTH_SHORT).show()
                    }
                }


            }

            rvCardList.layoutManager = LinearLayoutManager(context)
            rvCardList.setHasFixedSize(true)

            val adapter = CardListItemsAdapter(context, model.cards)
            rvCardList.adapter = adapter

            adapter.setOnClickListener(object :
                CardListItemsAdapter.OnClickListener {
                override fun onClick(cardPosition: Int) {

                    if (context is TaskListActivity) {
                        context.cardDetails(holder.absoluteAdapterPosition, cardPosition)
                    }
                }
            })

            val dividerItemDecoration =
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            rvCardList.addItemDecoration(dividerItemDecoration)

            //  Creates an ItemTouchHelper that will work with the given Callback.
            val helper = ItemTouchHelper(object :
                ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

                /*Called when ItemTouchHelper wants to move the dragged item from its old position to
                 the new position.*/
                override fun onMove(
                    recyclerView: RecyclerView,
                    dragged: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val draggedPosition = dragged.adapterPosition
                    val targetPosition = target.adapterPosition

                    // TODO (Step 4: Assign the global variable with updated values.)
                    // START
                    if (mPositionDraggedFrom == -1) {
                        mPositionDraggedFrom = draggedPosition
                    }
                    mPositionDraggedTo = targetPosition
                    // END

                    /**
                     * Swaps the elements at the specified positions in the specified list.
                     */

                    /**
                     * Swaps the elements at the specified positions in the specified list.
                     */
                    Collections.swap(list[position].cards, draggedPosition, targetPosition)

                    // move item in `draggedPosition` to `targetPosition` in adapter.
                    adapter.notifyItemMoved(draggedPosition, targetPosition)

                    return false // true if moved, false otherwise
                }

                // Called when a ViewHolder is swiped by the user.
                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int
                ) { // remove from adapter
                }

                // TODO (Step 5: Finally when the dragging is completed than call the function to update the cards in the database and reset the global variables.)
                // START
                /*Called by the ItemTouchHelper when the user interaction with an element is over and it
                 also completed its animation.*/
                override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                    super.clearView(recyclerView, viewHolder)

                    if (mPositionDraggedFrom != -1 && mPositionDraggedTo != -1 && mPositionDraggedFrom != mPositionDraggedTo) {

                        (context as TaskListActivity).updateCardsInTaskList(
                            position,
                            list[position].cards
                        )
                    }

                    // Reset the global variables
                    mPositionDraggedFrom = -1
                    mPositionDraggedTo = -1
                }
                // END
            })

            /*Attaches the ItemTouchHelper to the provided RecyclerView. If TouchHelper is already
            attached to a RecyclerView, it will first detach from the previous one.*/
            helper.attachToRecyclerView(rvCardList)




        }
    }

    private fun alertDialogForDeleteList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        //set title for alert dialog
        builder.setTitle("Alert")
        //set message for alert dialog
        builder.setMessage("Are you sure you want to delete $title.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed

            if (context is TaskListActivity) {
                context.deleteTaskList(position)
            }
        }

        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }

    override fun getItemCount(): Int = list.size

    private fun dpToPx(dp: Int): Int =
        (dp * context.resources.displayMetrics.density).toInt()

    class MyViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)
}
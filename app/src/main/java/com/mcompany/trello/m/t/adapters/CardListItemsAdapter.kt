package com.mcompany.trello.m.t.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mcompany.trello.R
import com.mcompany.trello.databinding.ItemCardBinding
import com.mcompany.trello.databinding.ItemTaskBinding
import com.mcompany.trello.m.t.activities.TaskListActivity
import com.mcompany.trello.m.t.model.Card
import com.mcompany.trello.m.t.model.SelectedMembers

open class CardListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Card>
) : RecyclerView.Adapter<CardListItemsAdapter.MyViewHolder>() {

    private var onClickListener: OnClickListener? = null

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val binding = ItemCardBinding.inflate(LayoutInflater.from(context), parent, false)

        return MyViewHolder(binding)
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: MyViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val model = list[position]

        with(holder.binding){

            if (model.labelColor.isNotEmpty()) {
                viewLabelColor.visibility = View.VISIBLE
                viewLabelColor.setBackgroundColor(Color.parseColor(model.labelColor))
            } else {
                viewLabelColor.visibility = View.GONE
            }


            tvCardName.text = model.name

            if ((context as TaskListActivity).mAssignedMembersDetailList.size > 0) {
                // A instance of selected members list.
                val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

                // Here we got the detail list of members and add it to the selected members list as required.
                for (i in context.mAssignedMembersDetailList.indices) {
                    for (j in model.assignedTo) {
                        if (context.mAssignedMembersDetailList[i].id == j) {
                            val selectedMember = SelectedMembers(
                                context.mAssignedMembersDetailList[i].id,
                                context.mAssignedMembersDetailList[i].image
                            )

                            selectedMembersList.add(selectedMember)
                        }
                    }
                }

                if (selectedMembersList.size > 0) {

                    if (selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy) {
                        rvCardSelectedMembersList.visibility = View.GONE
                    } else {
                        rvCardSelectedMembersList.visibility = View.VISIBLE

                        rvCardSelectedMembersList.layoutManager =
                            GridLayoutManager(context, 4)
                        val adapter = CardMemberListItemsAdapter(context, selectedMembersList, false)
                        rvCardSelectedMembersList.adapter = adapter
                        adapter.setOnClickListener(object :
                            CardMemberListItemsAdapter.OnClickListener {
                            override fun onClick() {
                                if (onClickListener != null) {
                                    onClickListener!!.onClick(position)
                                }
                            }
                        })
                    }
                } else {
                    rvCardSelectedMembersList.visibility = View.GONE
                }
            }

            root.setOnClickListener{
                onClickListener?.onClick(position)
            }


        }
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * A function for OnClickListener where the Interface is the expected parameter..
     */
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    /**
     * An interface for onclick items.
     */
    interface OnClickListener {
        fun onClick(position: Int)
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    class MyViewHolder(val binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root)
}

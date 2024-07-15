package com.mcompany.trello.m.t.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mcompany.trello.R
import com.mcompany.trello.databinding.ItemCardSelectedMemberBinding
import com.mcompany.trello.m.t.model.SelectedMembers
import com.squareup.picasso.Picasso

open class CardMemberListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<SelectedMembers>,
    private val assignMembers: Boolean
) : RecyclerView.Adapter<CardMemberListItemsAdapter.MyViewHolder>() {

    private var onClickListener: OnClickListener? = null



    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemCardSelectedMemberBinding.inflate(LayoutInflater.from(context), parent, false)

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
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]

        with(holder.binding){

            if (position == list.size - 1 && assignMembers) {
                ivAddMember.visibility = View.VISIBLE
                ivSelectedMemberImage.visibility = View.GONE
            } else {
                ivAddMember.visibility = View.GONE
                ivSelectedMemberImage.visibility = View.VISIBLE

                Picasso.get()
                    .load(model.image)
                    .placeholder(R.drawable.ic_user_place_holder)
                    .error(R.drawable.ic_user_place_holder)
                    .into(ivSelectedMemberImage)
            }


            root.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick()
                }
            }


        }
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    /**
     * An interface for onclick items.
     */
    interface OnClickListener {
        fun onClick()
    }


    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    class MyViewHolder(val binding: ItemCardSelectedMemberBinding) : RecyclerView.ViewHolder(binding.root)
}
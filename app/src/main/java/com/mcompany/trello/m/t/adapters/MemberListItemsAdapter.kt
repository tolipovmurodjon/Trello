package com.mcompany.trello.m.t.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mcompany.trello.R
import com.mcompany.trello.databinding.ItemMemberBinding
import com.mcompany.trello.m.t.model.User
import com.mcompany.trello.m.t.utils.Constants
import com.squareup.picasso.Picasso

open class MemberListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<User>
) : RecyclerView.Adapter<MemberListItemsAdapter.MyViewHolder>() {

    private var onClickListener: OnClickListener? = null


    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemMemberBinding.inflate(LayoutInflater.from(context), parent, false)

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

            Picasso.get()
                .load(model.image)
                .placeholder(R.drawable.ic_user_place_holder)
                .error(R.drawable.ic_user_place_holder)
                .into(ivMemberImage);

//            Glide
//                .with(context)
//                .load(model.image)
//                .centerCrop()
//                .placeholder(R.drawable.ic_user_place_holder)
//                .into(ivMemberImage)

            tvMemberName.text = model.name
            tvMemberEmail.text = model.email

            if (model.selected) {
                ivSelectedMember.visibility = View.VISIBLE
            } else {
                ivSelectedMember.visibility = View.GONE
            }


            root.setOnClickListener {

                if (onClickListener != null) {
                    // TODO (Step 3: Pass the constants here according to the selection.)
                    // START
                    if (model.selected) {
                        onClickListener!!.onClick(position, model, Constants.UN_SELECT)
                    } else {
                        onClickListener!!.onClick(position, model, Constants.SELECT)
                    }
                    // END
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


    interface OnClickListener {
        fun onClick(position: Int, user: User, action: String)
    }


    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    class MyViewHolder(val binding: ItemMemberBinding) : RecyclerView.ViewHolder(binding.root)
}
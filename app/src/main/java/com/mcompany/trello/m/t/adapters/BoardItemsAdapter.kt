package com.mcompany.trello.m.t.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.mcompany.trello.R
import com.mcompany.trello.databinding.ItemBoardBinding
import com.mcompany.trello.m.t.model.Board
import com.squareup.picasso.Picasso

open class BoardItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Board>
) : RecyclerView.Adapter<BoardItemsAdapter.MyViewHolder>() {

    private var onClickListener: OnClickListener? = null

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemBoardBinding.inflate(LayoutInflater.from(context), parent, false)
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

        holder.tvName.text = model.name
        holder.tvCreatedBy.text = "Created by ${model.createdBy}"

        if (model.image.isNotEmpty()) {
            Picasso.get()
                .load(model.image)
                .placeholder(R.drawable.ic_user_place_holder)
                .error(R.drawable.ic_user_place_holder)
                .into(holder.ivBoardImage)
        } else {
            // Set a placeholder image if the URL is empty
            holder.ivBoardImage.setImageResource(R.drawable.ic_user_place_holder)
        }

        // Set the click listener on the root view
        holder.itemView.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, model)
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
     * A function for OnClickListener where the Interface is the expected parameter.
     */
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    /**
     * An interface for onClick items.
     */
    interface OnClickListener : AdapterView.OnItemClickListener {
        fun onClick(position: Int, model: Board)
        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            TODO("Not yet implemented")
        }
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    class MyViewHolder(val binding: ItemBoardBinding) : RecyclerView.ViewHolder(binding.root) {
        val ivBoardImage = binding.ivBoardImage
        val tvName = binding.tvName
        val tvCreatedBy = binding.tvCreatedBy
    }
}

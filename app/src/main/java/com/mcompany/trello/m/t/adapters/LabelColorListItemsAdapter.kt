package com.mcompany.trello.m.t.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mcompany.trello.databinding.ItemLabelColorBinding

class LabelColorListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<String>,
    private val mSelectedColor: String
) : RecyclerView.Adapter<LabelColorListItemsAdapter.MyViewHolder>() {

    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemLabelColorBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val color = list[position]

        with(holder.binding) {
            viewMain.setBackgroundColor(Color.parseColor(color))

            if (color == mSelectedColor) {
                ivSelectedColor.visibility = View.VISIBLE
            } else {
                ivSelectedColor.visibility = View.GONE
            }
        }

        holder.itemView.setOnClickListener {
            onItemClickListener?.onClick(position, color)
        }
    }

    interface OnItemClickListener {
        fun onClick(position: Int, color: String)
    }

    class MyViewHolder(val binding: ItemLabelColorBinding) : RecyclerView.ViewHolder(binding.root)
}
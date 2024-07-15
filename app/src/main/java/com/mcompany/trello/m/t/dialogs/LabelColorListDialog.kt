package com.mcompany.trello.m.t.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.mcompany.trello.R
import com.mcompany.trello.databinding.DialogListBinding
import com.mcompany.trello.databinding.ItemLabelColorBinding
import com.mcompany.trello.m.t.adapters.LabelColorListItemsAdapter

abstract class LabelColorListDialog(
    context: Context,
    private var list: ArrayList<String>,
    private val title: String = "",
    private val mSelectedColor: String = ""
) : Dialog(context) {

    private lateinit var binding: DialogListBinding
    private lateinit var adapter: LabelColorListItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        binding.tvTitle.text = title
        binding.rvList.layoutManager = LinearLayoutManager(context)
        adapter = LabelColorListItemsAdapter(context, list, mSelectedColor)
        binding.rvList.adapter = adapter

        adapter.onItemClickListener = object : LabelColorListItemsAdapter.OnItemClickListener {
            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)
            }
        }
    }

    protected abstract fun onItemSelected(color: String)
}
package com.mcompany.trello.m.t.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.mcompany.trello.databinding.DialogListBinding
import com.mcompany.trello.m.t.adapters.LabelColorListItemsAdapter
import com.mcompany.trello.m.t.adapters.MemberListItemsAdapter
import com.mcompany.trello.m.t.model.User

abstract class MembersListDialog(
    context: Context,
    private var list: ArrayList<User>,
    private val title: String = ""
) : Dialog(context) {

    private lateinit var binding: DialogListBinding
    private var adapter: MemberListItemsAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: Bundle())

        binding = DialogListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        binding.tvTitle.text = title

        if (list.size > 0) {

            binding.rvList.layoutManager = LinearLayoutManager(context)
            adapter = MemberListItemsAdapter(context, list)
            binding.rvList.adapter = adapter

            adapter!!.setOnClickListener(object :
                MemberListItemsAdapter.OnClickListener {
                override fun onClick(position: Int, user: User, action:String) {
                    dismiss()
                    onItemSelected(user, action)
                }
            })
        }
    }

    protected abstract fun onItemSelected(user: User, action:String)
}

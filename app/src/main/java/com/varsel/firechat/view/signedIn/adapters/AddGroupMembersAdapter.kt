package com.varsel.firechat.view.signedIn.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.varsel.firechat.R
import com.varsel.firechat.model.User.User

class AddGroupMembersAdapter(val checkChanged: ()-> Unit): RecyclerView.Adapter<AddGroupMembersAdapter.AddMemberViewHolder>() {

    val users: ArrayList<User> = arrayListOf()
    val selected: ArrayList<String> = arrayListOf()

    class AddMemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val parent = itemView.findViewById<LinearLayout>(R.id.parent_clickable)
        val checkbox = itemView.findViewById<CheckBox>(R.id.checkbox)
        val name = itemView.findViewById<TextView>(R.id.name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddMemberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_create_group_item, parent, false)
        return AddMemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddMemberViewHolder, position: Int) {
        val item: User = users[position]

        holder.name.text = item.name

        holder.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                select(item.userUID)
            } else {
                unselect(item.userUID)
            }
            checkChanged()
        }

        // TODO: Set parent click listener
        holder.parent.setOnClickListener {

        }
    }

    private fun select(uid: String){
        selected.add(uid)
    }

    private fun unselect(uid: String){
        selected.remove(uid)
    }

    override fun getItemCount(): Int {
        return users.size
    }
}
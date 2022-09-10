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

// TODO: Change to ListView (optional)
class CreateGroupAdapter(val checkChanged: ()-> Unit): RecyclerView.Adapter<CreateGroupAdapter.CreateGroupViewHolder>() {
    var friends: ArrayList<User?> = arrayListOf<User?>()
    var selected: ArrayList<String?> = arrayListOf()

    class CreateGroupViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val parent = itemView.findViewById<LinearLayout>(R.id.parent_clickable)
        val checkbox = itemView.findViewById<CheckBox>(R.id.checkbox)
        val name = itemView.findViewById<TextView>(R.id.name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreateGroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_create_group_item, parent, false)
        return CreateGroupViewHolder(view)
    }

    // TODO: Fix potential bug where the item is scrolled off in the view
    override fun onBindViewHolder(holder: CreateGroupViewHolder, position: Int) {
        val item: User? = friends[position]
        if (item != null) {
            holder.name.text = item.name
        }

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                item?.userUID?.let { select(it) }
            } else {
                item?.userUID?.let { unselect(it) }
            }

            checkChanged()
        }

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
        return friends.size
    }
}
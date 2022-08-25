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

class CreateGroupAdapter(): RecyclerView.Adapter<CreateGroupAdapter.CreateGroupViewHolder>() {
    var friends: ArrayList<User?> = arrayListOf<User?>()

    class CreateGroupViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val parent = itemView.findViewById<LinearLayout>(R.id.parent_clickable)
        val checkbox = itemView.findViewById<CheckBox>(R.id.checkbox)
        val name = itemView.findViewById<TextView>(R.id.name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreateGroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_create_group_item, parent, false)
        return CreateGroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: CreateGroupViewHolder, position: Int) {
        val item: User? = friends[position]
        if (item != null) {
            holder.name.text = item.name
        }

        holder.checkbox.setOnClickListener {

        }

        holder.parent.setOnClickListener {

        }
    }

    override fun getItemCount(): Int {
        return friends.size
    }
}
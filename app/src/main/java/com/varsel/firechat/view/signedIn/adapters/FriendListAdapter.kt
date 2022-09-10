package com.varsel.firechat.view.signedIn.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.varsel.firechat.R
import com.varsel.firechat.model.User.User

class FriendListAdapter(val parentListener: (id: String)-> Unit): RecyclerView.Adapter<FriendListAdapter.FriendItemViewHolder>() {
    val friends: MutableList<User> = mutableListOf()

    class FriendItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val name = itemView.findViewById<TextView>(R.id.name)
        val parentClickable = itemView.findViewById<LinearLayout>(R.id.parent_clickable)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_friends_list_item, parent, false)

        return FriendItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendItemViewHolder, position: Int) {
        val item: User = friends[position]

        holder.name.text = item.name

        holder.parentClickable.setOnClickListener {
            item.userUID?.let { it1 -> parentListener(it1) }
        }
    }

    override fun getItemCount(): Int {
        return friends.size
    }
}
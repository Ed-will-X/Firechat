package com.varsel.firechat.view.signedIn.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.varsel.firechat.R
import com.varsel.firechat.model.User.User

class FriendRequestsAdapter(val parentListener: (id: String?)-> Unit, val btnListener: (user: User?)-> Unit): RecyclerView.Adapter<FriendRequestsAdapter.FriendRequestViewHolder>(){

    var users = arrayListOf<User>()

    class FriendRequestViewHolder(item: View): RecyclerView.ViewHolder(item){
        val name: TextView = item.findViewById<TextView>(R.id.name_friend_request)
        val accept: Button = item.findViewById<Button>(R.id.btn_accept)
        val parentClickable: LinearLayout = item.findViewById(R.id.parent_clickable)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_friend_request_list_item, parent, false)

        return FriendRequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int) {
        val item: User = users[position]

        holder.name.text = item.name

        holder.parentClickable.setOnClickListener {
            parentListener(item.userUID)
        }

        holder.accept.setOnClickListener {
            btnListener(item)
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }
}
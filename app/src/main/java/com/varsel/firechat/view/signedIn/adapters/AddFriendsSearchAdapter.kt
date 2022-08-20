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

class AddFriendsSearchAdapter(val clickListener: (UID: String?)-> Unit): RecyclerView.Adapter<AddFriendsSearchAdapter.UserItemViewHolder>() {

    var users: ArrayList<User> = arrayListOf()

    class UserItemViewHolder(item: View): RecyclerView.ViewHolder(item){
        val root = item.findViewById<LinearLayout>(R.id.root)
        val profileImage = item.findViewById<ImageView>(R.id.profile_image)
        val name = item.findViewById<TextView>(R.id.name)
        val occupation = item.findViewById<TextView>(R.id.occupation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_add_friends_list_item, parent, false)
        return UserItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserItemViewHolder, position: Int) {
        val item: User = users[position]
        holder.name.setText(item.name)
        // TODO: Set occupation
        holder.occupation.setText(item.name)
        // TODO: Set the image resource if profile pic is present

        holder.root.setOnClickListener {
            clickListener(item.userUID)
        }

    }

    override fun getItemCount(): Int {
        return users.size
    }
}
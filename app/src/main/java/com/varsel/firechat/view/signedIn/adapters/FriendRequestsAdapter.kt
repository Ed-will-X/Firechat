package com.varsel.firechat.view.signedIn.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.varsel.firechat.R
import com.varsel.firechat.model.User.User

// TODO: Change to ListAdapter
class FriendRequestsAdapter(val parentListener: (id: String?)-> Unit, val btnListener: (user: User?)-> Unit): RecyclerView.Adapter<FriendRequestsAdapter.FriendRequestViewHolder>(){

    var users = arrayListOf<User>()

    class FriendRequestViewHolder(item: View): RecyclerView.ViewHolder(item){
        val name: TextView = item.findViewById<TextView>(R.id.name_friend_request)
        val accept: Button = item.findViewById<Button>(R.id.btn_accept)
        val parentClickable: LinearLayout = item.findViewById(R.id.parent_clickable)
        val profileImageParent = itemView.findViewById<MaterialCardView>(R.id.profile_image_parent)
        val profileImage = itemView.findViewById<ImageView>(R.id.profile_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_friend_request_list_item, parent, false)

        return FriendRequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int) {
        val item: User = users[position]

        holder.name.text = item.name
//        ImageUtils.setProfileImage(item.profileImage, holder.profileImageParent, holder.profileImage)


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
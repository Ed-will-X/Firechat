package com.varsel.firechat.view.signedIn.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.varsel.firechat.R
import com.varsel.firechat.model.User.User
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.view.signedIn.SignedinActivity

class FriendListAdapter(
    val activity: SignedinActivity,
    val parentListener: (id: String, user: User, base64: String?)-> Unit
): RecyclerView.Adapter<FriendListAdapter.FriendItemViewHolder>() {
    var friends: MutableList<User> = mutableListOf()

    class FriendItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val name = itemView.findViewById<TextView>(R.id.name)
        val parentClickable = itemView.findViewById<LinearLayout>(R.id.parent_clickable)
        val profileImageParent = itemView.findViewById<MaterialCardView>(R.id.profile_image_parent)
        val profileImage = itemView.findViewById<ImageView>(R.id.profile_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_friends_list_item, parent, false)

        return FriendItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendItemViewHolder, position: Int) {
        val item: User = friends[position]

        holder.name.text = item.name
        ImageUtils.setProfilePicOtherUser(item, holder.profileImage, holder.profileImageParent, activity) { base64 ->
            holder.parentClickable.setOnClickListener {
                parentListener(item.userUID, item, base64)
            }
        }
    }

    override fun getItemCount(): Int {
        return friends.size
    }
}
package com.varsel.firechat.view.signedIn.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.varsel.firechat.R
import com.varsel.firechat.model.User.User
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.view.signedIn.SignedinActivity

class FriendsAdapter(
    val activity: SignedinActivity,
    val parentClickListener: (user: User)-> Unit,
    val chatIconClickListener: (user: User, base64: String?)-> Unit
): ListAdapter<User, FriendsAdapter.FriendItem>(FriendItemDiffCallback()){
    private lateinit var context: Context
    class FriendItem(itemView: View) : RecyclerView.ViewHolder(itemView){
        val name = itemView.findViewById<TextView>(R.id.name_friend)
        val occupation = itemView.findViewById<TextView>(R.id.occupation_friend)
        val parentClickable = itemView.findViewById<LinearLayout>(R.id.parent_clickable_friend)
        val messageIcon = itemView.findViewById<ImageView>(R.id.chat_icon_clickable)
        val nameWithOccupation = itemView.findViewById<LinearLayout>(R.id.name_with_occupation_friend)
        val profileImageParent = itemView.findViewById<MaterialCardView>(R.id.profile_image_parent)
        val profileImage = itemView.findViewById<ImageView>(R.id.profile_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendItem {
        context = parent.context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_friend_item, parent, false)

        return FriendItem(view)
    }

    override fun onBindViewHolder(holder: FriendItem, position: Int) {
        val item = getItem(position)

        holder.name.text = item.name
        ImageUtils.setProfilePicOtherUser(item, holder.profileImage, holder.profileImageParent, activity) { base64 ->
            holder.parentClickable.setOnClickListener {
                parentClickListener(item)
            }

            holder.messageIcon.setOnClickListener {
                chatIconClickListener(item, base64)
            }
        }

        if(item.occupation != null){
            holder.occupation.text = item.occupation
        } else {
            holder.occupation.text = context.getString(R.string.no_occupation)
        }
    }
}

class FriendItemDiffCallback(): DiffUtil.ItemCallback<User>(){
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.userUID == newItem.userUID
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return (oldItem == newItem)
    }

}
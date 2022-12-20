package com.varsel.firechat.presentation.signedIn.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.varsel.firechat.R
import com.varsel.firechat.data.local.ProfileImage.ProfileImageEntity
import com.varsel.firechat.data.local.User.UserEntity
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.presentation.signedIn.SignedinActivity

class FriendListAdapter(
    val activity: SignedinActivity,
    val parentListener: (id: String, user: UserEntity, base64: String?)-> Unit,
    val profileImageClickListener: (profileImage: ProfileImageEntity, user: UserEntity) -> Unit
): RecyclerView.Adapter<FriendListAdapter.FriendItemViewHolder>() {
    var friends: MutableList<UserEntity> = mutableListOf()

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
        val item: UserEntity = friends[position]

        holder.name.text = item.name

        holder.parentClickable.setOnClickListener {
            parentListener(item.userUID, item, null)
        }

        ImageUtils.setProfilePicOtherUser_fullObject(item, holder.profileImage, holder.profileImageParent, activity) { image ->
            holder.parentClickable.setOnClickListener {
                parentListener(item.userUID, item, image?.image)
            }

            holder.profileImage.setOnClickListener {
                if(image != null){
                    profileImageClickListener(image, item)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return friends.size
    }
}
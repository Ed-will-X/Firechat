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

// TODO: Change to ListAdapter
class FriendRequestsAdapter(
    val activity: SignedinActivity,
    val parentListener: (id: String, user: UserEntity, base64: String?)-> Unit,
    val btnListener: (user: UserEntity)-> Unit,
    val imageClickListener: (image: ProfileImageEntity, user: UserEntity) -> Unit
): RecyclerView.Adapter<FriendRequestsAdapter.FriendRequestViewHolder>(){

    var users = mutableListOf<UserEntity>()

    class FriendRequestViewHolder(item: View): RecyclerView.ViewHolder(item){
        val name: TextView = item.findViewById<TextView>(R.id.name_friend_request)
//        val accept: Button = item.findViewById<Button>(R.id.btn_accept)
        val parentClickable: LinearLayout = item.findViewById(R.id.parent_clickable)
        val profileImageParent = itemView.findViewById<MaterialCardView>(R.id.profile_image_parent)
        val profileImage = itemView.findViewById<ImageView>(R.id.profile_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_friend_request_list_item, parent, false)

        return FriendRequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int) {
        val item: UserEntity = users[position]

        holder.name.text = item.name
        holder.parentClickable.setOnClickListener {
            parentListener(item.userUID, item, null)
        }
//        holder.accept.setOnClickListener {
//            btnListener(item)
//        }
        ImageUtils.setProfilePicOtherUser_fullObject(item, holder.profileImage, holder.profileImageParent, activity) { image ->
            if(image != null){
                holder.parentClickable.setOnClickListener {
                    parentListener(item.userUID, item, image?.image)
                }

                holder.profileImage.setOnClickListener {
                    imageClickListener(image, item)
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return users.size
    }
}
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
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.presentation.signedIn.SignedinActivity

class AddFriendsSearchAdapter(
    val activity: SignedinActivity,
    val clickListener: (UID: String, user: User, base64: String?)-> Unit,
    val imageClickListener: (profileImage: ProfileImage, user: User) -> Unit
): RecyclerView.Adapter<AddFriendsSearchAdapter.UserItemViewHolder>() {

    var users: ArrayList<User> = arrayListOf()

    class UserItemViewHolder(item: View): RecyclerView.ViewHolder(item){
        val root = item.findViewById<LinearLayout>(R.id.root)
        val name = item.findViewById<TextView>(R.id.name)
        val occupation = item.findViewById<TextView>(R.id.occupation)
        val profileImageParent = itemView.findViewById<MaterialCardView>(R.id.profile_image_parent)
        val profileImage = itemView.findViewById<ImageView>(R.id.profile_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_add_friends_list_item, parent, false)
        return UserItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserItemViewHolder, position: Int) {
        val item: User = users[position]

        holder.root.setOnClickListener {
            clickListener(item.userUID, item, null)
        }

        holder.name.setText(item.name)
        holder.occupation.setText(item.occupation ?: "")

        ImageUtils.setProfilePicOtherUser_fullObject(item, holder.profileImage, holder.profileImageParent, activity) { profileImage ->
            holder.root.setOnClickListener {
                clickListener(item.userUID, item, profileImage?.image)
            }

            if(profileImage != null){
                holder.profileImage.setOnClickListener {
                    imageClickListener(profileImage, item)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }
}
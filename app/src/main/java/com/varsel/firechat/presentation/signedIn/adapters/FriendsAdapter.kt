package com.varsel.firechat.presentation.signedIn.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.varsel.firechat.R
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.fragments.screen_groups.viewPager.friends.FriendsViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FriendsAdapter(
    val activity: SignedinActivity,
    val viewModel: FriendsViewModel,
    val lifecycleOwner: LifecycleOwner,
    val parentClickListener: (user: User, base64: String?)-> Unit,
    val chatIconClickListener: (user: User, base64: String?)-> Unit,
    val imageClickListener: (profileImage: ProfileImage, user: User) -> Unit
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
        val item: User = getItem(position)

        holder.parentClickable.setOnClickListener {
            parentClickListener(item, null)
        }

        holder.messageIcon.setOnClickListener {
            chatIconClickListener(item, null)
        }

        holder.name.text = item.name

        lifecycleOwner.lifecycleScope.launch {
            viewModel.getOtherUserProfileImageUseCase(item).onEach {
                if(it?.image != null) {
                    viewModel.setProfilePicUseCase(it.image!!, holder.profileImage, holder.profileImageParent, activity)
                    holder.parentClickable.setOnClickListener { _ ->
                        parentClickListener(item, it.image)
                    }

                    holder.messageIcon.setOnClickListener { _ ->
                        chatIconClickListener(item, it.image)
                    }

                    holder.profileImage.setOnClickListener { _ ->
                        imageClickListener(it, item)
                    }
                } else {
                    holder.profileImageParent.visibility = View.GONE
                }
            }.launchIn(this)
        }

//        ImageUtils.setProfilePicOtherUser_fullObject(item, holder.profileImage, holder.profileImageParent, activity) { image ->
//            holder.parentClickable.setOnClickListener {
//                parentClickListener(item, image?.image)
//            }
//
//            holder.messageIcon.setOnClickListener {
//                chatIconClickListener(item, image?.image)
//            }
//
//            if(image != null){
//                holder.profileImage.setOnClickListener {
//                    imageClickListener(image, item)
//                }
//            }
//        }

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
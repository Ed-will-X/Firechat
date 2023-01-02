package com.varsel.firechat.presentation.signedIn.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.varsel.firechat.R
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.viewModel.FriendListFragmentViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FriendListAdapter(
    val activity: SignedinActivity,
    val lifecycleOwner: LifecycleOwner,
    val viewModel: FriendListFragmentViewModel,
    val parentListener: (id: String, user: User, base64: String?)-> Unit,
    val profileImageClickListener: (profileImage: ProfileImage, user: User) -> Unit
): RecyclerView.Adapter<FriendListAdapter.FriendItemViewHolder>() {
    var friends: List<User> = listOf()

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

        holder.parentClickable.setOnClickListener {
            parentListener(item.userUID, item, null)
        }

        lifecycleOwner.lifecycleScope.launch {
            viewModel.getOtherUserProfileImageUseCase(item).onEach {
                if(it?.image != null) {
                    viewModel.setImageUseCase(it.image!!, holder.profileImage, holder.profileImageParent, activity)
                    holder.parentClickable.setOnClickListener { _ ->
                        parentListener(item.userUID, item, it.image)
                    }

                    holder.profileImage.setOnClickListener { _ ->
                        profileImageClickListener(it, item)
                    }
                } else {
                    holder.profileImageParent.visibility = View.GONE
                }
            }.launchIn(this)
        }

//        ImageUtils.setProfilePicOtherUser_fullObject(item, holder.profileImage, holder.profileImageParent, activity) { image ->
//            holder.parentClickable.setOnClickListener {
//                parentListener(item.userUID, item, image?.image)
//            }
//
//            holder.profileImage.setOnClickListener {
//                if(image != null){
//                    profileImageClickListener(image, item)
//                }
//            }
//        }
    }

    override fun getItemCount(): Int {
        return friends.size
    }
}
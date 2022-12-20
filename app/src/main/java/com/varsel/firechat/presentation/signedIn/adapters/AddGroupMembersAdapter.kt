package com.varsel.firechat.presentation.signedIn.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.varsel.firechat.R
import com.varsel.firechat.data.local.ProfileImage.ProfileImageEntity
import com.varsel.firechat.data.local.User.UserEntity
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.presentation.signedIn.SignedinActivity

class AddGroupMembersAdapter(
    val activity: SignedinActivity,
    val checkChanged: ()-> Unit,
    val profileImageListener: (profileImage: ProfileImageEntity, user: UserEntity)-> Unit
): RecyclerView.Adapter<AddGroupMembersAdapter.AddMemberViewHolder>() {

    var users: ArrayList<UserEntity?> = arrayListOf()
    val selected: ArrayList<String> = arrayListOf()

    class AddMemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val parent = itemView.findViewById<LinearLayout>(R.id.parent_clickable)
        val checkbox = itemView.findViewById<CheckBox>(R.id.checkbox)
        val profileImageParent = itemView.findViewById<MaterialCardView>(R.id.profile_image_parent)
        val profileImage = itemView.findViewById<ImageView>(R.id.profile_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddMemberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_create_group_item, parent, false)
        return AddMemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddMemberViewHolder, position: Int) {
        val item = users[position]

        if(item != null){
            holder.checkbox.text = item.name
            ImageUtils.setProfilePicOtherUser_fullObject(item, holder.profileImage, holder.profileImageParent, activity) {
                if(it != null){
                    holder.profileImage.setOnClickListener { it2 ->
                        profileImageListener(it, item)
                    }
                }
            }

            holder.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked){
                    select(item.userUID)
                } else {
                    unselect(item.userUID)
                }
                checkChanged()
            }

            // TODO: Set parent click listener
            holder.parent.setOnClickListener {

            }
        }
    }

    private fun select(uid: String){
        selected.add(uid)
    }

    private fun unselect(uid: String){
        selected.remove(uid)
    }

    override fun getItemCount(): Int {
        return users.size
    }
}
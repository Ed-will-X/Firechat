package com.varsel.firechat.view.signedIn.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.varsel.firechat.R
import com.varsel.firechat.model.ProfileImage.ProfileImage
import com.varsel.firechat.model.User.User
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.view.signedIn.SignedinActivity

class CreateGroupAdapter(
    val activity: SignedinActivity,
    val checkChanged: ()-> Unit,
    val imageClickListener: (profileImage: ProfileImage, user: User) -> Unit
): RecyclerView.Adapter<CreateGroupAdapter.CreateGroupViewHolder>() {
    var friends: ArrayList<User?> = arrayListOf<User?>()
    var selected: ArrayList<String?> = arrayListOf()

    class CreateGroupViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val parent = itemView.findViewById<LinearLayout>(R.id.parent_clickable)
        val checkbox = itemView.findViewById<CheckBox>(R.id.checkbox)
        val profileImageParent = itemView.findViewById<MaterialCardView>(R.id.profile_image_parent)
        val profileImage = itemView.findViewById<ImageView>(R.id.profile_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreateGroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_create_group_item, parent, false)
        return CreateGroupViewHolder(view)
    }

    // TODO: Fix potential bug where the item is scrolled off in the view
        // TODO: Fix by checking if it is in the selected Array each time onBind() is called
    override fun onBindViewHolder(holder: CreateGroupViewHolder, position: Int) {
        val item: User? = friends[position]
        if (item != null) {
            holder.checkbox.text = item.name
            ImageUtils.setProfilePicOtherUser_fullObject(item, holder.profileImage, holder.profileImageParent, activity) { image: ProfileImage? ->
                if(image != null){
                    holder.profileImage.setOnClickListener {
                        imageClickListener(image, item)
                    }
                }
            }
        }

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                item?.userUID?.let { select(it) }
            } else {
                item?.userUID?.let { unselect(it) }
            }

            checkChanged()
        }

        // TODO: Set parent click listener
        holder.parent.setOnClickListener {

        }
    }

    private fun select(uid: String){
        selected.add(uid)
    }

    private fun unselect(uid: String){
        selected.remove(uid)
    }

    override fun getItemCount(): Int {
        return friends.size
    }
}
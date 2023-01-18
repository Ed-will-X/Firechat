package com.varsel.firechat.presentation.signedIn.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.varsel.firechat.R
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.fragments.screens.add_group_members.AddGroupMembersViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AddGroupMembersAdapter(
    val activity: SignedinActivity,
    val lifecycleOwner: LifecycleOwner,
    val viewModel: AddGroupMembersViewModel,
    val checkChanged: ()-> Unit,
    val profileImageListener: (profileImage: ProfileImage, user: User)-> Unit
): RecyclerView.Adapter<AddGroupMembersAdapter.AddMemberViewHolder>() {

    var users = mutableListOf<User>()
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

        holder.checkbox.text = item.name

        lifecycleOwner.lifecycleScope.launch {
            viewModel.getOtherUserProfileImageUseCase(item).onEach {
                if(it?.image != null){
                    viewModel.setProfilePicUseCase(it.image!!, holder.profileImage, holder.profileImageParent, activity)
                    holder.profileImage.setOnClickListener { it2 ->
                        profileImageListener(it, item)
                    }
                } else {
                    holder.profileImageParent.visibility = View.GONE
                }
            }.launchIn(this)
        }

        /*
    *   Checks if the user has been previously selected,
    *   if so, it checks it
    *   else, unchecks it
    * */
        if(isSelected(item.userUID)){
            holder.checkbox.isChecked = true
        } else {
            holder.checkbox.isChecked = false
        }

        holder.checkbox.setOnClickListener {
            if(holder.checkbox.isChecked){
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

    private fun isSelected(userId: String): Boolean{
        for (i in selected){
            if (i == userId){
                return true
            }
        }
        return false
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
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
import com.varsel.firechat.presentation.signedIn.fragments.screens.create_group.CreateGroupViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CreateGroupAdapter(
    val activity: SignedinActivity,
    val lifecycleOwner: LifecycleOwner,
    val viewModel: CreateGroupViewModel,
    val checkChanged: ()-> Unit,
    val imageClickListener: (profileImage: ProfileImage, user: User) -> Unit
): RecyclerView.Adapter<CreateGroupAdapter.CreateGroupViewHolder>() {
    var friends: MutableList<User?> = arrayListOf()
//    var selected: ArrayList<String?> = arrayListOf()

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
        if(item != null){
            holder.checkbox.text = item.name

            lifecycleOwner.lifecycleScope.launch {
                viewModel.getOtherUserProfileImageUseCase(item).onEach {
                    if(it?.image != null){
                        viewModel.setProfilePicUseCase(it.image!!, holder.profileImage, holder.profileImageParent, activity)
                        holder.profileImage.setOnClickListener { _ ->
                            imageClickListener(it, item)
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
                    viewModel.select(item.userUID)
                } else {
                    viewModel.unselect(item.userUID)
                }
                checkChanged()

            }
        }
    }

    private fun isSelected(userId: String): Boolean{
        if(viewModel.selected.value == null) {
            return false
        }
        for (i in viewModel.selected.value!!){
            if (i == userId){
                return true
            }
        }
        return false
    }

    override fun getItemCount(): Int {
        return friends.size
    }
}
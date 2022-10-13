package com.varsel.firechat.view.signedIn.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.varsel.firechat.R
import com.varsel.firechat.model.Chat.GroupRoom
import com.varsel.firechat.model.User.User

class ParticipantsListAdapter(
    val context: Context,
    val firebaseAuth: FirebaseAuth,
    val groupRoom: GroupRoom,
    val pressListener: (userId: String)-> Unit,
    val longPressListener: (userId: String) -> Unit
    )
    : ListAdapter<User, ParticipantsListAdapter.ParticipantViewHolder>(ParticipantAdapterDiffUtilCallback()) {
    class ParticipantViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val name = itemView.findViewById<TextView>(R.id.name)
//        val more = itemView.findViewById<ImageView>(R.id.more)
        val parentClickable = itemView.findViewById<LinearLayout>(R.id.parent_clickable)
        val admin = itemView.findViewById<LinearLayout>(R.id.admin)
        val profileImageParent = itemView.findViewById<MaterialCardView>(R.id.profile_image_parent)
        val profileImage = itemView.findViewById<ImageView>(R.id.profile_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_group_participant_item, parent, false)
        return ParticipantViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        val item = getItem(position)

//        ImageUtils.setProfileImage(item.profileImage, holder.profileImageParent, holder.profileImage)

        if(item.userUID?.let { isAdmin(it) } == true){
            holder.admin.visibility = View.VISIBLE
        } else {
            holder.admin.visibility = View.GONE
        }

        if(item.userUID?.let { isCurrentUser(it) } == true){
            holder.name.text = context.getString(R.string.you)
            holder.parentClickable.setOnLongClickListener {
                longPressListener(item.userUID!!)
                true
            }
        } else {
            holder.name.text = item.name
            holder.parentClickable.setOnClickListener {
                pressListener(item.userUID!!)
            }

            holder.parentClickable.setOnLongClickListener {
                longPressListener(item.userUID!!)
                true
            }
        }
    }

    private fun isAdmin(id: String): Boolean{
        val admins = groupRoom.admins?.values
        if (admins != null) {
            for(i in admins){
                if (id == i){
                    return true
                }
            }
        }
        return false
    }

    private fun isCurrentUser(id: String): Boolean{
        if(id == firebaseAuth.currentUser?.uid.toString()){
            return true
        }

        return false
    }
}

class ParticipantAdapterDiffUtilCallback(): DiffUtil.ItemCallback<User>(){
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean = oldItem.userUID == newItem.userUID

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = oldItem == newItem

}
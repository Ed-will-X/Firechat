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
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.view.signedIn.SignedinActivity

class ParticipantsListAdapter(
    val activity: SignedinActivity,
    val context: Context,
    val firebaseAuth: FirebaseAuth,
    val groupRoom: GroupRoom,
    val pressListener: (userId: String, user: User, base64: String?)-> Unit,
    val longPressListener: (userId: String, user: User, base64: String?) -> Unit
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

        if(isAdmin(item.userUID)){
            holder.admin.visibility = View.VISIBLE
        } else {
            holder.admin.visibility = View.GONE
        }

        if(isCurrentUser(item.userUID)){
            holder.name.text = context.getString(R.string.you)
        } else {
            holder.name.text = item.name
        }

        ImageUtils.setProfilePicOtherUser(item, holder.profileImage, holder.profileImageParent, activity) { base64 ->
            if(isCurrentUser(item.userUID)){
                holder.parentClickable.setOnLongClickListener {
                    longPressListener(item.userUID, item, base64)
                    true
                }
            } else {
                holder.parentClickable.setOnClickListener {
                    pressListener(item.userUID, item, base64)
                }

                holder.parentClickable.setOnLongClickListener {
                    longPressListener(item.userUID, item, base64)
                    true
                }
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
package com.varsel.firechat.view.signedIn.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.varsel.firechat.R
import com.varsel.firechat.model.Chat.ChatRoom
import com.varsel.firechat.model.Chat.GroupRoom
import com.varsel.firechat.model.User.User
import com.varsel.firechat.utils.UserUtils

class GroupChatsListAdapter(val mAuth: FirebaseAuth,val addNewListener: ()-> Unit, val groupItemListener: (id: String)-> Unit): ListAdapter<GroupRoom, RecyclerView.ViewHolder>(GroupChatDiffUtilItemCallback()) {
    private val ADD_NEW = 0
    private val GROUP_CHAT = 1

    class GroupChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val parent = itemView.findViewById<MaterialCardView>(R.id.group_parent)
        val groupName: TextView = itemView.findViewById<TextView>(R.id.group_name)
        val img_card_1 = itemView.findViewById<MaterialCardView>(R.id.gc_image_card_1)
        val img_card_2 = itemView.findViewById<MaterialCardView>(R.id.gc_image_card_2)
        val img_card_3 = itemView.findViewById<MaterialCardView>(R.id.gc_image_card_3)
        val img_card_4 = itemView.findViewById<MaterialCardView>(R.id.gc_image_card_4)
        val img_card_5 = itemView.findViewById<MaterialCardView>(R.id.gc_image_card_5)

    }

    class AddNewViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val parent = itemView.findViewById<MaterialCardView>(R.id.add_new_group_parent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == GROUP_CHAT){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_group_chat_item, parent, false)
            return GroupChatViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_group_chat_add, parent, false)
            return AddNewViewHolder(view)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item: GroupRoom = getItem(position)

        val participants = filterOutCurrentUser(item.participants!!.values.toList())

        if(holder.javaClass == AddNewViewHolder::class.java){
            val viewHolder = holder as AddNewViewHolder

            viewHolder.parent.setOnClickListener {
                addNewListener()
            }

        } else {
            val viewHolder = holder as GroupChatViewHolder
            getParticipantCount(participants, viewHolder)

            viewHolder.groupName.text = item.groupName?.let { UserUtils.truncate(it, 15) }
            viewHolder.parent.setOnClickListener {
                item.roomUID?.let { it1 -> groupItemListener(it1) }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        if(item.roomUID == "ADD_NEW_GROUP_CHAT"){
            return ADD_NEW
        } else {
            return GROUP_CHAT
        }
    }

    private fun filterOutCurrentUser(users: List<String>): List<String>{
        val currentUser = mAuth.currentUser?.uid.toString()
        val otherUsers: MutableList<String>  = mutableListOf()

        for(i in users){
            if(i != currentUser){
                otherUsers.add(i)
            }
        }

        return otherUsers
    }

    // TODO: Implement for only users who have profile pictures
    private fun setUsersProfilePicture(){

    }

    // TODO: Implement get participants
    private fun getParticipants(){

    }

    private fun getParticipantCount(users: List<String>, holder: GroupChatViewHolder){
        if(users.isEmpty()){
            holder.img_card_5.visibility = View.GONE
            holder.img_card_4.visibility = View.GONE
            holder.img_card_3.visibility = View.GONE
            holder.img_card_2.visibility = View.GONE
            holder.img_card_1.visibility = View.GONE
        } else if(users.size == 1){
            holder.img_card_5.visibility = View.GONE
            holder.img_card_4.visibility = View.GONE
            holder.img_card_3.visibility = View.GONE
            holder.img_card_2.visibility = View.GONE
        } else if(users.size == 2){
            holder.img_card_5.visibility = View.GONE
            holder.img_card_4.visibility = View.GONE
            holder.img_card_3.visibility = View.GONE
        } else if(users.size == 3){
            holder.img_card_5.visibility = View.GONE
            holder.img_card_4.visibility = View.GONE
        } else if(users.size == 4){
            holder.img_card_5.visibility = View.GONE
        } else if(users.size > 4){

        }
    }
}

class GroupChatDiffUtilItemCallback(): DiffUtil.ItemCallback<GroupRoom>(){
    override fun areItemsTheSame(oldItem: GroupRoom, newItem: GroupRoom): Boolean = oldItem.roomUID == newItem.roomUID

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: GroupRoom, newItem: GroupRoom): Boolean = oldItem == newItem

}
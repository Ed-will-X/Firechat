package com.varsel.firechat.view.signedIn.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.varsel.firechat.R
import com.varsel.firechat.model.Chat.ChatRoom
import com.varsel.firechat.model.User.User
import com.varsel.firechat.utils.MessageUtils
import com.varsel.firechat.utils.UserUtils
import org.w3c.dom.Text

class ChatListAdapter(val bindListener: (userId: String, holder: ChatItemViewHolder)-> Unit, val mAuth: FirebaseAuth, val parentClickListener: (userId: String, chatRoomId: String)-> Unit, val profileImageClickListener: ()-> Unit): ListAdapter<ChatRoom, ChatListAdapter.ChatItemViewHolder>(DiffUtilItemCallback()) {

    class ChatItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val name = itemView.findViewById<TextView>(R.id.name_chats_list)
        val lastMessage = itemView.findViewById<TextView>(R.id.last_message_chats_list)
        val timestamp = itemView.findViewById<TextView>(R.id.timestamp_chats_list)
        val parent = itemView.findViewById<LinearLayout>(R.id.parent_clickable_chats_list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_individual_chat_item, parent, false)

        return ChatItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatItemViewHolder, position: Int) {
        val item: ChatRoom = getItem(position)
        if(item.participants != null){
            val id = getUserId(item.participants!!)
            item.roomUID?.let { bindListener(id, holder) }
            holder.lastMessage.text = UserUtils.truncate(item.messages?.values?.toList()?.get(0)?.message.toString(), 38)
            holder.timestamp.text = MessageUtils.formatStampChatsPage(item.messages!!.values.toList().get(0).time!!.toString())

            holder.parent.setOnClickListener {
                parentClickListener(id, item.roomUID!!)
            }
        }
    }

    fun getUserId(participants: HashMap<String, String>): String{
        var otherUser = ""
        for (i in participants.values){
            if(i != mAuth.currentUser?.uid.toString()){
                otherUser = i
            }
        }

        return otherUser
    }
}


class DiffUtilItemCallback(): DiffUtil.ItemCallback<ChatRoom>() {
    override fun areItemsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean = oldItem.roomUID == newItem.roomUID

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean = oldItem == newItem
}
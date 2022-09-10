package com.varsel.firechat.view.signedIn.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.varsel.firechat.R
import com.varsel.firechat.model.Chat.ChatRoom
import com.varsel.firechat.model.Chat.GroupRoom
import com.varsel.firechat.model.User.User
import com.varsel.firechat.model.message.Message
import com.varsel.firechat.utils.MessageUtils
import com.varsel.firechat.utils.UserUtils
import com.varsel.firechat.viewModel.FirebaseViewModel

class ChatListAdapter(
    val mAuth: FirebaseAuth,
    val mDbRef: DatabaseReference,
    val firebaseViewModel: FirebaseViewModel,
    val parentClickListener: (userId: String, chatRoomId: String)-> Unit,
    val profileImageClickListener: ()-> Unit,
) : ListAdapter<ChatRoom, ChatListAdapter.ChatItemViewHolder>(ChatsListAdapterDiffItemCallback()) {

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
            if(item != null){
                val id = getUserId(item.participants!!)
                holder.lastMessage.text = UserUtils.truncate(getLastMessage(item), 38)
                if(item.participants != null){
                    getUser(getUserId(item.participants!!)){
                        holder.name.text = it.name
                    }
                }
                if(item.messages != null){
                    holder.timestamp.text = MessageUtils.formatStampChatsPage(getLastMessageTimestamp(item))
                }
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

    private fun getLastMessage(chatRoom: ChatRoom): String {
        val sortedMessages: List<Message>? = MessageUtils.sortMessages(chatRoom)

        return (sortedMessages?.last()?.message ?: "")
    }

    private fun getLastMessageTimestamp(chatRoom: ChatRoom): String {
        val sortedMessages: List<Message>? = MessageUtils.sortMessages(chatRoom)

        return (sortedMessages?.last()?.time.toString() ?: "")
    }

    private fun getUser(id: String, afterCallback: (user: User)-> Unit) {
        lateinit var user: User
        firebaseViewModel.getUserSingle(id, mDbRef, {
            if (it != null) {
                user = it
            }
        }, {
            afterCallback(user)
        })
    }
}

class ChatsListAdapterDiffItemCallback(): DiffUtil.ItemCallback<ChatRoom>(){
    override fun areItemsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean = oldItem.roomUID == newItem.roomUID

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean = oldItem == newItem

}
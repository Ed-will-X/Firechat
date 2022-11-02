package com.varsel.firechat.view.signedIn.adapters

import android.annotation.SuppressLint
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
import com.varsel.firechat.R
import com.varsel.firechat.model.Chat.ChatRoom
import com.varsel.firechat.model.User.User
import com.varsel.firechat.model.Message.Message
import com.varsel.firechat.model.Message.MessageType
import com.varsel.firechat.model.ProfileImage.ProfileImage
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.MessageUtils
import com.varsel.firechat.utils.UserUtils
import com.varsel.firechat.view.signedIn.SignedinActivity

class ChatListAdapter(
    val activity: SignedinActivity,
    val parentClickListener: (userId: String, chatRoomId: String, user: User, base64: String?)-> Unit,
    val profileImageClickListener: (profileImage: ProfileImage, user: User)-> Unit,
) : ListAdapter<ChatRoom, ChatListAdapter.ChatItemViewHolder>(ChatsListAdapterDiffItemCallback()) {

    class ChatItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val name = itemView.findViewById<TextView>(R.id.name_chats_list)
        val lastMessage = itemView.findViewById<TextView>(R.id.last_message_chats_list)
        val timestamp = itemView.findViewById<TextView>(R.id.timestamp_chats_list)
        val parent = itemView.findViewById<LinearLayout>(R.id.parent_clickable_chats_list)
        val profileImageParent = itemView.findViewById<MaterialCardView>(R.id.profile_image_parent)
        val profileImage = itemView.findViewById<ImageView>(R.id.profile_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_individual_chat_item, parent, false)

        return ChatItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatItemViewHolder, position: Int) {
        val item: ChatRoom = getItem(position)


        val id = getUserId(item.participants!!)
        if(getLastMessageObject(item)?.type == MessageType.TEXT){
            holder.lastMessage.text = UserUtils.truncate(getLastMessage(item), 38)
        } else if(getLastMessageObject(item)?.type == MessageType.IMAGE){
            holder.lastMessage.text = activity.getString(R.string.image_with_emoji)
        }

        if(item.participants != null){
            // TODO: Replace with internal database code
            getUser(getUserId(item.participants!!)){ user ->
                holder.parent.setOnClickListener { _ ->
                    parentClickListener(id, item.roomUID, user, null)
                }
                holder.name.text = user.name
                ImageUtils.setProfilePicOtherUser_fullObject(user, holder.profileImage, holder.profileImageParent, activity) { profileImage ->
                    holder.parent.setOnClickListener { _ ->
                        parentClickListener(id, item.roomUID, user, profileImage?.image)
                    }

                    if(profileImage != null){
                        holder.profileImage.setOnClickListener {
                            profileImageClickListener(profileImage, user)
                        }
                    }
                }

            }
        }
        if(item.messages != null){
            holder.timestamp.text = MessageUtils.formatStampChatsPage(getLastMessageTimestamp(item))
        }

    }

    fun getUserId(participants: HashMap<String, String>): String{
        var otherUser = ""
        for (i in participants.values){
            if(i != activity.firebaseAuth.currentUser?.uid.toString()){
                otherUser = i
            }
        }

        return otherUser
    }

    private fun getLastMessage(chatRoom: ChatRoom): String {
        val sortedMessages: List<Message>? = MessageUtils.sortMessages(chatRoom)

        return (sortedMessages?.last()?.message ?: "")
    }

    private fun getLastMessageObject(chatRoom: ChatRoom): Message? {
        val sortedMessages: List<Message>? = MessageUtils.sortMessages(chatRoom)

        return (sortedMessages?.last())
    }

    private fun getLastMessageTimestamp(chatRoom: ChatRoom): String {
        val sortedMessages: List<Message>? = MessageUtils.sortMessages(chatRoom)

        return (sortedMessages?.last()?.time.toString() ?: "")
    }

    // TODO: Show shimmer if the adapter can't account for every username
    private fun getUser(id: String, afterCallback: (user: User)-> Unit) {
        lateinit var user: User
        activity.firebaseViewModel.getUserSingle(id, activity.mDbRef, {
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
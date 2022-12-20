package com.varsel.firechat.presentation.signedIn.adapters

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.varsel.firechat.R
import com.varsel.firechat.data.local.Chat.ChatRoomEntity
import com.varsel.firechat.data.local.User.UserEntity
import com.varsel.firechat.data.local.Message.MessageEntity
import com.varsel.firechat.data.local.Message.MessageType
import com.varsel.firechat.data.local.ProfileImage.ProfileImageEntity
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.MessageUtils
import com.varsel.firechat.utils.UserUtils
import com.varsel.firechat.presentation.signedIn.SignedinActivity

class ChatListAdapter(
    val activity: SignedinActivity,
    val parentClickListener: (userId: String, chatRoomId: String, user: UserEntity, base64: String?)-> Unit,
    val profileImageClickListener: (profileImage: ProfileImageEntity, user: UserEntity)-> Unit,
    val readReceiptChange: (unreadChatRooms: MutableMap<String, ChatRoomEntity>)-> Unit
) : ListAdapter<ChatRoomEntity, ChatListAdapter.ChatItemViewHolder>(ChatsListAdapterDiffItemCallback()) {
    val unreadChatRooms: MutableMap<String, ChatRoomEntity> = mutableMapOf()


    class ChatItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val name = itemView.findViewById<TextView>(R.id.name_chats_list)
        val lastMessage = itemView.findViewById<TextView>(R.id.last_message_chats_list)
        val timestamp = itemView.findViewById<TextView>(R.id.timestamp_chats_list)
        val parent = itemView.findViewById<LinearLayout>(R.id.parent_clickable_chats_list)
        val profileImageParent = itemView.findViewById<MaterialCardView>(R.id.profile_image_parent)
        val profileImage = itemView.findViewById<ImageView>(R.id.profile_image)
        val unreadIndicator = itemView.findViewById<MaterialCardView>(R.id.unread_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_individual_chat_item, parent, false)

        return ChatItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatItemViewHolder, position: Int) {
        val item: ChatRoomEntity = getItem(position)

        val lastMessageObject = MessageUtils.getLastMessageObject(item)
        if(lastMessageObject != null){
            determineReceipts(item, lastMessageObject, {
                // Receipt Callback
                if(activity.isNightMode()){
                    // TODO: Change all color references to dynamic theme attributes
                    holder.lastMessage.setTextColor(ContextCompat.getColor(activity, R.color.white))
                    holder.timestamp.setTextColor(ContextCompat.getColor(activity, R.color.white))
                } else {
                    holder.lastMessage.setTextColor(ContextCompat.getColor(activity, R.color.black))
                    holder.timestamp.setTextColor(ContextCompat.getColor(activity, R.color.black))
                }
                holder.unreadIndicator.visibility = View.VISIBLE
                holder.name.setTypeface(null, Typeface.BOLD)

                readReceiptChange(unreadChatRooms)

            }, {
                // No Receipt Callback
                if(activity.isNightMode()){
                    holder.lastMessage.setTextColor(ContextCompat.getColor(activity, R.color.transparent_grey))
                    holder.timestamp.setTextColor(ContextCompat.getColor(activity, R.color.transparent_grey))
                } else {
                    holder.lastMessage.setTextColor(ContextCompat.getColor(activity, R.color.grey))
                    holder.timestamp.setTextColor(ContextCompat.getColor(activity, R.color.grey))
                }

                holder.unreadIndicator.visibility = View.GONE
                holder.name.setTypeface(null, Typeface.NORMAL)

                readReceiptChange(unreadChatRooms)
            })
        }

        val id = UserUtils.getOtherUserId(item.participants!!, activity)
        if(MessageUtils.getLastMessageObject(item)?.type == MessageType.TEXT){
            holder.lastMessage.text = UserUtils.truncate(MessageUtils.getLastMessage(item), 38)
        } else if(MessageUtils.getLastMessageObject(item)?.type == MessageType.IMAGE){
            holder.lastMessage.text = activity.getString(R.string.image_with_emoji)
        }

        if(item.participants != null){
            // TODO: Replace with internal database code
            getUser(UserUtils.getOtherUserId(item.participants!!, activity)){ user ->
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
            holder.timestamp.text = MessageUtils.formatStampChatsPage(MessageUtils.getLastMessageTimestamp(item))
        }

    }

    // TODO: Modularise
    private fun determineReceipts(item: ChatRoomEntity, lastMessage: MessageEntity, receiptCallback: ()-> Unit, noReceiptCallback: ()-> Unit){
        val receipt = activity.readReceiptViewModel.fetchReceipt("${item.roomUID}:${activity.firebaseAuth.currentUser!!.uid}")

        receipt.observe(activity, Observer {
            if(it == null || it.timestamp < lastMessage.time){
                unreadChatRooms.put(item.roomUID, item)
                receiptCallback()
            } else {
                unreadChatRooms.remove(item.roomUID)
                noReceiptCallback()
            }
        })
    }

    // TODO: Show shimmer if the adapter can't account for every username
    private fun getUser(id: String, afterCallback: (user: UserEntity)-> Unit) {
        lateinit var user: UserEntity
        activity.firebaseViewModel.getUserSingle(id, activity.mDbRef, {
            if (it != null) {
                user = it
            }
        }, {
            afterCallback(user)
        })
    }
}

class ChatsListAdapterDiffItemCallback(): DiffUtil.ItemCallback<ChatRoomEntity>(){
    override fun areItemsTheSame(oldItem: ChatRoomEntity, newItem: ChatRoomEntity): Boolean = oldItem.roomUID == newItem.roomUID

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: ChatRoomEntity, newItem: ChatRoomEntity): Boolean = oldItem == newItem

}
package com.varsel.firechat.presentation.signedIn.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.varsel.firechat.R
import com.varsel.firechat.databinding.RecyclerViewIndividualChatItemBinding
import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.Message.Message
import com.varsel.firechat.data.local.Message.MessageType
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.MessageUtils
import com.varsel.firechat.utils.UserUtils
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.fragments.screens.chat_search.ChatsSearchFragment

// TODO: Implement read receipts here
class ChatSearchAdapter(
    val activity: SignedinActivity,
    val fragment: ChatsSearchFragment,
    val chatRoomClick: (chatRoom: ChatRoom, profileImage: ProfileImage?, user: User)-> Unit,
    val groupRoomClick: (groupRoom: GroupRoom, profileImage: ProfileImage?)-> Unit,
    val imageClickChatRoom: (profileImage: ProfileImage, user: User)-> Unit,
    val imageClickGroup: (profileImage: ProfileImage, groupRoom: GroupRoom)-> Unit
    ): RecyclerView.Adapter<ChatSearchAdapter.ChatSearchItem>() {

    var results = mutableListOf<Any>()

    class ChatSearchItem(binding: RecyclerViewIndividualChatItemBinding) : RecyclerView.ViewHolder(binding.root){
        val image = binding.profileImage
        val imageParent = binding.profileImageParent
        val username = binding.nameChatsList
        val lastMessage = binding.lastMessageChatsList
        val timestamp = binding.timestampChatsList
        val parent = binding.parentClickableChatsList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatSearchItem {
        val binding = RecyclerViewIndividualChatItemBinding.inflate(fragment.layoutInflater, parent, false)
        return ChatSearchItem(binding)
    }

    override fun onBindViewHolder(holder: ChatSearchItem, position: Int) {
        val item = results[position]

        if(item is GroupRoom){
            val groupRoom = item as GroupRoom

            val lastMessage = MessageUtils.getLastMessageObject(groupRoom)

            formatLastMessage(lastMessage, holder)

            holder.username.text = groupRoom.groupName
            holder.lastMessage.text = UserUtils.truncate(MessageUtils.getLastMessage(groupRoom), 50)
            holder.timestamp.text = MessageUtils.formatStampChatsPage(MessageUtils.getLastMessageTimestamp(groupRoom))

            holder.parent.setOnClickListener {
                groupRoomClick(groupRoom, null)
            }

            ImageUtils.setProfilePicGroup_fullObject(groupRoom, holder.image, holder.imageParent, activity) {
                holder.parent.setOnClickListener { it2 ->
                    if(it != null){
                        groupRoomClick(groupRoom, it)
                    } else {
                        groupRoomClick(groupRoom, null)
                    }
                }

                holder.image.setOnClickListener { it2->
                    if(it != null){
                        imageClickGroup(it, groupRoom)
                    }
                }
            }
        }else if(item is ChatRoom){
            val chatRoom = item as ChatRoom
            val otherUserId = UserUtils.getOtherUserId(chatRoom.participants ?: hashMapOf(), activity)
            val lastMessage = MessageUtils.getLastMessageObject(chatRoom)

            formatLastMessage(lastMessage, holder)

            UserUtils.getUser(otherUserId, activity) { user ->
                holder.username.text = user.name
                holder.timestamp.text = MessageUtils.formatStampChatsPage(MessageUtils.getLastMessageTimestamp(chatRoom))

                holder.parent.setOnClickListener { it2->
                    chatRoomClick(chatRoom, null, user)
                }
                ImageUtils.setProfilePicOtherUser_fullObject(user, holder.image, holder.imageParent, activity) {
                    holder.parent.setOnClickListener { it2->
                        if(it != null){
                            chatRoomClick(chatRoom, it, user)
                        } else {
                            chatRoomClick(chatRoom, null, user)
                        }
                    }

                    holder.image.setOnClickListener { it2 ->
                        if(it != null){
                            imageClickChatRoom(it, user)
                        }
                    }
                }
            }

        }
    }

    private fun formatLastMessage(lastMessage: Message?, holder: ChatSearchItem) {
        if(lastMessage?.type == MessageType.IMAGE){
            holder.lastMessage.text = UserUtils.truncate(activity.getString(R.string.image_with_emoji), 50)
        } else if(lastMessage?.sender == "SYSTEM"){
            MessageUtils.formatSystemMessage(lastMessage, activity) {
                holder.lastMessage.text = UserUtils.truncate(it, 50)
            }
        } else if(lastMessage?.type == MessageType.TEXT){
            holder.lastMessage.text = UserUtils.truncate(lastMessage.message, 50)
        }
    }

    override fun getItemCount(): Int {
        return results.size
    }
}
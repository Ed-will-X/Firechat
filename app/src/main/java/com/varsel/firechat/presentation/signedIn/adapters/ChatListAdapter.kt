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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.varsel.firechat.R
import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.data.local.Message.Message
import com.varsel.firechat.data.local.Message.MessageType
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.utils.MessageUtils
import com.varsel.firechat.utils.UserUtils
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.fragments.screen_groups.viewPager.Individual.IndividualViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ChatListAdapter(
    val activity: SignedinActivity,
    val viewModel: IndividualViewModel,
    val lifecycleOwner: LifecycleOwner,
    val parentClickListener: (userId: String, chatRoomId: String, user: User, base64: String?)-> Unit,
    val profileImageClickListener: (profileImage: ProfileImage, user: User)-> Unit,
    val readReceiptChange: (unreadChatRooms: MutableMap<String, ChatRoom>)-> Unit,
) : ListAdapter<ChatRoom, ChatListAdapter.ChatItemViewHolder>(ChatsListAdapterDiffItemCallback()) {
    val unreadChatRooms: MutableMap<String, ChatRoom> = mutableMapOf()


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
        val item: ChatRoom = getItem(position)

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

        getUser(UserUtils.getOtherUserId(item.participants, activity)){ user ->
            holder.parent.setOnClickListener { _ ->
                parentClickListener(id, item.roomUID, user, null)
            }
            holder.name.text = user.name

            lifecycleOwner.lifecycleScope.launch {
                viewModel.getOtherUserProfileImageUseCase(user).onEach {
                    if(it?.image != null) {
                        viewModel.setProfilePicUseCase(it.image!!, holder.profileImage, holder.profileImageParent, activity)
                        holder.parent.setOnClickListener { _ ->
                            parentClickListener(id, item.roomUID, user, it.image)
                        }

                        holder.profileImage.setOnClickListener { _ ->
                            profileImageClickListener(it, user)
                        }
                    } else {
                        holder.profileImageParent.visibility = View.GONE
                    }
                }.launchIn(this)
            }

//            ImageUtils.setProfilePicOtherUser_fullObject(user, holder.profileImage, holder.profileImageParent, activity) { profileImage ->
//                holder.parent.setOnClickListener { _ ->
//                    parentClickListener(id, item.roomUID, user, profileImage?.image)
//                }
//
//                if(profileImage != null){
//                    holder.profileImage.setOnClickListener {
//                        profileImageClickListener(profileImage, user)
//                    }
//                }
//            }

        }

        if(item.messages != null){
            holder.timestamp.text = MessageUtils.formatStampChatsPage(MessageUtils.getLastMessageTimestamp(item))
        }

    }

    // TODO: Modularise
    private fun determineReceipts(item: ChatRoom, lastMessage: Message, receiptCallback: ()-> Unit, noReceiptCallback: ()-> Unit){
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
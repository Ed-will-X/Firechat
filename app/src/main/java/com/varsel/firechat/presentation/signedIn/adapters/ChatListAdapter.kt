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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.varsel.firechat.R
import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.data.local.Message.MessageType
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
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
//    val unreadChatRooms: MutableMap<String, ChatRoom> = mutableMapOf()


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

        val lastMessageObject = viewModel.getLastMessage(item)
        if(lastMessageObject != null && lastMessageObject.sender != "SYSTEM"){
            val isRead = viewModel.hasBeenRead(item, lastMessageObject)

            // TODO: Change all color references to dynamic theme attributes
            if(isRead) {
                setRead(holder)
            } else {
                setUnread(holder)
            }
        }

        val id = viewModel.getOtherUserId(item.participants)
        if(viewModel.getLastMessage(item)?.type == MessageType.TEXT){
            holder.lastMessage.text = viewModel.truncate(viewModel.getLastMessage(item)?.message ?: "", 38)
        } else if(viewModel.getLastMessage(item)?.type == MessageType.IMAGE){
            holder.lastMessage.text = activity.getString(R.string.image_with_emoji)
        }

        getUser(viewModel.getOtherUserId(item.participants)){ user ->
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
        }

        if(item.messages != null){
            holder.timestamp.text = viewModel.formatStampChatsPage(viewModel.getLastMessage(item)?.time.toString())
        }

    }

    // TODO: Show shimmer if the adapter can't account for every username
    private fun getUser(id: String, afterCallback: (user: User)-> Unit) {
        lateinit var user: User
        viewModel.firebaseRepository.getFirebaseInstance().getUserSingle(id, {
            user = it
        }, {
            afterCallback(user)
        })
    }

    private fun setRead(holder: ChatItemViewHolder) {
        if(viewModel.isNightMode(activity)){
            holder.lastMessage.setTextColor(ContextCompat.getColor(activity, R.color.transparent_grey))
            holder.timestamp.setTextColor(ContextCompat.getColor(activity, R.color.transparent_grey))
        } else {
            holder.lastMessage.setTextColor(ContextCompat.getColor(activity, R.color.grey))
            holder.timestamp.setTextColor(ContextCompat.getColor(activity, R.color.grey))
        }

        holder.unreadIndicator.visibility = View.GONE
        holder.name.setTypeface(null, Typeface.NORMAL)
    }

    private fun setUnread(holder: ChatItemViewHolder) {
        if(viewModel.isNightMode(activity)){
            holder.lastMessage.setTextColor(ContextCompat.getColor(activity, R.color.white))
            holder.timestamp.setTextColor(ContextCompat.getColor(activity, R.color.white))
        } else {
            holder.lastMessage.setTextColor(ContextCompat.getColor(activity, R.color.black))
            holder.timestamp.setTextColor(ContextCompat.getColor(activity, R.color.black))
        }
        holder.unreadIndicator.visibility = View.VISIBLE
        holder.name.setTypeface(null, Typeface.BOLD)
    }
}

class ChatsListAdapterDiffItemCallback(): DiffUtil.ItemCallback<ChatRoom>(){
    override fun areItemsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean = oldItem.roomUID == newItem.roomUID

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean = oldItem == newItem

}
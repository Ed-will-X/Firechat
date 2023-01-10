package com.varsel.firechat.presentation.signedIn.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.varsel.firechat.R
import com.varsel.firechat.data.local.Image.Image
import com.varsel.firechat.data.local.Message.Message
import com.varsel.firechat.data.local.Message.MessageStatus
import com.varsel.firechat.data.local.Message.MessageType
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.MessageUtils
import com.varsel.firechat.common._utils.UserUtils
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.fragments.screens.chat_page.ChatPageViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.lang.Exception

class ChatPageType {
    companion object {
        val INDIVIDUAL = 0
        val GROUP = 1
    }
}

class MessageListAdapter(
    val chatRoomId: String,
    val activity: SignedinActivity,
    val fragment: Fragment,
    val context: Context,
    val lifecycleOwner: LifecycleOwner,
    val viewModel: ChatPageViewModel,
    val pageType: Int,
    val imgClickListener: (message: Message, image: Image)-> Unit,
    val onClickListener: (message: Message, messageType: Int, messageStatus: Int)-> Unit,
    val profileImgClickListener: (profileImage: ProfileImage, user: User) -> Unit
    )
    : ListAdapter<Message, RecyclerView.ViewHolder>(MessagesCallback()) {

    class SentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val textParent = itemView.findViewById<LinearLayout>(R.id.text_parent)
        val text = itemView.findViewById<TextView>(R.id.sent_text)
        val timestamp = itemView.findViewById<TextView>(R.id.timestamp)
        val sentImage = itemView.findViewById<ImageView>(R.id.sent_image)
        val sentImageSecond = itemView.findViewById<ImageView>(R.id.sent_image_second)
        val imageViewParent = itemView.findViewById<FrameLayout>(R.id.image_view_parent)
        val imageViewParentSecond = itemView.findViewById<FrameLayout>(R.id.image_view_parent_second)

    }

    class ReceivedViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val textParent = itemView.findViewById<LinearLayout>(R.id.text_parent)
        val text = itemView.findViewById<TextView>(R.id.received_text)
        val profilePicContainer = itemView.findViewById<FrameLayout>(R.id.profile_image_silhouette)
        val profileImageParent = itemView.findViewById<FrameLayout>(R.id.profile_image_parent)
        val profileImage = itemView.findViewById<ImageView>(R.id.profile_image)
        val emptyPadding = itemView.findViewById<View>(R.id.empty_padding)
        val timestamp = itemView.findViewById<TextView>(R.id.timestamp)
        val receivedImage = itemView.findViewById<ImageView>(R.id.received_image)
        val receivedImageSecond = itemView.findViewById<ImageView>(R.id.received_image_second)
        val imageViewParent = itemView.findViewById<FrameLayout>(R.id.image_view_parent)
        val imageViewParentSecond = itemView.findViewById<FrameLayout>(R.id.image_view_parent_second)
    }

    class SystemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val systemText = itemView.findViewById<TextView>(R.id.system_text)
        val systemMessageParent = itemView.findViewById<LinearLayout>(R.id.system_message_parent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == MessageStatus.SENT){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_sent_text, parent, false)
            return SentViewHolder(view)
        } else if(viewType == MessageStatus.SYSTEM){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_system_text, parent, false)
            return SystemViewHolder(view)
        }else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_received_text, parent, false)
            return ReceivedViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item: Message = getItem(position)

        if(holder.javaClass == SentViewHolder::class.java){
            // Sent Holder
            val viewHolder = holder as SentViewHolder

            if(item.type == MessageType.TEXT){
                viewHolder.text.text = item.message
                viewHolder.imageViewParent.visibility = View.GONE
                viewHolder.imageViewParentSecond.visibility = View.GONE
                viewHolder.textParent.visibility = View.VISIBLE
            } else if(item.type == MessageType.IMAGE){
                viewHolder.textParent.visibility = View.GONE
            }

            try {
                val prev: Message? = getItem(position - 1)
                if(prev?.sender.equals(item.sender) && MessageUtils.calculateTimestampDifferenceLess(item.time!!, prev?.time!!)){
                    viewHolder.textParent.background = fragment.activity?.let { ContextCompat.getDrawable(it, R.drawable.bg_current_user_chat_second) }
                    if(item.type == MessageType.IMAGE){
                        handleDownloadOnClick(item, viewHolder.sentImageSecond, holder.imageViewParentSecond, holder.imageViewParent)

                    }
                } else {
                    viewHolder.textParent.background = fragment.activity?.let { ContextCompat.getDrawable(it, R.drawable.bg_current_user_chat) }
                    if(item.type == MessageType.IMAGE){
                        handleDownloadOnClick(item, viewHolder.sentImage, holder.imageViewParent, holder.imageViewParentSecond)
                    }
                }
            } catch(e: Exception){
                viewHolder.textParent.background = fragment.activity?.let { ContextCompat.getDrawable(it, R.drawable.bg_current_user_chat) }
                if(item.type == MessageType.IMAGE){
                    handleDownloadOnClick(item, viewHolder.sentImage, holder.imageViewParent, holder.imageViewParentSecond)

                }
            }

            try {
                val next: Message? = getItem(position + 1)
                if(next?.sender == item.sender && MessageUtils.calculateTimestampDifferenceLess(next?.time!!, item.time!!)){
                    viewHolder.timestamp.visibility = View.GONE
                } else {
                    viewHolder.timestamp.visibility = View.VISIBLE
                    viewHolder.timestamp.text = MessageUtils.formatStampMessage(item.time.toString())
                }
            } catch(e: Exception) {
                viewHolder.timestamp.visibility = View.VISIBLE
                viewHolder.timestamp.text = MessageUtils.formatStampMessage(item.time.toString())
            }

        } else if(holder.javaClass == SystemViewHolder::class.java){
            // system message
            val viewHolder = holder as SystemViewHolder

            MessageUtils.formatSystemMessage(item, activity) {
                viewHolder.systemText.text = it
            }

            viewHolder.systemMessageParent.setOnClickListener {
                onClickListener(item, MessageType.TEXT, MessageStatus.SYSTEM)
            }

        }else {
            // RECEIVED VIEW HOLDER
            val viewHolder = holder as ReceivedViewHolder

            if(item.type == MessageType.TEXT){
                viewHolder.text.text = item.message
                viewHolder.imageViewParent.visibility = View.GONE
                viewHolder.imageViewParentSecond.visibility = View.GONE
                viewHolder.textParent.visibility = View.VISIBLE
            } else if(item.type == MessageType.IMAGE){
                viewHolder.textParent.visibility = View.GONE
            }

            try {
                val next: Message? = getItem(position + 1)
                if(next?.sender.equals(item.sender)){
                    viewHolder.textParent.background = fragment.activity?.let { ContextCompat.getDrawable(it, R.drawable.bg_other_user_chat) }
                    viewHolder.timestamp.visibility = View.GONE
                    if(item.type == MessageType.IMAGE){
                        handleDownloadOnClick(item, viewHolder.receivedImage, viewHolder.imageViewParent, viewHolder.imageViewParentSecond)
                    }
                } else {
                    viewHolder.textParent.background = fragment.activity?.let { ContextCompat.getDrawable(it, R.drawable.bg_other_user_chat_second) }
                    viewHolder.timestamp.visibility = View.VISIBLE

                    if(item.type == MessageType.IMAGE){
                        handleDownloadOnClick(item, viewHolder.receivedImageSecond, viewHolder.imageViewParentSecond, viewHolder.imageViewParent)

                    }
                    setOtherUserTimestamp(viewHolder, item)
                }
            } catch (e: Exception) {
                viewHolder.textParent.background = fragment.activity?.let { ContextCompat.getDrawable(it, R.drawable.bg_other_user_chat_second) }
                viewHolder.timestamp.visibility = View.VISIBLE

                if(item.type == MessageType.IMAGE){
                    handleDownloadOnClick(item, viewHolder.receivedImageSecond, viewHolder.imageViewParentSecond, viewHolder.imageViewParent)
                }
                setOtherUserTimestamp(viewHolder, item)
            }


            try {
                val prev: Message? = getItem(position - 1)
                if(prev?.sender.equals(item.sender)){
                    viewHolder.profilePicContainer.visibility = View.GONE
                    viewHolder.emptyPadding.visibility = View.VISIBLE
                } else {
                    UserUtils.getUser(item.sender, activity){ user ->
                        lifecycleOwner.lifecycleScope.launch {
                            viewModel.getOtherUserProfileImageUseCase(user).onEach {
                                if(it?.image != null) {
                                    viewModel.setProfilePicUseCase(it.image!!, holder.profileImage, holder.profileImageParent, activity)
                                    holder.profileImage.setOnClickListener { _ ->
                                        profileImgClickListener(it, user)
                                    }
                                } else {
                                    holder.imageViewParent.visibility = View.GONE
                                }
                            }.launchIn(this)
                        }
//                        ImageUtils.setProfilePicOtherUser_fullObject(user, holder.profileImage, holder.profileImageParent, activity) { profileImage ->
//                            holder.profileImage.setOnClickListener {
//                                if (profileImage != null){
//                                    profileImgClickListener(profileImage, user)
//                                }
//                            }
//                        }
                    }
                    viewHolder.profilePicContainer.visibility = View.VISIBLE
                    viewHolder.emptyPadding.visibility = View.GONE
                }
            } catch (e: Exception) {
                UserUtils.getUser(item.sender, activity){ user ->

                    lifecycleOwner.lifecycleScope.launch {
                        viewModel.getOtherUserProfileImageUseCase(user).onEach {
                            if(it?.image != null) {
                                viewModel.setProfilePicUseCase(it.image!!, holder.profileImage, holder.profileImageParent, activity)
                                holder.profileImage.setOnClickListener { _ ->
                                    profileImgClickListener(it, user)
                                }
                            } else {
                                holder.imageViewParent.visibility = View.GONE
                            }
                        }.launchIn(this)
                    }
//                    ImageUtils.setProfilePicOtherUser_fullObject(user, holder.profileImage, holder.profileImageParent, activity) { profileImage ->
//                        holder.profileImage.setOnClickListener {
//                            if (profileImage != null){
//                                profileImgClickListener(profileImage, user)
//                            }
//                        }
//                    }
                }
                viewHolder.profilePicContainer.visibility = View.VISIBLE
                viewHolder.emptyPadding.visibility = View.GONE
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item: Message = getItem(position)
        if(item.sender.toString() == activity.firebaseAuth.currentUser?.uid.toString()){
            return MessageStatus.SENT
        } else if(item.sender == "SYSTEM"){
            return MessageStatus.SYSTEM
        }else {
            return MessageStatus.RECEIVED
        }
    }

    private fun handleDownloadOnClick(
        item: Message,
        imageView: ImageView,
        imageViewParent: FrameLayout,
        image_view_parent_to_hide: FrameLayout,
    ){
        var has_been_clicked = false

        imageViewParent.visibility = View.VISIBLE
        image_view_parent_to_hide.visibility = View.GONE

        // check if the img is present in the database
        ImageUtils.check_if_chat_image_in_db(item, activity) {
            if(it != null){
                // if: bind it directly (the same way it was before)
                ImageUtils.setChatImage(it.image, imageView, imageViewParent, activity)
                imageView.setOnClickListener { it2 ->
                    imgClickListener(item, it)
                }

            } else {
                // else set the parent click listener
                imageViewParent.setOnClickListener {
                    if(!has_been_clicked){
                        ImageUtils.getAndSetChatImage_fullObject(item, chatRoomId, imageView, imageViewParent, activity) { image ->
                            imageView.setOnClickListener {
                                imgClickListener(item, image)
                            }
                        }

                        // Disable btn
                        imageViewParent.isEnabled = false
                        // set has been clicked to true
                        has_been_clicked = true
                    } else {
                        imageViewParent.isEnabled = false
                    }
                }
            }
        }

    }

    private fun setOtherUserTimestamp(viewHolder: ReceivedViewHolder, item: Message){
        if(pageType == ChatPageType.INDIVIDUAL){
            viewHolder.timestamp.text = MessageUtils.formatStampMessage(item.time.toString())
        } else {
            UserUtils.getUser(item.sender, activity) {
                viewHolder.timestamp.text = "${MessageUtils.formatStampMessage(item.time.toString())} · ${it.name}"
            }

        }
    }
}

class MessagesCallback(): DiffUtil.ItemCallback<Message>(){
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean  = oldItem.messageUID == newItem.messageUID

    // TODO: Fix potential bug
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean = oldItem == newItem
}
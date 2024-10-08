package com.varsel.firechat.presentation.signedIn.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.util.TypedValue
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.varsel.firechat.R
import com.varsel.firechat.common.Resource
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Image.Image
import com.varsel.firechat.data.local.Message.Message
import com.varsel.firechat.data.local.Message.MessageStatus
import com.varsel.firechat.data.local.Message.MessageType
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.use_case._util.InfobarColors
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.fragments.screens.chat_page.ChatPageViewModel
import com.varsel.firechat.presentation.signedIn.fragments.screens.group_chat_page.GroupChatPageViewModel
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


// TODO: Remove excess long click listeners
class MessageListAdapter(
    val chatRoomId: String,
    val activity: SignedinActivity,
    val fragment: Fragment,
    val context: Context,
    val lifecycleOwner: LifecycleOwner,
    val chatsViewModel: ChatPageViewModel,
    val groupViewModel: GroupChatPageViewModel?,
    val pageType: Int,
    val imgClickListener: (message: Message, image: Image)-> Unit,
    val onClickListener: (message: Message, messageType: Int, messageStatus: Int)-> Unit,
    val profileImgClickListener: (profileImage: ProfileImage, user: User) -> Unit,
    val messageLongPress: (message: Message, user: User?) -> Unit
    ) : ListAdapter<Message, RecyclerView.ViewHolder>(MessagesCallback()) {
//    val groupViewModel = this.viewModel as GroupChatPageViewModel
    val blacklisted = mutableListOf<String>()

    class SentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val textParent = itemView.findViewById<LinearLayout>(R.id.text_parent)
        val text = itemView.findViewById<TextView>(R.id.sent_text)
        val timestamp = itemView.findViewById<TextView>(R.id.timestamp)
        val sentImage = itemView.findViewById<ImageView>(R.id.sent_image)
        val sentImageSecond = itemView.findViewById<ImageView>(R.id.sent_image_second)
        val imageViewParent = itemView.findViewById<FrameLayout>(R.id.image_view_parent)
        val imageViewParentSecond = itemView.findViewById<FrameLayout>(R.id.image_view_parent_second)
        val messageParent = itemView.findViewById<LinearLayout>(R.id.message_parent)
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
        val messageParent = itemView.findViewById<LinearLayout>(R.id.message_parent)
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



            storeReceipt(item)



            // TODO: Implement delete functionality
            if(item.deletedBySender == false) {
                Log.d("BALLS", "Deleted by sender is false: ${item.message}")
//                    unsetDeletedText(viewHolder.text, R.color.white)

                if(item.type == MessageType.TEXT){
                    viewHolder.text.text = item.message
                    viewHolder.imageViewParent.visibility = View.GONE
                    viewHolder.imageViewParentSecond.visibility = View.GONE
                    viewHolder.textParent.visibility = View.VISIBLE

                } else if(item.type == MessageType.IMAGE){
                    viewHolder.textParent.visibility = View.GONE

                    viewHolder.imageViewParent.visibility = View.VISIBLE
                    viewHolder.imageViewParentSecond.visibility = View.VISIBLE
                }
            } else {
                Log.d("BALLS", "Deleted by sender is true: ${item.message}")

                setDeletedText(viewHolder.text, R.string.you_deleted_this_message, R.color.transparent_grey)
            }

            // Long click listener
            holder.textParent.setOnLongClickListener {
                handleLongClick_Received(item, null)
                true
            }

            try {
                val prev: Message? = getItem(position - 1)
                if(prev?.sender.equals(item.sender) && this.chatsViewModel.calculateTimestampDifferenceLess(item.time!!, prev?.time!!) && prev.deletedBy.keys.contains(chatsViewModel.getCurrentUserId()) == true) {
                    viewHolder.textParent.background = fragment.activity?.let { ContextCompat.getDrawable(it, R.drawable.bg_current_user_chat) }
                    if(item.type == MessageType.IMAGE){
                        handleDownloadOnClick(item, viewHolder.sentImage, holder.imageViewParent, holder.imageViewParentSecond)
                    }
                } else if(prev?.sender.equals(item.sender) && this.chatsViewModel.calculateTimestampDifferenceLess(item.time!!, prev?.time!!)){
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
                if(next?.deletedBy?.keys?.contains(chatsViewModel.getCurrentUserId()) == true){
                    viewHolder.timestamp.visibility = View.VISIBLE
                    viewHolder.timestamp.text = this.chatsViewModel.formatStampMessage(item.time.toString())
                } else if(next?.sender == item.sender && this.chatsViewModel.calculateTimestampDifferenceLess(next.time, item.time!!)){
                    viewHolder.timestamp.visibility = View.GONE
                } else {
                    viewHolder.timestamp.visibility = View.VISIBLE
                    viewHolder.timestamp.text = this.chatsViewModel.formatStampMessage(item.time.toString())
                }
            } catch(e: Exception) {
                viewHolder.timestamp.visibility = View.VISIBLE
                viewHolder.timestamp.text = this.chatsViewModel.formatStampMessage(item.time.toString())
            }

            ///////////// Handles Deleted by current
            if(item.deletedBy.keys.contains(chatsViewModel.getCurrentUserId())) {
                holder.messageParent.visibility = View.GONE
//                holder.messageParent.removeAllViews()
            } else {
                holder.messageParent.visibility = View.VISIBLE
            }

        } else if(holder.javaClass == SystemViewHolder::class.java){
            // system message
            val viewHolder = holder as SystemViewHolder

            this.chatsViewModel.formatSystemMessage(item, activity) {
                viewHolder.systemText.text = it
            }

            viewHolder.systemMessageParent.setOnClickListener {
                onClickListener(item, MessageType.TEXT, MessageStatus.SYSTEM)
            }

        }else {
            // RECEIVED VIEW HOLDER
            val viewHolder = holder as ReceivedViewHolder

            if(item.deletedBy.keys.contains(chatsViewModel.getCurrentUserId())) {
                holder.messageParent.visibility = View.GONE
//                holder.messageParent.removeAllViews()
            } else {
                holder.messageParent.visibility = View.VISIBLE
            }

            storeReceipt(item)

            if(item.type == MessageType.TEXT){
                // TODO: Implement delete functionality
                if(item.deletedBySender == false) {
                    viewHolder.text.text = item.message
                    viewHolder.imageViewParent.visibility = View.GONE
                    viewHolder.imageViewParentSecond.visibility = View.GONE
                    viewHolder.textParent.visibility = View.VISIBLE
                } else {
                    setDeletedText(viewHolder.text, R.string.this_message_was_deleted_by_user, R.color.transparent_grey)
                }
                // Long click listener
                holder.textParent.setOnLongClickListener {
                    handleLongClick_Received(item, null)
                    true
                }
            } else if(item.type == MessageType.IMAGE){
                viewHolder.textParent.visibility = View.GONE
                viewHolder.imageViewParent.visibility = View.VISIBLE
                viewHolder.imageViewParentSecond.visibility = View.VISIBLE
            }

            try {
                val next: Message? = getItem(position + 1)
                if(next?.sender.equals(item.sender) && next?.deletedBy?.keys?.contains(chatsViewModel.getCurrentUserId()) == false){
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
                if(prev?.sender.equals(item.sender) && prev?.deletedBy?.keys?.contains(chatsViewModel.getCurrentUserId()) == true) {
                    setOtherProfileImageChat(item, holder)
                } else if(prev?.sender.equals(item.sender)){
                    holder.profilePicContainer.visibility = View.GONE
                    holder.emptyPadding.visibility = View.VISIBLE
                } else {
                    setOtherProfileImageChat(item, holder)
                }
            } catch (e: Exception) {
                // Applies where the message is the first and has no previous one
                setOtherProfileImageChat(item, holder)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item: Message = getItem(position)
        if(item.sender.toString() == activity.firebaseAuth.currentUser?.uid.toString()){
            return MessageStatus.SENT
        } else if(item.sender == "SYSTEM"){
            return MessageStatus.SYSTEM
        } else {
            return MessageStatus.RECEIVED
        }
    }

    private fun setOtherProfileImageChat(item: Message, holder: ReceivedViewHolder) {
        this.chatsViewModel.firebase.getFirebaseInstance().getUserSingle(item.sender, { user ->
            lifecycleOwner.lifecycleScope.launch {
                // Get Other user profile image
                this@MessageListAdapter.chatsViewModel.getOtherUserProfileImageUseCase(user).onEach {
                    if(it?.image != null) {
                        this@MessageListAdapter.chatsViewModel.setProfilePicUseCase(it.image!!, holder.profileImage, holder.profileImageParent, activity)
                        holder.profileImage.setOnClickListener { _ ->
                            profileImgClickListener(it, user)
                        }
                    } else {
                        holder.imageViewParent.visibility = View.GONE
                    }
                }.launchIn(this)
            }
        })
        holder.profilePicContainer.visibility = View.VISIBLE
        holder.emptyPadding.visibility = View.GONE
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

        lifecycleOwner.lifecycleScope.launch {
            // check if the img is present in the database
            this@MessageListAdapter.chatsViewModel.checkChatImageInDb(item.message).onEach {
                when(it) {
                    is Resource.Success -> {
                        // TODO: Refactor entire success, loading and error clauses into different functions
                        if(it.data?.image != null) {
                            // if: bind it directly (the same way it was before)
                            this@MessageListAdapter.chatsViewModel.setChatImageUseCase(it.data.image!!, imageView, imageViewParent, activity)
                            imageView.setOnClickListener { it2 ->
                                imgClickListener(item, it.data)
                            }

                            imageView.setOnLongClickListener {
                                handleLongClick_Received(item, null)
                                true
                            }
                        }
                    }
                    is Resource.Loading -> {
                        // TODO: Show loading indicator
                    }
                    is Resource.Error -> {
                        imageViewParent.setOnClickListener {
                            if(!has_been_clicked){
                                lifecycleOwner.lifecycleScope.launch {
                                    this@MessageListAdapter.chatsViewModel.getChatImageUseCase(item.message, chatRoomId).onEach {
                                        when(it) {
                                            is Resource.Success -> {
                                                if(it.data != null) {
                                                    activity.infobarController.showBottomInfobar(activity.getString(R.string.downloaded_chat_image), InfobarColors.UPLOADING)

                                                    this@MessageListAdapter.chatsViewModel.setChatImageUseCase(it.data.image!!, imageView, imageViewParent, activity)
                                                    imageView.setOnClickListener { it2 ->
                                                        imgClickListener(item, it.data)
                                                    }

                                                    imageView.setOnLongClickListener {
                                                        handleLongClick_Received(item, null)
                                                        true
                                                    }
                                                }
                                            }
                                            is Resource.Loading -> {
                                                // TODO: Show loading indicator
                                                    // TODO: Hide Load
                                                    // TODO: Show a loading spinner
                                                activity.infobarController.showBottomInfobar(activity.getString(R.string.downloading_chat_image), InfobarColors.UPLOADING)
                                            }
                                            is Resource.Error -> {
                                                // TODO: Show Error blah blah blah
                                                activity.infobarController.showBottomInfobar(activity.getString(R.string.chat_image_download_failed), InfobarColors.FAILURE)
                                            }
                                        }
                                    }.launchIn(this)
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
            }.launchIn(this)
        }
    }

    private fun setOtherUserTimestamp(viewHolder: ReceivedViewHolder, item: Message){
        if(pageType == ChatPageType.INDIVIDUAL){
            viewHolder.timestamp.text = this.chatsViewModel.formatStampMessage(item.time.toString())
        } else {
            this.chatsViewModel.firebase.getFirebaseInstance().getUserSingle(item.sender, {
                viewHolder.timestamp.text = "${this.chatsViewModel.formatStampMessage(item.time.toString())} · ${it.name}"
            })

        }
    }

    private fun storeReceipt(message: Message) {
        if(blacklisted.contains(message.messageUID)) {
            return
        }

        if(pageType == ChatPageType.INDIVIDUAL) {
            chatsViewModel.storeReceipt(message, chatsViewModel.state.value?.selectedChatRoom!!).onEach {
                when(it) {
                    is Response.Success -> {
                        blacklisted.add(message.messageUID)
                    }
                    is Response.Loading -> {

                    }
                    is Response.Fail -> {
                        blacklisted.add(message.messageUID)
                    }
                }
            }.launchIn(fragment.lifecycleScope)

        } else if(pageType == ChatPageType.GROUP) {
            if(groupViewModel == null) {
                return
            }

            groupViewModel.storeReceipt(message, groupViewModel.state.value?.selectedRoom!!).onEach {
                when(it) {
                    is Response.Success -> {
                        blacklisted.add(message.messageUID)
                    }
                    is Response.Loading -> {

                    }
                    is Response.Fail -> {
                        blacklisted.add(message.messageUID)
                    }
                }
            }.launchIn(fragment.lifecycleScope)
        }
    }

    private fun handleLongClick_Received(message: Message, user: User?) {
        messageLongPress(message, user)
    }

    private fun setDeletedText(
        textView: TextView,
        stringResource: Int = R.string.this_message_was_deleted_by_user,
        colorResource: Int = R.color.transparent_grey
    ) {
        textView.setTypeface(null, Typeface.ITALIC)
        textView.setTextColor(colorResource)

        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, activity.resources.getDimension(R.dimen.deleted_text_size))

        textView.setText((activity.getString(stringResource)))
    }

    private fun unsetDeletedText(
        textView: TextView,
        colorResource: Int = R.color.white
    ) {
        textView.setTypeface(null, Typeface.NORMAL)
        textView.setTextColor(colorResource)

        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, activity.resources.getDimension(R.dimen.deleted_text_size))
    }
}

class MessagesCallback(): DiffUtil.ItemCallback<Message>(){
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean  = oldItem.messageUID == newItem.messageUID

    // TODO: Fix potential bug
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean = oldItem == newItem
}
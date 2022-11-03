package com.varsel.firechat.view.signedIn.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.varsel.firechat.R
import com.varsel.firechat.model.Image.Image
import com.varsel.firechat.model.Message.Message
import com.varsel.firechat.model.Message.MessageStatus
import com.varsel.firechat.model.Message.MessageType
import com.varsel.firechat.model.Message.SystemMessageType
import com.varsel.firechat.model.ProfileImage.ProfileImage
import com.varsel.firechat.model.User.User
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.MessageUtils
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.viewModel.FirebaseViewModel
import java.lang.Exception

class ChatPageType {
    companion object {
        val INDIVIDUAL = 0
        val GROUP = 1
    }
}

class MessageListAdapter(
    val activity: SignedinActivity,
    val fragment: Fragment,
    val context: Context,
    val pageType: Int,
    val firebaseViewModel: FirebaseViewModel,
    val imgClickListener: (message: Message, image: Image)-> Unit,
    val onLongClickListener: (message: Message, messageType: Int, messageStatus: Int)-> Unit,
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

            formatSystemMessage(item, item.time) {
                viewHolder.systemText.text = it
            }

            viewHolder.systemMessageParent.setOnLongClickListener {
                onLongClickListener(item, MessageType.TEXT, MessageStatus.SYSTEM)
                true
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
                    getUser(item.sender){ user ->
                        ImageUtils.setProfilePicOtherUser_fullObject(user, holder.profileImage, holder.profileImageParent, activity) { profileImage ->
                            holder.profileImage.setOnClickListener {
                                if (profileImage != null){
                                    profileImgClickListener(profileImage, user)
                                }
                            }
                        }
                    }
                    viewHolder.profilePicContainer.visibility = View.VISIBLE
                    viewHolder.emptyPadding.visibility = View.GONE
                }
            } catch (e: Exception) {
                getUser(item.sender){ user ->
                    ImageUtils.setProfilePicOtherUser_fullObject(user, holder.profileImage, holder.profileImageParent, activity) { profileImage ->
                        holder.profileImage.setOnClickListener {
                            if (profileImage != null){
                                profileImgClickListener(profileImage, user)
                            }
                        }
                    }
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
                        Log.d("LLL", "download button clicked")
                        ImageUtils.getAndSetChatImage_fullObject(item, imageView, imageViewParent, activity) { image ->
                            Log.d("LLL", "About to set image")
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

    private fun getUser(userId: String, callback: (user: User)-> Unit){
        firebaseViewModel.getUserSingle(userId, activity.mDbRef, {
            if(it != null){
                callback(it)
            }
        },{})
    }

    private fun formatSystemMessage(message: Message, time: Long, afterCallback: (message: String)-> Unit){
        val currentUser = activity.firebaseAuth.currentUser!!.uid
        if(message.messageUID == SystemMessageType.GROUP_REMOVE){
            val messageArr: Array<String> = message.message.split(" ").toTypedArray()

            getUser(messageArr[0]){ remover ->
                getUser(messageArr[1]) { removed ->
//                    afterCallback("${if (remover.userUID == currentUser) "You" else "${remover.name}"} removed ${if(removed.userUID == currentUser) "You" else "${removed.name}"}")
                    afterCallback(context.getString(R.string.group_removed, if (remover.userUID == currentUser) "You" else "${remover.name}", if(removed.userUID == currentUser) "You" else "${removed.name}"))
                }
            }
            // TODO: Add lone return
        }

        if(message.messageUID == SystemMessageType.GROUP_ADD){
            val users: Array<String> = message.message.split(" ").toTypedArray()
            getUser(users[0]) {
                if (users.size < 3) {
                    formatPerson(it.userUID, {
                        afterCallback(context.getString(R.string.group_add_second_person_singular))
                    }, {
                        afterCallback(context.getString(R.string.group_add_third_person_singular, it.name))
                    })
                } else {
                    formatPerson(it.userUID, {
                        afterCallback(context.getString(R.string.group_add_second_person, users.size -1))
                    }, {
                        afterCallback(context.getString(R.string.group_add_third_person, it.name, users.size -1))
                    })
                }
            }
            // TODO: Add lone return statement
        }

        getUser(message.message) {
            if(message.messageUID == SystemMessageType.GROUP_CREATE){
                formatPerson(it.userUID, {
                    afterCallback(context.getString(R.string.group_create_second_person, MessageUtils.formatStampChatsPage(time.toString())))
                }, {
                    afterCallback(context.getString(R.string.group_create_third_person, it.name, MessageUtils.formatStampChatsPage(time.toString())))
                })
            }
            else if(message.messageUID == SystemMessageType.NOW_ADMIN){
                formatPerson(it.userUID, {
                    afterCallback(context.getString(R.string.now_admin_second_person))
                },{
                    afterCallback(context.getString(R.string.now_admin_third_person, it.name))
                })
            }
            else if(message.messageUID == SystemMessageType.NOT_ADMIN){
                formatPerson(it.userUID, {
                    afterCallback(context.getString(R.string.not_admin_second_person))
                },{
                    afterCallback(context.getString(R.string.not_admin_third_person, it.name))
                })
            }
            else if(message.messageUID == SystemMessageType.GROUP_EXIT){
                formatPerson(it.userUID, {
                    afterCallback(context.getString(R.string.group_exit_second_person))
                }, {
                    afterCallback(context.getString(R.string.group_exit_third_person, it.name))
                })
            }
        }
    }

    private fun formatPerson(userId: String, secondPersonCallback: ()-> Unit, thirdPersonCallback: ()-> Unit){
        if(userId == activity.firebaseAuth.currentUser!!.uid){
            secondPersonCallback()
        } else {
            thirdPersonCallback()
        }
    }

    private fun setOtherUserTimestamp(viewHolder: ReceivedViewHolder, item: Message){
        if(pageType == ChatPageType.INDIVIDUAL){
            viewHolder.timestamp.text = MessageUtils.formatStampMessage(item.time.toString())
        } else {
            getUser(item.sender) {
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
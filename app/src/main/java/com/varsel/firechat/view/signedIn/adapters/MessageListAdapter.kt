package com.varsel.firechat.view.signedIn.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.varsel.firechat.R
import com.varsel.firechat.model.Message.Message
import com.varsel.firechat.model.Message.SystemMessageType
import com.varsel.firechat.model.User.User
import com.varsel.firechat.utils.MessageUtils
import com.varsel.firechat.viewModel.FirebaseViewModel
import java.lang.Exception

class ChatPageType {
    companion object {
        val INDIVIDUAL = 0
        val GROUP = 1
    }
}

class MessageType {
    companion object {
        val SENT = 0
        val RECEIVED = 1
        val SYSTEM = 2
    }
}
class MessageListAdapter(
    val mAuth: FirebaseAuth,
    val mDbRef: DatabaseReference,
    val fragment: Fragment,
    val pageType: Int,
    val firebaseViewModel: FirebaseViewModel)
    : ListAdapter<Message, RecyclerView.ViewHolder>(MessagesCallback()) {
//    private val SENT = 0
//    private val RECEIVED = 1

    class SentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val parent = itemView.findViewById<LinearLayout>(R.id.parent)
        val text = itemView.findViewById<TextView>(R.id.text)
        val timestamp = itemView.findViewById<TextView>(R.id.timestamp)
    }

    class ReceivedViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val parent = itemView.findViewById<LinearLayout>(R.id.parent)
        val text = itemView.findViewById<TextView>(R.id.text)
        val empty_profile_pic = itemView.findViewById<FrameLayout>(R.id.profile_image_silhouette)
        val emptyPadding = itemView.findViewById<View>(R.id.empty_padding)
        val timestamp = itemView.findViewById<TextView>(R.id.timestamp)
    }

    class SystemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val systemText = itemView.findViewById<TextView>(R.id.system_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == MessageType.SENT){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_sent_text, parent, false)
            return SentViewHolder(view)
        } else if(viewType == MessageType.SYSTEM){
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
            viewHolder.text.text = item.message

            viewHolder.timestamp.text = MessageUtils.formatStampMessage(item.time.toString())

            try {
                val prev: Message? = getItem(position - 1)
                if(prev?.sender.equals(item.sender) && MessageUtils.calculateTimestampDifferenceLess(item.time!!, prev?.time!!)){
                    viewHolder.parent.background = fragment.activity?.let { ContextCompat.getDrawable(it, R.drawable.bg_current_user_chat_second) }
                } else {
                    viewHolder.parent.background = fragment.activity?.let { ContextCompat.getDrawable(it, R.drawable.bg_current_user_chat) }
                }
            } catch(e: Exception){
                viewHolder.parent.background = fragment.activity?.let { ContextCompat.getDrawable(it, R.drawable.bg_current_user_chat) }
            }

            try {
                val next: Message? = getItem(position + 1)
                if(next?.sender == item.sender  && MessageUtils.calculateTimestampDifferenceLess(next?.time!!, item.time!!)){
                    viewHolder.timestamp.visibility = View.GONE
                } else {
                    viewHolder.timestamp.visibility = View.VISIBLE
                }
            } catch(e: Exception) {
                viewHolder.timestamp.visibility = View.VISIBLE
            }

        } else if(holder.javaClass == SystemViewHolder::class.java){
            // system message
            val viewHolder = holder as SystemViewHolder

            formatSystemMessage(item, item.time) {
                viewHolder.systemText.text = it
            }

        }else {
            // Received Holder
            val viewHolder = holder as ReceivedViewHolder
            viewHolder.text.text = item.message
            setOtherUserTimestamp(viewHolder, item)

            try {
                val next: Message? = getItem(position + 1)
                if(next?.sender.equals(item.sender)){
                    viewHolder.parent.background = fragment.activity?.let { ContextCompat.getDrawable(it, R.drawable.bg_other_user_chat) }
                    viewHolder.timestamp.visibility = View.GONE
                } else {
                    viewHolder.parent.background = fragment.activity?.let { ContextCompat.getDrawable(it, R.drawable.bg_other_user_chat_second) }
                    viewHolder.timestamp.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                viewHolder.parent.background = fragment.activity?.let { ContextCompat.getDrawable(it, R.drawable.bg_other_user_chat_second) }
                viewHolder.timestamp.visibility = View.VISIBLE
            }


            try {
                val prev: Message? = getItem(position - 1)
                if(prev?.sender.equals(item.sender)){
                    viewHolder.empty_profile_pic.visibility = View.GONE
                    viewHolder.emptyPadding.visibility = View.VISIBLE
                } else {
                    viewHolder.empty_profile_pic.visibility = View.VISIBLE
                    viewHolder.emptyPadding.visibility = View.GONE
                }
            } catch (e: Exception) {
                viewHolder.empty_profile_pic.visibility = View.VISIBLE
                viewHolder.emptyPadding.visibility = View.GONE
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item: Message = getItem(position)
        if(item.sender.toString() == mAuth.currentUser?.uid.toString()){
            return MessageType.SENT
        } else if(item.sender == "SYSTEM"){
            return MessageType.SYSTEM
        }else {
            return MessageType.RECEIVED
        }
    }

    fun getUser(userId: String, callback: (user: User)-> Unit){
        firebaseViewModel.getUserSingle(userId, mDbRef, {
            if(it != null){
                callback(it)
            }
        },{})
    }

    fun formatSystemMessage(message: Message, time: Long, afterCallback: (message: String)-> Unit){
        if(message.messageUID == SystemMessageType.GROUP_REMOVE){
            val messageArr: Array<String> = message.message.split(" ").toTypedArray()

            getUser(messageArr[0]){ remover ->
                getUser(messageArr[1]) { removed ->
                    afterCallback("${remover.name} removed ${removed.name}")
                }
            }
        }

        getUser(message.message) {
            if(message.messageUID == SystemMessageType.GROUP_CREATE){
                afterCallback("${it.name} created the group ${MessageUtils.formatStampChatsPage(time.toString())}")
            } else if(message.messageUID == SystemMessageType.NOW_ADMIN){
                afterCallback("${it.name} is now admin")
            } else if(message.messageUID == SystemMessageType.NOT_ADMIN){
                afterCallback("${it.name} is no longer admin")
            } else if(message.messageUID == SystemMessageType.GROUP_EXIT){
                afterCallback("${it.name} left the group")
            }
        }
    }

    fun setOtherUserTimestamp(viewHolder: ReceivedViewHolder, item: Message){
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
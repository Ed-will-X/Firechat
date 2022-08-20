package com.varsel.firechat.view.signedIn.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.varsel.firechat.R
import com.varsel.firechat.model.message.Message
import com.varsel.firechat.view.signedIn.fragments.ChatPageFragment
import java.lang.Exception

class MessageListAdapter(val mAuth: FirebaseAuth, val fragment: ChatPageFragment): ListAdapter<Message, RecyclerView.ViewHolder>(MessagesCallback()) {
    private val SENT = 0
    private val RECEIVED = 1

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == SENT){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_sent_text, parent, false)
            return SentViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_received_text, parent, false)
            return ReceivedViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item: Message = getItem(position)

        Log.d("LLL", "Current ${item.message}")

        if(holder.javaClass == SentViewHolder::class.java){
            // Sent Holder
            val viewHolder = holder as SentViewHolder
            viewHolder.text.text = item.message
            try {
                val prev: Message? = getItem(position - 1)
                if(prev?.sender.equals(item.sender)){
                    viewHolder.parent.background = fragment.activity?.let { ContextCompat.getDrawable(it, R.drawable.bg_current_user_chat_second) }
                }
            } catch(e: Exception){ }

            try {
                val next: Message? = getItem(position + 1)
                if(next?.sender == item.sender){
                    viewHolder.timestamp.visibility = View.GONE
                }
            } catch(e: Exception) {}


        } else {
            // Received Holder
            val viewHolder = holder as ReceivedViewHolder
            viewHolder.text.text = item.message
            try {
                val next: Message? = getItem(position + 1)
                if(next?.sender.equals(item.sender)){
                    viewHolder.parent.background = fragment.activity?.let { ContextCompat.getDrawable(it, R.drawable.bg_other_user_chat) }
                    viewHolder.timestamp.visibility = View.GONE
                }
            } catch (e: Exception) {}
            try {
                val prev: Message? = getItem(position - 1)
                if(prev?.sender.equals(item.sender)){
                    viewHolder.empty_profile_pic.visibility = View.GONE
                    viewHolder.emptyPadding.visibility = View.VISIBLE
                }
            } catch (e: Exception) {}
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item: Message = getItem(position)
        if(item.sender.toString() == mAuth.currentUser?.uid.toString()){
            return SENT
        } else {
            return RECEIVED
        }
    }
}

class MessagesCallback(): DiffUtil.ItemCallback<Message>(){
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean  = oldItem.messageUID == newItem.messageUID

    // TODO: Fix potential bug
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean = oldItem == newItem
}
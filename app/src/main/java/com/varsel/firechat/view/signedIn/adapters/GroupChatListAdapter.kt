package com.varsel.firechat.view.signedIn.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.varsel.firechat.R
import com.varsel.firechat.model.Chat.ChatRoom
import com.varsel.firechat.model.Chat.GroupRoom
import com.varsel.firechat.model.Message.Message
import com.varsel.firechat.model.ProfileImage.ProfileImage
import com.varsel.firechat.utils.AnimationUtils
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.MessageUtils
import com.varsel.firechat.utils.UserUtils
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.viewModel.FirebaseViewModel

val FAVORITE_ICON_DARK_MODE = R.color.grey

class GroupChatsListAdapter(
    val activity: SignedinActivity,
    val context: Context,
    val addNewListener: ()-> Unit,
    val groupItemListener: (id: String, image: ProfileImage?)-> Unit,
    val imageClickListener: (groupImage: ProfileImage, group: GroupRoom) -> Unit
) : ListAdapter<GroupRoom, RecyclerView.ViewHolder>(GroupChatDiffUtilItemCallback()) {
    private val ADD_NEW = 0
    private val GROUP_CHAT = 1

    val unreadGroups = mutableMapOf<String, GroupRoom>()

    class GroupChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val parent = itemView.findViewById<MaterialCardView>(R.id.group_parent)
        val groupName: TextView = itemView.findViewById<TextView>(R.id.group_name)
        val favoriteIcon: ImageView = itemView.findViewById(R.id.favorite_icon)
        val img_card_1 = itemView.findViewById<MaterialCardView>(R.id.gc_image_card_1)
        val img_card_2 = itemView.findViewById<MaterialCardView>(R.id.gc_image_card_2)
        val img_card_3 = itemView.findViewById<MaterialCardView>(R.id.gc_image_card_3)
        val img_card_4 = itemView.findViewById<MaterialCardView>(R.id.gc_image_card_4)
        val img_card_5 = itemView.findViewById<MaterialCardView>(R.id.gc_image_card_5)
        val imageParent = itemView.findViewById<FrameLayout>(R.id.profile_image_parent)
        val image = itemView.findViewById<ImageView>(R.id.profile_image)
    }

    class AddNewViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val parent = itemView.findViewById<MaterialCardView>(R.id.add_new_group_parent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == GROUP_CHAT){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_group_chat_item, parent, false)
            return GroupChatViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_group_chat_add, parent, false)
            return AddNewViewHolder(view)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item: GroupRoom = getItem(position)


        if(holder.javaClass == AddNewViewHolder::class.java){
            val viewHolder = holder as AddNewViewHolder

            viewHolder.parent.setOnClickListener {
                addNewListener()
            }

        } else {
            val viewHolder = holder as GroupChatViewHolder
            val participants = filterOutCurrentUser(item.participants!!.values.toList())

            val lastMessage = MessageUtils.getLastMessageObject(item)
            if(lastMessage != null){
                determineReceipts(item, lastMessage, {
                    holder.parent.strokeWidth = activity.getResources().getDimensionPixelSize(R.dimen.unread_group_stroke_width)
                    holder.parent.strokeColor = activity.resources.getColor(R.color.gold)
                }, {
                    holder.parent.strokeWidth = activity.getResources().getDimensionPixelSize(R.dimen.zero)
                    holder.parent.strokeColor = activity.resources.getColor(R.color.gold)
                })
            }

            getParticipantCount(participants, viewHolder)

            viewHolder.parent.setOnClickListener {
                groupItemListener(item.roomUID, null)
            }

            viewHolder.groupName.text = item.groupName?.let { UserUtils.truncate(it, 15) }

            if(isFavorite(item.roomUID)){
                AnimationUtils.changeColor(holder.favoriteIcon, R.color.yellow, context)
            } else {
                if(activity.isNightMode()){
                    AnimationUtils.changeColor(holder.favoriteIcon, FAVORITE_ICON_DARK_MODE, context)
                } else {
                    AnimationUtils.changeColor(holder.favoriteIcon, R.color.light_grey_2, context)
                }
            }

            holder.favoriteIcon.setOnClickListener {
                setFavorite(item.roomUID, holder.favoriteIcon)
            }

            ImageUtils.setProfilePicGroup_fullObject(item, holder.image, holder.imageParent, activity) { profileImage ->
                viewHolder.parent.setOnClickListener {
                    if(profileImage != null){
                        groupItemListener(item.roomUID, profileImage)
                    } else {
                        groupItemListener(item.roomUID, null)
                    }
                }

                if(profileImage != null){
                    holder.image.setOnClickListener {
                        imageClickListener(profileImage, item)

                    }
                }
            }
        }
    }

    private fun determineReceipts(item: GroupRoom, lastMessage: Message, receiptCallback: ()-> Unit, noReceiptCallback: ()-> Unit){
        val receipt = activity.readReceiptViewModel.fetchReceipt("${item.roomUID}:${activity.firebaseAuth.currentUser!!.uid}")


        receipt.observe(activity, Observer {

            if(it == null || it.timestamp < lastMessage.time){
                unreadGroups.put(item.roomUID, item)
                receiptCallback()
            } else {
                unreadGroups.remove(item.roomUID)
                noReceiptCallback()
            }
        })
    }


    private fun setFavorite(groupId: String, icon: ImageView){
        if(!isFavorite(groupId)){
            activity.firebaseViewModel.addGroupToFavorites(groupId, activity.firebaseAuth, activity.mDbRef, {
                AnimationUtils.changeColor(icon, R.color.yellow, context)
            },{
            })
        } else {
            activity.firebaseViewModel.removeGroupFromFavorites(groupId, activity.firebaseAuth, activity.mDbRef, {
                if(activity.isNightMode()){
                    AnimationUtils.changeColor(icon, FAVORITE_ICON_DARK_MODE, context)
                } else {
                    AnimationUtils.changeColor(icon, R.color.light_grey_2, context)
                }
            },{

            })
        }
    }

    private fun isFavorite(groupID: String): Boolean{
        val favorites = activity.firebaseViewModel.currentUser.value!!.favoriteGroups?.keys

        if (favorites != null) {
            for(i in favorites){
                if(i == groupID){
                    return true
                }
            }
        }

        return false
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        if(item.roomUID == "ADD_NEW_GROUP_CHAT"){
            return ADD_NEW
        } else {
            return GROUP_CHAT
        }
    }

    private fun filterOutCurrentUser(users: List<String>): List<String>{
        val currentUser = activity.firebaseAuth.currentUser?.uid.toString()
        val otherUsers: MutableList<String> = mutableListOf()

        for(i in users){
            if(i != currentUser){
                otherUsers.add(i)
            }
        }

        return otherUsers
    }

    // TODO: Implement for only users who have profile pictures
    private fun setUsersProfilePicture(){

    }

    // TODO: Implement get participants
    private fun getParticipants(){

    }

    private fun getParticipantCount(users: List<String>, holder: GroupChatViewHolder){
        if(users.isEmpty()){
            holder.img_card_5.visibility = View.GONE
            holder.img_card_4.visibility = View.GONE
            holder.img_card_3.visibility = View.GONE
            holder.img_card_2.visibility = View.GONE
            holder.img_card_1.visibility = View.GONE
        } else if(users.size == 1){
            holder.img_card_5.visibility = View.GONE
            holder.img_card_4.visibility = View.GONE
            holder.img_card_3.visibility = View.GONE
            holder.img_card_2.visibility = View.GONE
        } else if(users.size == 2){
            holder.img_card_5.visibility = View.GONE
            holder.img_card_4.visibility = View.GONE
            holder.img_card_3.visibility = View.GONE
        } else if(users.size == 3){
            holder.img_card_5.visibility = View.GONE
            holder.img_card_4.visibility = View.GONE
        } else if(users.size == 4){
            holder.img_card_5.visibility = View.GONE
        } else if(users.size > 4){

        }
    }
}

class GroupChatDiffUtilItemCallback(): DiffUtil.ItemCallback<GroupRoom>(){
    override fun areItemsTheSame(oldItem: GroupRoom, newItem: GroupRoom): Boolean = oldItem.roomUID == newItem.roomUID

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: GroupRoom, newItem: GroupRoom): Boolean = oldItem == newItem

}
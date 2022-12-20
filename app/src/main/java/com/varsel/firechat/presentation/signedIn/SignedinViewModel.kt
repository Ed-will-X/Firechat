package com.varsel.firechat.presentation.signedIn

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.Chat.GroupRoom

class SignedinViewModel(): ViewModel() {
    val currentChatRoomId = MutableLiveData<String>()

    fun setNetworkOverlayTimer(onEnd: ()-> Unit): CountDownTimer {
        val timer = object : CountDownTimer(4000, 1000){
            override fun onTick(millisUntilFinished: Long) {
                if(millisUntilFinished > 5000){
                    this.cancel()
                }
            }

            override fun onFinish() {
                onEnd()
            }
        }.start()

        return timer
    }

    fun getCurrentUserSingle(activity: SignedinActivity){
        activity.firebaseViewModel.getCurrentUserSingle(activity.firebaseAuth, activity.mDbRef, { }, {
            // sets the chat rooms in the viewModel
            if(activity.firebaseViewModel.currentUser.value?.chatRooms != null && activity.firebaseViewModel.currentUser.value?.chatRooms?.isNotEmpty() == true){
                getAllChats(activity, activity.firebaseViewModel.currentUser.value!!.chatRooms!!.keys.toList())
            } else {
                // current user data is not stored in the viewModel
                activity.firebaseViewModel.chatRooms.value = mutableListOf()
            }

            // Sets the groups in the viewModel
            if(activity.firebaseViewModel.currentUser.value?.groupRooms != null && activity.firebaseViewModel.currentUser.value?.groupRooms?.isNotEmpty() == true){
                getAllGroupChats(activity, activity.firebaseViewModel.currentUser.value!!.groupRooms!!.keys.toList())
            } else {
                activity.firebaseViewModel.groupRooms.value = mutableListOf()
            }

        })
    }

    private fun getAllChats(activity: SignedinActivity, chatRoomsUID: List<String>){
        val chatRooms = mutableListOf<ChatRoom>()
        for(i in chatRoomsUID){
            activity.firebaseViewModel.getChatRoomRecurrent(i, activity.mDbRef, { chatRoom ->
                if(activity.firebaseViewModel.chatRooms.value != null){
                    val groupIterator = activity.firebaseViewModel.chatRooms.value!!.iterator()
                    while (groupIterator.hasNext()){
                        val g = groupIterator.next()
                        if(g?.roomUID == chatRoom?.roomUID){
                            groupIterator.remove()
                        }
                    }
                }
                if(chatRoom != null){
                    chatRooms.add(chatRoom)
                }
            }, {
                activity.firebaseViewModel.chatRooms.value = chatRooms
            })
        }
    }

    private fun getAllGroupChats(activity: SignedinActivity, groupRoomIDs: List<String>){
        val groupRooms = mutableListOf<GroupRoom>()

        for (i in groupRoomIDs){
            // TODO: Fix bug here:
            activity.firebaseViewModel.getGroupChatRoomRecurrent(i, activity.mDbRef, {
                if(activity.firebaseViewModel.groupRooms.value != null){
                    val groupIterator = activity.firebaseViewModel.groupRooms.value!!.iterator()
                    while (groupIterator.hasNext()){
                        val g = groupIterator.next()
                        if(g?.roomUID == it?.roomUID){
                            groupIterator.remove()
                        }
                    }
                }
                if(it != null){
                    groupRooms.add(it)
                }

            }, {
                activity.firebaseViewModel.groupRooms.value = groupRooms
            })
        }
    }

    fun determineChatroom(userId: String, chatRooms: MutableList<ChatRoom>?): Boolean{
        // TODO: Fix potential null pointer exception
        val chatRooms = chatRooms!!
        var contains: Boolean = false
        for(i in chatRooms){
            if(i.participants!!.contains(userId)){
                contains = true
                currentChatRoomId.value = i.roomUID
                break
            }
        }
        return contains
    }

    fun findChatRoom(userId: String, chatRooms: MutableList<ChatRoom?>?): ChatRoom?{
        // TODO: Fix potential null pointer exception
        val chatRooms = chatRooms!!
        for(i in chatRooms){
            if(i!!.participants!!.contains(userId)){
                currentChatRoomId.value = i.roomUID.toString()
                return i
            }
        }

        return null
    }
}
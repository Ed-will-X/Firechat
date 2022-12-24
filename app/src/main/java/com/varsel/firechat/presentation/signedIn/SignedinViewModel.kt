package com.varsel.firechat.presentation.signedIn

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.domain.use_case.current_user.GetCurrentUserRecurrentUseCase
import com.varsel.firechat.domain.use_case.current_user.GetFriendsUseCase
import com.varsel.firechat.domain.use_case.current_user.OpenCurrentUserCollectionStream
import com.varsel.firechat.domain.use_case.current_user.OpenFriendsUpdateStream
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SignedinViewModel @Inject constructor(
    val openCurrentUserCollectionStream: OpenCurrentUserCollectionStream,
    val getCurrentUserRecurrentUseCase: GetCurrentUserRecurrentUseCase,
    val openFriendsUpdateStream: OpenFriendsUpdateStream,
    val getFriendsUseCase: GetFriendsUseCase
): ViewModel() {
    val currentChatRoomId = MutableLiveData<String>()
//    private val signedInState = MutableStateFlow(SignedInActivityState(dataState = ))

    init {
        // Opens the realtime database stream to receive current user
        openCurrentUserCollectionStream().onEach {

        }.launchIn(viewModelScope)

        // Observes the user
        getCurrentUserRecurrentUseCase().onEach {
            Log.d("CLEAN", "${it.data?.name}")
            // TODO: Initialise fetch friend stream
            if(it.data?.friends != null) {
                openFriendsUpdateStream(it.data.friends).onEach {
                    if(it == Response.Success()){
                        Log.d("CLEAN", "Successfully opened friend update stream")
                    }
                }.launchIn(viewModelScope)
            }

        }.launchIn(viewModelScope)

        getFriendsUseCase().onEach {
            Log.d("CLEAN", "Friend Count: ${it.data?.size}")
        }.launchIn(viewModelScope)
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
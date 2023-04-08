package com.varsel.firechat.presentation.signedIn.fragments.screens.chat_page

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.common.Resource
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.Message.Message
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.repository.FirebaseRepository
import com.varsel.firechat.domain.use_case._util.message.CalculateTimestampDifferenceLess_UseCase
import com.varsel.firechat.domain.use_case._util.message.FormatStampMessage_UseCase
import com.varsel.firechat.domain.use_case._util.message.FormatSystemMessage_UseCase
import com.varsel.firechat.domain.use_case._util.message.GetLastMessage_UseCase
import com.varsel.firechat.domain.use_case.chat_room.AppendChatRoom_UseCase
import com.varsel.firechat.domain.use_case.chat_room.AppendParticipants_UseCase
import com.varsel.firechat.domain.use_case.chat_room.SendMessage_UseCase
import com.varsel.firechat.domain.use_case.current_user.GetCurrentUserIdUseCase
import com.varsel.firechat.domain.use_case.chat_image.CheckChatImageInDb
import com.varsel.firechat.domain.use_case.chat_image.GetChatImageUseCase
import com.varsel.firechat.domain.use_case.chat_image.SetChatImageUseCase
import com.varsel.firechat.domain.use_case.chat_image.StoreChatImageUseCase
import com.varsel.firechat.domain.use_case.message.GetChatRoomsRecurrentUseCase
import com.varsel.firechat.domain.use_case.other_user.GetOtherUserFromParticipants_UseCase
import com.varsel.firechat.domain.use_case.other_user.GetOtherUserSingle
import com.varsel.firechat.domain.use_case.profile_image.GetOtherUserProfileImageUseCase
import com.varsel.firechat.domain.use_case.profile_image.SetProfilePicUseCase
import com.varsel.firechat.domain.use_case.read_receipt.StoreChatReceipt_UseCase
import com.varsel.firechat.domain.use_case.read_receipt.StoreGroupReceipt_UseCase
import com.varsel.firechat.domain.use_case.read_receipt_temp.StoreReceipt_UseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ChatPageViewModel @Inject constructor(
    val getOtherUserProfileImageUseCase: GetOtherUserProfileImageUseCase,
    val setProfilePicUseCase: SetProfilePicUseCase,
    val getChatRoomsRecurrentUseCase: GetChatRoomsRecurrentUseCase,
    val getOtherUserSingle: GetOtherUserSingle,
    val getotheruserfromparticipantsUsecase: GetOtherUserFromParticipants_UseCase,
    val sendmessageUsecase: SendMessage_UseCase,
    val appendparticipantsUsecase: AppendParticipants_UseCase,
    val appendchatroomUsecase: AppendChatRoom_UseCase,
    val getCurrentUserId: GetCurrentUserIdUseCase,
    val checkChatImageInDb: CheckChatImageInDb,
    val getChatImageUseCase: GetChatImageUseCase,
    val storeChatImageUseCase: StoreChatImageUseCase,
    val setChatImageUseCase: SetChatImageUseCase,
    val formatStampMessage: FormatStampMessage_UseCase,
    val calculateTimestampDifferenceLess: CalculateTimestampDifferenceLess_UseCase,
    val getLastMessage: GetLastMessage_UseCase,
    val formatSystemMessage: FormatSystemMessage_UseCase,
    val firebase: FirebaseRepository,
    val storeReceipt: StoreChatReceipt_UseCase,
    val storeGroupReceipt: StoreGroupReceipt_UseCase
): ViewModel() {
    val actionBarVisibility = MutableLiveData<Boolean>(false)

    private val _state = MutableLiveData(ChatPageState())
    val state: LiveData<ChatPageState> = _state

    private val _user = MutableLiveData<User?>(null)
    val user: LiveData<User?> = _user

    private val _hasRun = MutableLiveData(false)

    val _lastFetchTimestamp = MutableLiveData(0L)

    private val _first_message_sent = MutableLiveData(false)

    init {
        // TODO: Move to the chat page, for performance optimisations, because another fragment is using the entire viewModel for the use cases
        getChatRooms()
    }

    fun getOtherUser(userId: String) {
        // gets the participants
        getOtherUserSingle(userId).onEach {
            when(it) {
                is Resource.Success -> {
                    _user.value = it.data
                }
                is Resource.Loading -> {
                    _user.value = null
                }
                is Resource.Error -> {
                    _user.value = null
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun getChatRooms() {
        getChatRoomsRecurrentUseCase().onEach {
            when(it) {
                is Resource.Success -> {
                    _state.value = _state.value?.copy(chatRooms = it.data ?: listOf())
                }
                is Resource.Loading -> {
                    _state.value = _state.value?.copy(chatRooms = listOf())
                }
                is Resource.Error -> {
                    _state.value = _state.value?.copy(chatRooms = listOf())
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getChatRoom(id: String) {
        for (i in state.value?.chatRooms ?: listOf()) {
            if(i.roomUID == id && (getLastMessage(i)?.time ?: 0L) > _lastFetchTimestamp.value!!) {
                // Sets the selected room value
                _state.value = _state.value?.copy(selectedChatRoom = i)

                _lastFetchTimestamp.value = getLastMessage(i)?.time
                break
            }
        }
    }

    fun toggleActionbarVisibility(){
        actionBarVisibility.value = actionBarVisibility.value?.not()
    }

    fun handleSendMessage(message: Message, existingRoomId: String?, newRoom: ChatRoom, after: () -> Unit) {
//        message.readBy.put(getCurrentUserId(), System.currentTimeMillis()) // TODO: Check modification error

        if(user.value != null) {
            if(existingRoomId != null) {
                if(state.value?.selectedChatRoom != null) {
                    sendMessage(message, existingRoomId) {
                        after()
                    }
                }
            } else {
                sendMessageToNewRoom(message, newRoom) {
                    after()
                }
            }
        }
    }

    private fun sendMessage(message: Message, existingRoomId: String, after: ()-> Unit) {
        sendmessageUsecase(message, existingRoomId).onEach {
            when(it) {
                is Response.Success -> { after() }
                is Response.Loading -> { }
                is Response.Fail -> { }
            }
        }.launchIn(viewModelScope)
    }

    private fun sendMessageToNewRoom(message: Message, newRoom: ChatRoom, after: () -> Unit) {
        appendChatRoomToUsers(newRoom.roomUID, user.value?.userUID!!) {
            Log.d("CLEAN", "Append to Users Ran")
            if(_first_message_sent.value == false){
                Log.d("CLEAN", "First message sent is false")
                appendParticipantsToChatRoom(newRoom) {
                    Log.d("CLEAN", "Append to Room Ran")
                    sendMessage(message, newRoom.roomUID) {
                        Log.d("CLEAN", "Send message Ran")
                        after()
                        _first_message_sent.value = true
                    }
                }
            } else {
                Log.d("CLEAN", "First message sent is true")
                sendMessage(message, newRoom.roomUID) {
                    after()
                }
            }
        }
    }

    private fun appendChatRoomToUsers(roomId: String, otherUserId: String, success: () -> Unit) {
        appendchatroomUsecase(roomId, otherUserId).onEach {
            when(it) {
                is Response.Success -> {
                    success()
                }
                is Response.Loading -> { }
                is Response.Fail -> { }
            }
        }.launchIn(viewModelScope)
    }

    fun appendParticipantsToChatRoom(chatRoom: ChatRoom, success: ()-> Unit) {
        appendparticipantsUsecase(chatRoom).onEach {
            when(it) {
                is Response.Success -> {
                    success()
                }
                is Response.Loading -> { }
                is Response.Fail -> { }
            }
        }.launchIn(viewModelScope)
    }
}

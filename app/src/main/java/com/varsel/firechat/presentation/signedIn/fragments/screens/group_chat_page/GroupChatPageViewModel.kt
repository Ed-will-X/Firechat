package com.varsel.firechat.presentation.signedIn.fragments.screens.group_chat_page

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.common.Resource
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.use_case._util.message.GetLastMessage_UseCase
import com.varsel.firechat.domain.use_case.current_user.GetCurrentUserIdUseCase
import com.varsel.firechat.domain.use_case.group_chat.GetGroupChatRecurrentUseCase
import com.varsel.firechat.domain.use_case.group_chat.GetGroupParticipantsUseCase
import com.varsel.firechat.domain.use_case.message.GetGroupRoomsRecurrentUseCase
import com.varsel.firechat.domain.use_case.profile_image.GetGroupImageUseCase
import com.varsel.firechat.domain.use_case.read_receipt.StoreGroupReceipt_UseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupChatPageViewModel @Inject constructor(
    private val getGroup: GetGroupChatRecurrentUseCase,
    private val getAllGroups: GetGroupRoomsRecurrentUseCase,
    private val getGroupParticipantsUseCase: GetGroupParticipantsUseCase,
    val getCurrentUserId: GetCurrentUserIdUseCase,
    private val getLastMessage: GetLastMessage_UseCase,
    val getGroupImage_UseCase: GetGroupImageUseCase,
    val storeReceipt: StoreGroupReceipt_UseCase
) : ViewModel() {

    private val _state = MutableLiveData(GroupChatPageState())
    val state = _state

    val _hasRun = MutableLiveData(false)

    private val _participants = MutableLiveData(listOf<User>())
    val participsnts = _participants

    val _lastFetchTimestamp = MutableLiveData(0L)
    val _lastParticipantCount = MutableLiveData(-1)

    init {
        getGroupRooms()
    }

    fun getGroupImage(groupRoom: GroupRoom) {
        viewModelScope.launch {
            getGroupImage_UseCase(groupRoom).onEach {
                _state.value = _state.value?.copy(groupImage = it)
            }.launchIn(this)
        }
    }

    private fun getGroupRooms() {
        getAllGroups().onEach {
            when(it) {
                is Resource.Success -> {
                    _state.value = _state.value?.copy(groupRooms = it.data ?: listOf())
                }
                is Resource.Loading -> {
                    _state.value = _state.value?.copy(groupRooms = listOf())
                }
                is Resource.Error -> {
                    _state.value = _state.value?.copy(groupRooms = listOf())
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getGroupChat(id: String) {
        for (i in state.value?.groupRooms ?: listOf()) {
            if(i.roomUID == id && (getLastMessage(i)?.time ?: 0L) > _lastFetchTimestamp.value!!) {
                // Sets the selected room value
                _state.value = _state.value?.copy(selectedRoom = i)

                // gets the participants
                if(_lastParticipantCount.value == -1 || _lastParticipantCount.value != i.participants.count()) {
                    getParticipants(id, i)
                    _lastParticipantCount.value = i.participants.size
                }

                _lastFetchTimestamp.value = getLastMessage(i)?.time
                break
            }
        }
    }

    fun getParticipants(id: String, group: GroupRoom) {
        getGroupParticipantsUseCase(id).onEach {
            if(group.participants.count() == it.count()) {
                _participants.value = it
            }
        }.launchIn(viewModelScope)
    }
}
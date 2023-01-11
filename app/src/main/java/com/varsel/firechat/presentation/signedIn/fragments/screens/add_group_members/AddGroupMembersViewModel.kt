package com.varsel.firechat.presentation.signedIn.fragments.screens.add_group_members

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.use_case._util.search.SetupSearchBarUseCase
import com.varsel.firechat.domain.use_case.current_user.GetCurrentUserIdUseCase
import com.varsel.firechat.domain.use_case.current_user.GetFriendsUseCase
import com.varsel.firechat.domain.use_case.group_chat.AddGroupMembers_UseCase
import com.varsel.firechat.domain.use_case.group_chat.GetGroupParticipantsUseCase
import com.varsel.firechat.domain.use_case.message.GetGroupRoomsRecurrentUseCase
import com.varsel.firechat.domain.use_case.profile_image.GetOtherUserProfileImageUseCase
import com.varsel.firechat.domain.use_case.profile_image.SetProfilePicUseCase
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class AddGroupMembersViewModel @Inject constructor(
    val getOtherUserProfileImageUseCase: GetOtherUserProfileImageUseCase,
    val setProfilePicUseCase: SetProfilePicUseCase,
    val setupSearchBarUseCase: SetupSearchBarUseCase,
    val getFriendsUseCase: GetFriendsUseCase,
    val getAllGroups: GetGroupRoomsRecurrentUseCase,
    val getGroupParticipantsUseCase: GetGroupParticipantsUseCase,
    val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    val addGroupMembers: AddGroupMembers_UseCase
): ViewModel() {
    val hasBeenClicked = MutableLiveData<Boolean>(false)

    private val _state = MutableLiveData(AddGroupMembersState())
    private val state = _state

    private val _participants = MutableLiveData(listOf<User>())
    val participants = _participants

    private val _non_participants = MutableStateFlow(listOf<User>())
    val non_participants = _non_participants

    init {
        getGroupRooms()
    }

    private fun getGroupRooms() {
        val groupRooms = getAllGroups().value.data
        _state.value = _state.value?.copy(groupRooms = groupRooms ?: listOf())
    }

    fun getGroupChat(id: String) {
        for (i in state.value?.groupRooms ?: listOf()) {
            if(i.roomUID == id) {
                // Sets the selected room value
                _state.value = _state.value?.copy(selectedGroup = i)

                // gets the participants
                getParticipants(id, i)
                break
            }
        }
    }

    // TODO: Move to background thread
    fun getNonParticipants(participants: List<String>){
        val friends: List<User> = getFriendsUseCase().value.data ?: listOf()
        val non_participants = mutableListOf<User>()

        for((i_index, i_value) in friends.withIndex()){
            for((j_index, j_value) in participants.withIndex()){
                if(i_value.userUID == j_value){
                    break
                } else if(j_index == participants.size -1) {
                    non_participants.add(i_value)
                }
            }
        }

        _non_participants.value = non_participants
    }

    fun getParticipants(id: String, group: GroupRoom) {
        getGroupParticipantsUseCase(id).onEach {
            if(group.participants.count() == it.count()) {
                _participants.value = it

                getNonParticipants(group.participants.keys.toList())
            }
        }.launchIn(viewModelScope)
    }
}
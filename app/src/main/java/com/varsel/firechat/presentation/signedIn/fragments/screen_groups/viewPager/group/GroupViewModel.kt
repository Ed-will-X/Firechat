package com.varsel.firechat.presentation.signedIn.fragments.screen_groups.viewPager.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.common.Resource
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.domain.use_case._util.animation.ChangeIconColorUseCase
import com.varsel.firechat.domain.use_case._util.message.GetLastMessage_UseCase
import com.varsel.firechat.domain.use_case._util.message.HasBeenRead_UseCase
import com.varsel.firechat.domain.use_case._util.string.Truncate_UseCase
import com.varsel.firechat.domain.use_case._util.system.CheckIfNightMode_UseCase
import com.varsel.firechat.domain.use_case.current_user.GetCurrentUserRecurrentUseCase
import com.varsel.firechat.domain.use_case.group_chat.AddGroupToFavorites_UseCase
import com.varsel.firechat.domain.use_case.group_chat.RemoveGroupFromFavorites_UseCase
import com.varsel.firechat.domain.use_case.message.GetGroupRoomsRecurrentUseCase
import com.varsel.firechat.domain.use_case.profile_image.GetGroupImageUseCase
import com.varsel.firechat.domain.use_case.profile_image.SetProfilePicUseCase
import com.varsel.firechat.domain.use_case.read_receipt_temp.FetchReceipt_UseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    val getGroupRoomsRecurrentUseCase: GetGroupRoomsRecurrentUseCase,
    val getCurrentUserRecurrentUseCase: GetCurrentUserRecurrentUseCase,
    val getGroupImageUseCase: GetGroupImageUseCase,
    val setProfilePicUseCase: SetProfilePicUseCase,
    val changeIconColor: ChangeIconColorUseCase,
    val truncate: Truncate_UseCase,
    val getLastMessage: GetLastMessage_UseCase,
    val fetchReceipt: FetchReceipt_UseCase,
    val addGroupToFavorites: AddGroupToFavorites_UseCase,
    val removeGroupFromFavorites: RemoveGroupFromFavorites_UseCase,
    val isNightMode: CheckIfNightMode_UseCase,
    val hasBeenRead: HasBeenRead_UseCase

): ViewModel() {
    private val _state = MutableStateFlow(GroupFragmentState())
    val state = _state

    private val _groupRooms = MutableLiveData(listOf<GroupRoom>())
    val groupRooms: LiveData<List<GroupRoom>> = _groupRooms

    init {
        getGroups()
        getUser()
    }

    private fun getGroups() {
        getGroupRoomsRecurrentUseCase().onEach {
            when(it){
                is Resource.Success -> {
                    _state.value = _state.value.copy(isLoading = false)
                    _groupRooms.value = it.data ?: listOf()
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                    _groupRooms.value = listOf()
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false)
                    _groupRooms.value = listOf()
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun getUser() {
        getCurrentUserRecurrentUseCase().onEach {
            when(it){
                is Resource.Success -> {
                    _state.value = _state.value.copy(currentUser = it.data)
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(currentUser = null)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(currentUser = null)
                }
            }
        }.launchIn(viewModelScope)
    }
}
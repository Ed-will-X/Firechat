package com.varsel.firechat.presentation.signedIn.fragments.screen_groups.viewPager.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.common.Resource
import com.varsel.firechat.domain.use_case._util.animation.ChangeIconColorUseCase
import com.varsel.firechat.domain.use_case.current_user.GetCurrentUserRecurrentUseCase
import com.varsel.firechat.domain.use_case.message.GetGroupRoomsRecurrentUseCase
import com.varsel.firechat.domain.use_case.profile_image.GetGroupImageUseCase
import com.varsel.firechat.domain.use_case.profile_image.GetOtherUserProfileImageUseCase
import com.varsel.firechat.domain.use_case.profile_image.SetProfilePicUseCase
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
    val changeIconColor: ChangeIconColorUseCase
): ViewModel() {
    private val _state = MutableStateFlow(GroupFragmentState())
    val state = _state

    init {
        getGroups()
        getUser()
    }

    private fun getGroups() {
        getGroupRoomsRecurrentUseCase().onEach {
            when(it){
                is Resource.Success -> {
                    _state.value = _state.value.copy(groupRooms = it.data ?: listOf(), isLoading = false)
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(groupRooms = listOf(), isLoading = true)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(groupRooms = listOf(), isLoading = false)
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
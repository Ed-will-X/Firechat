package com.varsel.firechat.presentation.signedIn.fragments.screen_groups.bottomNav.chats_tab_page

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.common.Resource
import com.varsel.firechat.domain.use_case.current_user.GetCurrentUserRecurrentUseCase
import com.varsel.firechat.domain.use_case.profile_image.GetCurrentUserProfileImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    val getCurrentUser: GetCurrentUserRecurrentUseCase,
    val getProfileImage_current: GetCurrentUserProfileImageUseCase
): ViewModel() {
    private val _state = MutableLiveData(ChatsState())
    val state: LiveData<ChatsState> = _state

    fun getUser() {
        getCurrentUser().onEach {
            when(it) {
                is Resource.Success -> {
                    _state.value = _state.value?.copy(currentUser = it.data)
                }
                is Resource.Loading -> {
                    _state.value = _state.value?.copy(currentUser = null)
                }
                is Resource.Error -> {
                    _state.value = _state.value?.copy(currentUser = null)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getUserImage() {
        viewModelScope.launch {
            getProfileImage_current().onEach {
                _state.value = _state.value?.copy(currentUserImage = it?.image)
            }.launchIn(viewModelScope)
        }
    }
}
package com.varsel.firechat.presentation.signedIn.fragments.screen_groups.bottomNav.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.common.Resource
import com.varsel.firechat.domain.use_case.current_user.GetCurrentUserRecurrentUseCase
import com.varsel.firechat.domain.use_case.profile_image.GetOtherUserProfileImageUseCase
import com.varsel.firechat.domain.use_case.profile_image.SetProfilePicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val getCurrentUserRecurrentUseCase: GetCurrentUserRecurrentUseCase,
    val getOtherUserProfileImageUseCase: GetOtherUserProfileImageUseCase,
    val setProfilePicUseCase: SetProfilePicUseCase
): ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state = _state

    init {
        getCurrentUser()
    }

    private fun getCurrentUser() {
        getCurrentUserRecurrentUseCase().onEach {
            when(it) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(currentUser = it.data, isLoading = false)
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(currentUser = null, isLoading = true)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(currentUser = null, isLoading = false)
                }
            }
        }.launchIn(viewModelScope)
    }
}
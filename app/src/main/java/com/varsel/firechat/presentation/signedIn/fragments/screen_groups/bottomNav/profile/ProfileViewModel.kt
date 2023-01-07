package com.varsel.firechat.presentation.signedIn.fragments.screen_groups.bottomNav.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.common.Resource
import com.varsel.firechat.domain.use_case.current_user.CheckServerConnectionUseCase
import com.varsel.firechat.domain.use_case.current_user.GetCurrentUserRecurrentUseCase
import com.varsel.firechat.domain.use_case.profile_image.GetCurrentUserProfileImageUseCase
import com.varsel.firechat.domain.use_case.profile_image.GetOtherUserProfileImageUseCase
import com.varsel.firechat.domain.use_case.profile_image.SetProfilePicUseCase
import com.varsel.firechat.domain.use_case.public_post.DoesPostExistUseCase
import com.varsel.firechat.domain.use_case.public_post.GetPublicPostUseCase
import com.varsel.firechat.domain.use_case.public_post.UploadPublicPostImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val getCurrentUserRecurrentUseCase: GetCurrentUserRecurrentUseCase,
    val getOtherUserProfileImageUseCase: GetOtherUserProfileImageUseCase,
    val setProfilePicUseCase: SetProfilePicUseCase,
    val getCurrentUserProfileImageUseCase: GetCurrentUserProfileImageUseCase,
    val checkServerConnection: CheckServerConnectionUseCase
): ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state = _state

    init {
        getCurrentUser()
        checkConnection()
    }

    fun checkConnection() {
        checkServerConnection().onEach {
            if(it){
                Log.d("CLEAN", "Connected to server")
                _state.value = _state.value.copy(isConnectedToServer = true)
            } else {
                Log.d("CLEAN", "Not connected to server")
                _state.value = _state.value.copy(isConnectedToServer = false)
            }
        }.launchIn(viewModelScope)
    }



    fun getProfileImage() {
        viewModelScope.launch {
            getCurrentUserProfileImageUseCase().onEach {
                if(it != null) {
                    _state.value = _state.value.copy(profileImage = it)
                } else {
                    _state.value = _state.value.copy(profileImage = null)
                }
            }.launchIn(this)
        }
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
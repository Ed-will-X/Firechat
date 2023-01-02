package com.varsel.firechat.presentation.signedIn.fragments.screens.other_profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.common.Resource
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.use_case.other_user.GetOtherUserRecurrent
import com.varsel.firechat.domain.use_case.other_user.RevokeFriendRequestUseCase
import com.varsel.firechat.domain.use_case.other_user.SendFriendRequestUseCase
import com.varsel.firechat.domain.use_case.other_user.UnfriendUserUseCase
import com.varsel.firechat.domain.use_case.profile_image.GetOtherUserProfileImageUseCase
import com.varsel.firechat.domain.use_case.profile_image.SetProfilePicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtherProfileViewModel @Inject constructor(
    val getOtherUserRecurrent: GetOtherUserRecurrent,
    val unfriendUserUseCase: UnfriendUserUseCase,
    val sendFriendRequestUseCase: SendFriendRequestUseCase,
    val revokeFriendRequestUseCase: RevokeFriendRequestUseCase,
    val getOtherUserProfileImageUseCase: GetOtherUserProfileImageUseCase,
    val setProfilePicUseCase: SetProfilePicUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OtherProfileState(null))
    val state: MutableStateFlow<OtherProfileState> = _state
    val _hasRun = MutableStateFlow(false)

    init {

    }

    fun getOtherUser(id: String) {
        _state.value = _state.value.copy(isLoading = true, user = null)
        getOtherUserRecurrent(id).onEach {
            when(it) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(user = it.data, isLoading = false)
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(user = null, isLoading = true)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(user = null, isLoading = false)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getProfileImage(user: User) {
        viewModelScope.launch {
            Log.d("CLEAN", "VIEW MODEL SCOPE LAUNCHED")
            getOtherUserProfileImageUseCase(user).onEach {
                if(it != null) {
                    _state.value = _state.value.copy(profileImage = it)
                    Log.d("CLEAN", "IMAGE NOT NULL IN VIEW MODEL")
                } else {
                    _state.value = _state.value.copy(profileImage = null)
                    Log.d("CLEAN", "IMAGE NULL IN VIEW MODEL")
                }
            }.launchIn(this)
        }
    }

    fun unfriendUser(user: User) {
        unfriendUserUseCase(user).onEach {
            if(it == Response.Success()) {
                // TODO: Show infobar
            } else {
                // TODO: Show Infobar
            }
        }.launchIn(viewModelScope)
    }

    fun sendFriendRequest(user: User) {
        sendFriendRequestUseCase(user).onEach {
            if(it == Response.Success()) {
                // TODO: Show infobar
            } else {
                // TODO: Show infobar
            }
        }.launchIn(viewModelScope)
    }

    fun revokeFriendRequest(user: User) {
        revokeFriendRequestUseCase(user).onEach {
            if(it == Response.Success()) {
                // TODO: Show infobar
            } else {
                // TODO: Show infobar
            }
        }.launchIn(viewModelScope)
    }
}
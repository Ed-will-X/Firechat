package com.varsel.firechat.presentation.signedIn.fragments.screens.other_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.use_case.OtherUserUseCase.GetOtherUserRecurrent
import com.varsel.firechat.domain.use_case.OtherUserUseCase.RevokeFriendRequestUseCase
import com.varsel.firechat.domain.use_case.OtherUserUseCase.SendFriendRequestUseCase
import com.varsel.firechat.domain.use_case.OtherUserUseCase.UnfriendUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class OtherProfileViewModel @Inject constructor(
    val getOtherUserRecurrent: GetOtherUserRecurrent,
    val unfriendUserUseCase: UnfriendUserUseCase,
    val sendFriendRequestUseCase: SendFriendRequestUseCase,
    val revokeFriendRequestUseCase: RevokeFriendRequestUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OtherProfileState(null))
    val state: MutableStateFlow<OtherProfileState> = _state

    fun getOtherUser(id: String) {
        _state.value = _state.value.copy(isLoading = true, user = null)
        getOtherUserRecurrent(id).onEach {
            _state.value = _state.value.copy(user = it, isLoading = false)
        }.launchIn(viewModelScope)
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
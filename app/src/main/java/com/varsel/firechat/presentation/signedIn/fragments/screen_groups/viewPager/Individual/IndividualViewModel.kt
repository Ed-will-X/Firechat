package com.varsel.firechat.presentation.signedIn.fragments.screen_groups.viewPager.Individual

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.common.Resource
import com.varsel.firechat.domain.use_case.current_user.GetCurrentUserRecurrentUseCase
import com.varsel.firechat.domain.use_case.message.GetChatRoomsRecurrentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class IndividualViewModel @Inject constructor(
    val getChatRoomsRecurrentUseCase: GetChatRoomsRecurrentUseCase,
    val getCurrentUserRecurrentUseCase: GetCurrentUserRecurrentUseCase
): ViewModel() {
    private val _state = MutableStateFlow<IndividualFragmentState>(IndividualFragmentState())
    val state = _state

    init {
        getUser()
        getChatRooms()
    }

    private fun getChatRooms() {
        getChatRoomsRecurrentUseCase().onEach {
            when(it) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(isLoading = false, chatRooms = it.data ?: listOf())
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true, chatRooms = listOf())
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false, chatRooms = listOf())
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun getUser() {
        getCurrentUserRecurrentUseCase().onEach {
            when(it) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(currentUser = it.data)
                }
                is Resource.Loading -> {

                }
                is Resource.Error -> {

                }
            }
        }.launchIn(viewModelScope)
    }
}
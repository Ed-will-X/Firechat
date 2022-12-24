package com.varsel.firechat.presentation.signedIn.fragments.screen_groups.viewPager.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.common.Resource
import com.varsel.firechat.domain.use_case.current_user.GetFriendsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class FriendsViewModel(
    val getFriendsUseCase: GetFriendsUseCase
): ViewModel() {
    private val _state = MutableStateFlow(FriendsFragmentState())
    val state = _state

    init {
        getFriends()
    }

    private fun getFriends() {
        getFriendsUseCase().onEach {
            when(it) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(isLoading = false, friends = it.data ?: listOf())
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true, friends = it.data ?: listOf())
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false, friends = listOf())
                }
            }
        }.launchIn(viewModelScope)
    }
}
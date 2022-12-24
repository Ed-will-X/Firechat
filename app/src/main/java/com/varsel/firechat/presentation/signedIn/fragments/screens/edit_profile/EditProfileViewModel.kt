package com.varsel.firechat.presentation.signedIn.fragments.screens.edit_profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.common.Resource
import com.varsel.firechat.common.Response
import com.varsel.firechat.domain.use_case.current_user.EditUserUseCase
import com.varsel.firechat.domain.use_case.current_user.GetCurrentUserRecurrentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    val getCurrentUserRecurrentUseCase: GetCurrentUserRecurrentUseCase,
    val editUserUseCase: EditUserUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(EditProfileState(user = null, isLoading = true))
    val state = _state


    fun getCurrentUser() {
        getCurrentUserRecurrentUseCase().onEach {
            when(it) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(user = it.data, isLoading = false)
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(user = null, isLoading = true)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(user = null, isLoading = false, errorMessage = it.message)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun editUser(key: String, value: String) {
        editUserUseCase(key, value).onEach {
            if(it == Response.Fail()) {
                // TODO: Show Error infobar
            }
        }.launchIn(viewModelScope)
    }
}
package com.varsel.firechat.presentation.signedIn.fragments.screens.add_friends

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.domain.use_case.other_user.SearchUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddFriendsViewModel @Inject constructor(
    val searchUsersUseCase: SearchUsersUseCase
): ViewModel() {
    val shouldRun = MutableLiveData<Boolean>(true)

    private val _state = MutableStateFlow(AddFriendsState())
    val state: MutableStateFlow<AddFriendsState> = _state

    private val _eventFlow = MutableSharedFlow<AddFriendsEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var searchJob: Job? = null


    fun onSearch(query: String) {
        _state.value = _state.value.copy(isFieldEmpty = query.isEmpty(), textCount = query.length)

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            delay(500L)

            val usersFlow = searchUsersUseCase(query)
            usersFlow.onEach {

                if(it.isNotEmpty()) {
                    _state.value = _state.value.copy(users = it, isLoading = false)
                } else {
                    _state.value = _state.value.copy(users = listOf(), isLoading = false)
                }
            }.launchIn(this)
        }
    }

}
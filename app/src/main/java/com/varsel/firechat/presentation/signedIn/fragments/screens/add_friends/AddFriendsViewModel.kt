package com.varsel.firechat.presentation.signedIn.fragments.screens.add_friends

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.common.Resource
import com.varsel.firechat.domain.repository.FirebaseRepository
import com.varsel.firechat.domain.use_case.current_user.GetCurrentUserRecurrentUseCase
import com.varsel.firechat.domain.use_case.other_user.SearchUsersUseCase
import com.varsel.firechat.domain.use_case.profile_image.GetOtherUserProfileImageUseCase
import com.varsel.firechat.domain.use_case.profile_image.SetProfilePicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddFriendsViewModel @Inject constructor(
    val searchUsersUseCase: SearchUsersUseCase,
    val getCurrentUserRecurrentUseCase: GetCurrentUserRecurrentUseCase,
    val getOtherUserProfileImageUseCase: GetOtherUserProfileImageUseCase,
    val setProfilePicUseCase: SetProfilePicUseCase,
    val firebase: FirebaseRepository
): ViewModel() {
    val shouldRun = MutableLiveData<Boolean>(true)

    private val _state = MutableStateFlow(AddFriendsState())
    val state: MutableStateFlow<AddFriendsState> = _state

    private val _eventFlow = MutableSharedFlow<AddFriendsEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var searchJob: Job? = null

    init {
        getCurrentUser()
    }

    fun getCurrentUser() {
        getCurrentUserRecurrentUseCase().onEach {
            when(it) {
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
package com.varsel.firechat.presentation.signedIn.fragments.screens.create_group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.common.Resource
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.use_case._util.search.SetupSearchBarUseCase
import com.varsel.firechat.domain.use_case.current_user.CheckServerConnectionUseCase
import com.varsel.firechat.domain.use_case.current_user.GetCurrentUserIdUseCase
import com.varsel.firechat.domain.use_case.current_user.GetCurrentUserRecurrentUseCase
import com.varsel.firechat.domain.use_case.current_user.GetFriendsUseCase
import com.varsel.firechat.domain.use_case.message.AppendGroupIdToUserUseCase
import com.varsel.firechat.domain.use_case.message.CreateGroupUseCase
import com.varsel.firechat.domain.use_case.profile_image.GetOtherUserProfileImageUseCase
import com.varsel.firechat.domain.use_case.profile_image.SetProfilePicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    val getCurrentUser: GetCurrentUserRecurrentUseCase,
    val getFriends: GetFriendsUseCase,
    val createGroupUseCase: CreateGroupUseCase,
    val appendGroupIdToUserUseCase: AppendGroupIdToUserUseCase,
    val getCurrentUserId: GetCurrentUserIdUseCase,
    val getOtherUserProfileImageUseCase: GetOtherUserProfileImageUseCase,
    val setProfilePicUseCase: SetProfilePicUseCase,
    val setupSearchBarUseCase: SetupSearchBarUseCase,
    val checkServerConnectionUseCase: CheckServerConnectionUseCase
) : ViewModel() {
    val hasBtnBeenClicked = MutableLiveData<Boolean>(false)
    private val _state = MutableStateFlow(CreateGroupState())
    val state = _state

    private val _friends = MutableStateFlow(listOf<User>())
    val friends = _friends

    private val _selected = MutableLiveData(arrayListOf<String>())
    val selected: LiveData<ArrayList<String>> = _selected

    init {
        getUser()
        fetchFriends()
    }

    fun select(uid: String){
        selected.value?.add(uid)
    }

    fun unselect(uid: String){
        selected.value?.remove(uid)
    }

    private fun getUser(){
        getCurrentUser().onEach {
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

    private fun fetchFriends() {
        getFriends().onEach {
            when(it) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(friends = it.data, isLoading = false)
                    _friends.value = it.data ?: listOf()
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(friends = null, isLoading = true)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(friends = null, isLoading = false)
                    _friends.value = listOf()
                }
            }
        }.launchIn(viewModelScope)
    }

    fun createGroup(group: GroupRoom, success: () -> Unit, fail: () -> Unit) {
        createGroupUseCase(group).onEach {
            when(it) {
                is Response.Success -> {
                    success()
                }
                is Response.Fail -> {
                    fail()
                }
            }
        }.launchIn(viewModelScope)
    }

    fun appendGroupIdToUser(group: GroupRoom, user: String, success: ()-> Unit = {}, fail: ()-> Unit ={}) {
        appendGroupIdToUserUseCase(group, user).onEach {
            when(it) {
                is Response.Success -> {
                    success()
                }
                is Response.Fail -> {
                    fail()
                }
            }
        }.launchIn(viewModelScope)
    }
}
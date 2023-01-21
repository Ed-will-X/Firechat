package com.varsel.firechat.presentation.signedIn.fragments.screen_groups.viewPager.Individual

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.common.Resource
import com.varsel.firechat.domain.use_case._util.message.FormatTimestampChatsPage_UseCase
import com.varsel.firechat.domain.use_case._util.message.GetLastMessage_UseCase
import com.varsel.firechat.domain.use_case._util.string.Truncate_UseCase
import com.varsel.firechat.domain.use_case._util.user.GetOtherUserId_UseCase
import com.varsel.firechat.domain.use_case.current_user.GetCurrentUserRecurrentUseCase
import com.varsel.firechat.domain.use_case.message.GetChatRoomsRecurrentUseCase
import com.varsel.firechat.domain.use_case.profile_image.GetOtherUserProfileImageUseCase
import com.varsel.firechat.domain.use_case.profile_image.SetProfilePicUseCase
import com.varsel.firechat.domain.use_case.read_receipt.FetchReceipt_UseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class IndividualViewModel @Inject constructor(
    val getChatRoomsRecurrentUseCase: GetChatRoomsRecurrentUseCase,
    val getCurrentUserRecurrentUseCase: GetCurrentUserRecurrentUseCase,
    val getOtherUserProfileImageUseCase: GetOtherUserProfileImageUseCase,
    val setProfilePicUseCase: SetProfilePicUseCase,
    val truncate: Truncate_UseCase,
    val getOtherUserId: GetOtherUserId_UseCase,
    val formatStampChatsPage: FormatTimestampChatsPage_UseCase,
    val getLastMessage: GetLastMessage_UseCase,
    val fetchReceipt: FetchReceipt_UseCase
): ViewModel() {
    private val _state = MutableStateFlow(IndividualFragmentState())
    val state = _state

    init {
        getUser()
        getChatRooms()
    }

    private fun getChatRooms() {
        _state.value = _state.value.copy(isLoading = true, chatRooms = listOf())

        getChatRoomsRecurrentUseCase().onEach {
            when(it) {
                is Resource.Success -> {
                    Log.d("CLEAN", "Count in viewModel: ${it.data?.size}")

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
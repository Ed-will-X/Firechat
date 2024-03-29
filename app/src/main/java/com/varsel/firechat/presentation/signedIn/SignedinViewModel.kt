package com.varsel.firechat.presentation.signedIn

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.common.Resource
import com.varsel.firechat.common.Response
import com.varsel.firechat.domain.use_case._util.message.FormatStampMessageDetail_UseCase
import com.varsel.firechat.domain.use_case._util.message.FormatStampMessage_UseCase
import com.varsel.firechat.domain.use_case._util.message.FormatTimestampChatsPage_UseCase
import com.varsel.firechat.domain.use_case.current_user.GetCurrentUserRecurrentUseCase
import com.varsel.firechat.domain.use_case.current_user.OpenCurrentUserCollectionStream
import com.varsel.firechat.domain.use_case.current_user.OpenFriendsUpdateStream
import com.varsel.firechat.domain.use_case.message.GetChatRoomsRecurrentUseCase
import com.varsel.firechat.domain.use_case.message.InitialiseChatRoomsStreamUseCase
import com.varsel.firechat.domain.use_case.message.InitialiseGroupRoomsStreamUseCase
import com.varsel.firechat.domain.use_case.other_user.GetOtherUserSingle
import com.varsel.firechat.domain.use_case.profile_image.GetOtherUserProfileImageUseCase
import com.varsel.firechat.domain.use_case.profile_image.SetProfilePicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SignedinViewModel @Inject constructor(
    val openCurrentUserCollectionStream: OpenCurrentUserCollectionStream,
    val getCurrentUserRecurrentUseCase: GetCurrentUserRecurrentUseCase,
    val openFriendsUpdateStream: OpenFriendsUpdateStream,
    val initialiseChatRoomsStreamUseCase: InitialiseChatRoomsStreamUseCase,
    val getChatRoomsRecurrentUseCase: GetChatRoomsRecurrentUseCase,
    val initialiseGroupRoomsStreamUseCase: InitialiseGroupRoomsStreamUseCase,
    val getOtherUserProfileImageUseCase: GetOtherUserProfileImageUseCase,
    val setProfilePicUseCase: SetProfilePicUseCase,
    val getOtherUserSingle: GetOtherUserSingle,
    val formatStampMessageDetail: FormatStampMessageDetail_UseCase
): ViewModel() {
    private val lastChatRoomCount = MutableStateFlow<Int>(-1)
    private val lastGroupRoomCount = MutableStateFlow<Int>(-1)
    val destinationId = MutableLiveData<Int?>()

    private val _signedInState = MutableStateFlow(SignedInActivityState())
    val signedInState = _signedInState

    init {
        // Opens the realtime database stream to receive current user
        openCurrentUserCollectionStream().onEach {

        }.launchIn(viewModelScope)

        // Observes the user
        getCurrentUserRecurrentUseCase().onEach {
            when(it){
                is Resource.Success -> {
                    _signedInState.value = _signedInState.value.copy(currentUser = it.data, isLoading = false)

                    if(lastChatRoomCount.value == -1 || lastChatRoomCount.value != it.data?.chatRooms?.keys?.size){
                        initialiseChatRoomsStreamUseCase(viewModelScope).onEach { it2 ->
                            when(it2) {
                                is Response.Success -> {
                                    lastChatRoomCount.value = it.data?.chatRooms?.keys?.size ?: 0
                                }
                            }
                        }.launchIn(viewModelScope)
                    }

                    if(lastGroupRoomCount.value == -1 || lastGroupRoomCount.value != it.data?.groupRooms?.keys?.size){
                        initialiseGroupRoomsStreamUseCase().onEach { it2 ->
                            when(it2) {
                                is Response.Success -> {
                                    lastGroupRoomCount.value = it.data?.groupRooms?.keys?.size ?: 0
                                }
                            }
                        }.launchIn(viewModelScope)
                    }

                    if(it.data?.friends != null) {
                        openFriendsUpdateStream(it.data.friends).onEach {

                        }.launchIn(viewModelScope)
                    }
                }
                is Resource.Loading -> {
                    _signedInState.value = _signedInState.value.copy(currentUser = null, isLoading = true)
                }
                is Resource.Error -> {
                    _signedInState.value = _signedInState.value.copy(currentUser = null, isLoading = false)
                }
            }

        }.launchIn(viewModelScope)

        getChatRoomsRecurrentUseCase().onEach {
        }.launchIn(viewModelScope)

    }

}
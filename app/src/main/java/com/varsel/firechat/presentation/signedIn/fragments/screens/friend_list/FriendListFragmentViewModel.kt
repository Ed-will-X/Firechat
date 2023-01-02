package com.varsel.firechat.presentation.viewModel

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.common.Resource
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.databinding.FragmentFriendListBinding
import com.varsel.firechat.domain.use_case._util.search.SetupSearchBarUseCase
import com.varsel.firechat.domain.use_case.current_user.GetCurrentUserRecurrentUseCase
import com.varsel.firechat.domain.use_case.current_user.GetFriendsUseCase
import com.varsel.firechat.domain.use_case.other_user.UnfriendUserUseCase
import com.varsel.firechat.domain.use_case.profile_image.GetOtherUserProfileImageUseCase
import com.varsel.firechat.domain.use_case.profile_image.SetProfilePicUseCase
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.fragments.screens.friend_list.FriendListFragment
import com.varsel.firechat.presentation.signedIn.fragments.screens.friend_list.FriendListState
import com.varsel.firechat.utils.SearchUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class SortTypes {
    companion object {
        val NEWEST = 0
        val OLDEST = 1
        val ASCENDING = 2
        val DESCENDING = 3
        val DEFAULT = 4
    }
}

@HiltViewModel
class FriendListFragmentViewModel @Inject constructor(
    val unfriendUserUseCase: UnfriendUserUseCase,
    val getFriendsUseCase: GetFriendsUseCase,
    val getCurrentUserRecurrentUseCase: GetCurrentUserRecurrentUseCase,
    val setupSearchBarUseCase: SetupSearchBarUseCase,
    val setImageUseCase: SetProfilePicUseCase,
    val getOtherUserProfileImageUseCase: GetOtherUserProfileImageUseCase
) : ViewModel() {
    val isSearchBarVisible = MutableLiveData<Boolean>(false)
    val isSortDialogOverlayOpen = MutableLiveData<Boolean>(false)
    val sortMethod = MutableLiveData<Int>(SortTypes.ASCENDING)
    val binding = MutableLiveData<FragmentFriendListBinding>()

    private val _state = MutableStateFlow(FriendListState())
    val state = _state

    init {
        getFriends()
        getCurrentUser()
    }

    fun getFriends() {
        _state.value = _state.value.copy(isLoading = true)

        getFriendsUseCase().onEach {
            when(it){
                is Resource.Success -> {
                    _state.value = _state.value.copy(isLoading = false, friends = it.data ?: listOf())
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false, friends = listOf())
                }
            }
        }.launchIn(viewModelScope)
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
        }
    }

    fun toggleSearchBarVisible(){
        isSearchBarVisible.value = !isSearchBarVisible.value!!
    }

    fun setupSearchBar(binding: FragmentFriendListBinding, parent: SignedinActivity, fragment: FriendListFragment, afterCallback: ()-> Unit, resultsCallback: (users: List<User>)-> Unit) {
        setupSearchBarUseCase(
            binding.clearText,
            binding.searchBox,
            fragment,
            binding.noFriends,
            binding.noMatch,
            binding.allFriendsRecyclerView,
            parent.firebaseViewModel.friends,
            {
                afterCallback()
            }, {
                resultsCallback(it as List<User>)
            }
        )
    }

    fun setBinding(binding: FragmentFriendListBinding){
        this.binding.value = binding
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

    fun changeSortMethod(sortType: Int){
        this.sortMethod.value = sortType
        this.isSortDialogOverlayOpen.value = false
        hideAllCheckboxes()

        if(sortType == SortTypes.ASCENDING){
            binding.value?.aToZChecked?.visibility = View.VISIBLE
        } else if(sortType == SortTypes.DESCENDING){
            binding.value?.zToAChecked?.visibility = View.VISIBLE
        } else if(sortType == SortTypes.NEWEST){
            binding.value?.newestFirstChecked?.visibility = View.VISIBLE
        } else if(sortType == SortTypes.OLDEST){
            binding.value?.oldestFirstChecked?.visibility = View.VISIBLE
        } else if(sortType == SortTypes.DEFAULT){
            binding.value?.defaultChecked?.visibility = View.VISIBLE
        }
    }

    fun hideAllCheckboxes(){
        binding.value?.aToZChecked?.visibility = View.GONE
        binding.value?.newestFirstChecked?.visibility = View.GONE
        binding.value?.oldestFirstChecked?.visibility = View.GONE
        binding.value?.zToAChecked?.visibility = View.GONE
        binding.value?.defaultChecked?.visibility = View.GONE
    }
}
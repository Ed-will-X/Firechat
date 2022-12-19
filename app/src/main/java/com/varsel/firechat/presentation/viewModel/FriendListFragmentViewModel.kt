package com.varsel.firechat.presentation.viewModel

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.varsel.firechat.databinding.FragmentFriendListBinding

class SortTypes {
    companion object {
        val NEWEST = 0
        val OLDEST = 1
        val ASCENDING = 2
        val DESCENDING = 3
        val DEFAULT = 4
    }
}

class FriendListFragmentViewModel: ViewModel() {
    val isSearchBarVisible = MutableLiveData<Boolean>(false)
    val isSortDialogOverlayOpen = MutableLiveData<Boolean>(false)
    val sortMethod = MutableLiveData<Int>(SortTypes.ASCENDING)
    val binding = MutableLiveData<FragmentFriendListBinding>()

//    fun setSearchBarVisibility(value: Boolean){
//        isSearchBarVisible.value = value
//    }

    fun toggleSearchBarVisible(){
        isSearchBarVisible.value = !isSearchBarVisible.value!!
    }



    fun setBinding(binding: FragmentFriendListBinding){
        this.binding.value = binding
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
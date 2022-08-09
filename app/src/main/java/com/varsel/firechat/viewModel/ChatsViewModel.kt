package com.varsel.firechat.viewModel

import androidx.lifecycle.ViewModel
import com.google.android.material.tabs.TabLayoutMediator
import com.varsel.firechat.databinding.FragmentChatsBinding

class ChatsViewModel: ViewModel() {

    fun setTabText(binding: FragmentChatsBinding){
        TabLayoutMediator(binding.chatsTabLayout, binding.chatsViewPager){ tab, position ->
            when(position){
                0 -> {
                    tab.text = "Messages"
                }
                1 -> {
                    tab.text = "Groups"
                }
            }
        }.attach()
    }
}
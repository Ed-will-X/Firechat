package com.varsel.firechat.presentation.signedIn.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.varsel.firechat.presentation.signedIn.fragments.screen_groups.viewPager.friends.FriendsFragment
import com.varsel.firechat.presentation.signedIn.fragments.screen_groups.viewPager.group.GroupFragment
import com.varsel.firechat.presentation.signedIn.fragments.screen_groups.viewPager.Individual.IndividualFragment

class ChatsViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        if(position == 0){
            return IndividualFragment()
        } else if(position == 1) {
            return GroupFragment()
        } else {
            return FriendsFragment()
        }
    }
}
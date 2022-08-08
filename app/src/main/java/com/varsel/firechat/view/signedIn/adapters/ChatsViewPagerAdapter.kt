package com.varsel.firechat.view.signedIn.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.varsel.firechat.view.signedIn.fragments.viewPager.GroupFragment
import com.varsel.firechat.view.signedIn.fragments.viewPager.IndividualFragment

class ChatsViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        if(position == 0){
            return IndividualFragment()
        } else {
            return GroupFragment()
        }
    }
}
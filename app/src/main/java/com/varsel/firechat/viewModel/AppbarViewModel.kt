package com.varsel.firechat.viewModel

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentChatsBinding

enum class AppbarTag {
    CHATS,
    PROFILE,
    SETTINGS,
    ADD_FRIENDS
}

class AppbarViewModel: ViewModel() {
    val selectedPage = MutableLiveData<AppbarTag>()

    fun setPage(tag: AppbarTag){
        if (tag == AppbarTag.CHATS){
            selectedPage.value = AppbarTag.CHATS
            Log.d("PAGE", "CHATS PAGE")
        }
        if(tag == AppbarTag.PROFILE){
            selectedPage.value = AppbarTag.PROFILE
            Log.d("PAGE", "PROFILE PAGE")
        }
        if(tag == AppbarTag.SETTINGS){
            selectedPage.value = AppbarTag.SETTINGS
            Log.d("PAGE", "inside settings")
        }
        if(tag == AppbarTag.ADD_FRIENDS){
            selectedPage.value = AppbarTag.ADD_FRIENDS
            Log.d("PAGE", "ADD FRIENDS")
        }
    }

    private lateinit var appbar: AppBarLayout
    private lateinit var icon2: ImageView
    private lateinit var icon3: ImageView
    private lateinit var largeText: TextView
    private lateinit var tab: TabLayout
    private lateinit var bottomNav: BottomNavigationView

    fun setNavProps(activity: FragmentActivity?, context: Context?, view: View){
        findViews(activity)
        resetViews(activity)
        if(selectedPage.value == AppbarTag.CHATS){
            bottomNav.visibility = View.VISIBLE
            icon2.visibility = View.VISIBLE
            icon3.visibility = View.VISIBLE
            largeText.visibility = View.VISIBLE
            icon2.setImageResource(R.drawable.ic_add_new_chat)
            icon3.setImageResource(R.drawable.ic_search)
            largeText.text = context?.getString(R.string.all_chats)
            tab.visibility = View.VISIBLE
            icon2.setOnClickListener {
                try {
                    view.findNavController().navigate(R.id.action_chatsFragment_to_addFriends)
                } catch(e: Exception){
                    Log.e("LLL", "$e")
                }
            }
        }
        if(selectedPage.value == AppbarTag.PROFILE){
            largeText.text = context?.getString(R.string.my_profile)
            icon3.visibility = View.VISIBLE
            icon3.setImageResource(R.drawable.ic_more_vert)
            bottomNav.visibility = View.VISIBLE
        }

        if(selectedPage.value == AppbarTag.SETTINGS){
            largeText.text = context?.getString(R.string.settings)
            bottomNav.visibility = View.VISIBLE
        }

        if (selectedPage.value == AppbarTag.ADD_FRIENDS){
            icon3.visibility = View.VISIBLE
            icon3.setImageResource(R.drawable.ic_filter)
            appbar.visibility = View.GONE
        }
    }

    private fun findViews(activity: FragmentActivity?){
        appbar = activity?.findViewById<AppBarLayout>(R.id.appbar)!!
        icon2 = activity.findViewById<ImageView>(R.id.icon_2)
        icon3 = activity.findViewById<ImageView>(R.id.icon_3)
        largeText = activity.findViewById<TextView>(R.id.large_text)
        tab = activity.findViewById(R.id.chats_tab_layout)
        bottomNav = activity.findViewById(R.id.bottom_nav_view)
    }

    private fun resetViews(activity: FragmentActivity?){
        icon2.visibility = View.GONE
        icon3.visibility = View.GONE
        tab.visibility = View.GONE
        appbar.visibility = View.VISIBLE
        bottomNav.visibility = View.GONE
    }

    fun setTabText(tabLayout: TabLayout, viewPager2: ViewPager2){
        TabLayoutMediator(tabLayout, viewPager2){ tab, position ->
            when(position){
                0 -> {
                    tab.text = "Messages"
                }
                1 -> {
                    tab.text = "Groups"
                }
                2 -> {
                    tab.text = "Friends"
                }
            }
        }.attach()
    }
}
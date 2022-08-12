package com.varsel.firechat.viewModel

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentChatsBinding

enum class AppbarTag {
    CHATS,
    PROFILE,
    SETTINGS
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
    }

    private lateinit var appbar: AppBarLayout
    private lateinit var icon1: ImageView
    private lateinit var icon2: ImageView
    private lateinit var icon3: ImageView
    private lateinit var largeText: TextView
    private lateinit var tab: TabLayout

    fun setNavProps(activity: FragmentActivity?, context: Context?){
        findViews(activity)
        resetViews(activity)
        if(selectedPage.value == AppbarTag.CHATS){
            icon2.visibility = View.VISIBLE
            icon3.visibility = View.VISIBLE
            largeText.visibility = View.VISIBLE
            icon2.setImageResource(R.drawable.ic_add_new_chat)
            icon3.setImageResource(R.drawable.ic_search)
            largeText.text = context?.getString(R.string.all_chats)
            tab.visibility = View.VISIBLE
        }
        if(selectedPage.value == AppbarTag.PROFILE){
            largeText.text = context?.getString(R.string.my_profile)
            icon3.visibility = View.VISIBLE
            icon3.setImageResource(R.drawable.ic_more_vert)
        }

        if(selectedPage.value == AppbarTag.SETTINGS){
            largeText.text = context?.getString(R.string.settings)
        }
    }

    private fun findViews(activity: FragmentActivity?){
        appbar = activity?.findViewById<AppBarLayout>(R.id.appbar)!!
        icon1 = activity?.findViewById<ImageView>(R.id.icon_1)
        icon2 = activity.findViewById<ImageView>(R.id.icon_2)
        icon3 = activity.findViewById<ImageView>(R.id.icon_3)
        largeText = activity.findViewById<TextView>(R.id.large_text)
        tab = activity.findViewById(R.id.chats_tab_layout)
    }

    private fun resetViews(activity: FragmentActivity?){
        icon1.visibility = View.GONE
        icon2.visibility = View.GONE
        icon3.visibility = View.GONE
        tab.visibility = View.GONE

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
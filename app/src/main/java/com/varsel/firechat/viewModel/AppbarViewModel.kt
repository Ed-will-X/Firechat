package com.varsel.firechat.viewModel

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.google.android.material.appbar.AppBarLayout
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentProfileBinding

enum class AppbarTag {
    CHATS,
    PROFILE
}

class AppbarViewModel: ViewModel() {
    val selectedPage = MutableLiveData<AppbarTag>()
    private var appbarHeightDefault: Int? = null
    private var appbarHeightExtended: Int? = null

    fun getAppbarDefault(value: Int?){
        appbarHeightDefault = value
    }

    fun getAppbarExtended(value: Int?){
        appbarHeightExtended = value
    }

    fun setPage(tag: AppbarTag){
        if (tag == AppbarTag.CHATS){
            selectedPage.value = AppbarTag.CHATS
            Log.d("PAGE", "CHATS PAGE")
        }
        if(tag == AppbarTag.PROFILE){
            selectedPage.value = AppbarTag.PROFILE
            Log.d("PAGE", "PROFILE PAGE")
        }
    }

    private lateinit var appbar: AppBarLayout
    private lateinit var icon1: ImageView
    private lateinit var icon2: ImageView
    private lateinit var icon3: ImageView
    private lateinit var largeText: TextView

    fun setProfile(activity: FragmentActivity?, context: Context?){
        findViews(activity)
        resetViews(activity)
        if(selectedPage.value == AppbarTag.CHATS){
            icon1.visibility = View.GONE
            icon2.visibility = View.VISIBLE
            icon3.visibility = View.VISIBLE
            largeText.visibility = View.VISIBLE
            icon2.setImageResource(R.drawable.ic_add_new_chat)
            icon3.setImageResource(R.drawable.ic_search)
            largeText.text = context?.getString(R.string.all_chats)
        }
        if(selectedPage.value == AppbarTag.PROFILE){
            largeText.text = context?.getString(R.string.my_profile)
            icon3.visibility = View.VISIBLE
            icon3.setImageResource(R.drawable.ic_more_vert)
        }
    }

    private fun findViews(activity: FragmentActivity?){
        appbar = activity?.findViewById<AppBarLayout>(R.id.appbar)!!
        icon1 = activity?.findViewById<ImageView>(R.id.icon_1)
        icon2 = activity.findViewById<ImageView>(R.id.icon_2)
        icon3 = activity.findViewById<ImageView>(R.id.icon_3)
        largeText = activity.findViewById<TextView>(R.id.large_text)
    }

    private fun resetViews(activity: FragmentActivity?){
        var defaultHeight: Int = appbarHeightDefault!!
        icon1.visibility = View.GONE
        icon2.visibility = View.GONE
        icon3.visibility = View.GONE
        appbar.layoutParams = LinearLayout.LayoutParams(Toolbar.LayoutParams.MATCH_PARENT, defaultHeight)
    }
}
package com.varsel.firechat.view.signedIn.fragments.bottomNav

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentChatsBinding
import com.varsel.firechat.view.signedIn.adapters.ChatsViewPagerAdapter
import com.varsel.firechat.viewModel.AppbarTag
import com.varsel.firechat.viewModel.AppbarViewModel
import com.varsel.firechat.viewModel.ChatsViewModel

class ChatsFragment : Fragment() {
    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ChatsViewPagerAdapter
    private val appbarViewModel: AppbarViewModel by activityViewModels()
    private lateinit var chatsViewModel: ChatsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        val view = binding.root

        // set page in VM
        appbarViewModel.setPage(AppbarTag.CHATS)
        appbarViewModel.setNavProps(activity, context)


        chatsViewModel = ViewModelProvider(this).get(ChatsViewModel::class.java)

        adapter = ChatsViewPagerAdapter(childFragmentManager, lifecycle)
        binding.chatsViewPager.adapter = adapter

        val chatsViewPager = view.findViewById<ViewPager2>(R.id.chats_view_pager)
        val chatsTab = activity?.findViewById<TabLayout>(R.id.chats_tab_layout)
        if (chatsTab != null) {
            appbarViewModel.setTabText(chatsTab, chatsViewPager)
        }

//        val icon = activity?.findViewById<ImageView>(R.id.icon_1)
//        icon?.visibility = View.GONE

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
package com.varsel.firechat.view.signedIn.fragments.bottomNav

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentChatsBinding
import com.varsel.firechat.view.signedIn.adapters.ChatsViewPagerAdapter
import com.varsel.firechat.viewModel.AppbarTag
import com.varsel.firechat.viewModel.AppbarViewModel
import com.varsel.firechat.viewModel.ChatsViewModel
import com.varsel.firechat.viewModel.FirebaseViewModel

class ChatsFragment : Fragment() {
    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ChatsViewPagerAdapter
    private val appbarViewModel: AppbarViewModel by activityViewModels()
    private lateinit var chatsViewModel: ChatsViewModel
    private val firebaseViewModel: FirebaseViewModel by activityViewModels()
    private lateinit var mDbRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        val view = binding.root

        // set page in VM
        appbarViewModel.setPage(AppbarTag.CHATS)
        appbarViewModel.setNavProps(activity, context, view)

        chatsViewModel = ViewModelProvider(this).get(ChatsViewModel::class.java)

        adapter = ChatsViewPagerAdapter(childFragmentManager, lifecycle)
        binding.chatsViewPager.adapter = adapter

        val chatsViewPager = view.findViewById<ViewPager2>(R.id.chats_view_pager)
        val chatsTab = activity?.findViewById<TabLayout>(R.id.chats_tab_layout)
        if (chatsTab != null) {
            appbarViewModel.setTabText(chatsTab, chatsViewPager)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
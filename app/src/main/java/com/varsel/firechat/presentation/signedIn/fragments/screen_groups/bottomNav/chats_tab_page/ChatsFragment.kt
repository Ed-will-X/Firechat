package com.varsel.firechat.presentation.signedIn.fragments.screen_groups.bottomNav.chats_tab_page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.varsel.firechat.R
import com.varsel.firechat.data.local.Notification.MessageType
import com.varsel.firechat.data.local.Notification.NotificationItem
import com.varsel.firechat.data.local.Notification.mock_notifications
import com.varsel.firechat.databinding.FragmentChatsBinding
import com.varsel.firechat.domain.use_case._util.notification.SendNotificationMessage_UseCase
import com.varsel.firechat.domain.use_case._util.status_bar.ChangeStatusBarColor_UseCase
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.adapters.ChatsViewPagerAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatsFragment : Fragment() {
    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ChatsViewPagerAdapter
    private lateinit var chatsViewModel: ChatsViewModel
    private lateinit var parent: SignedinActivity

    @Inject
    lateinit var changeStatusBarColor: ChangeStatusBarColor_UseCase

    @Inject
    lateinit var sendNotification_message: SendNotificationMessage_UseCase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        val view = binding.root

        parent = activity as SignedinActivity
        changeStatusBarColor(R.color.light_blue, false, parent)

        chatsViewModel = ViewModelProvider(this).get(ChatsViewModel::class.java)
        parent.hideKeyboard()

        chatsViewModel.getUser()
        chatsViewModel.getUserImage()

        adapter = ChatsViewPagerAdapter(childFragmentManager, lifecycle)
        binding.chatsViewPager.adapter = adapter

        val chatsViewPager = view.findViewById<ViewPager2>(R.id.chats_view_pager)
        val chatsTab = view.findViewById<TabLayout>(R.id.chats_tab_layout)

        if (chatsTab != null) {
            setTabText(chatsTab, chatsViewPager)
        }

        binding.addNewChat.setOnClickListener {
            mock_notifications.add(NotificationItem("Elsa Addams", "Heyyyy sweetie ❤️", chatsViewModel.state.value?.currentUserImage, "28394", MessageType.Text, "14h"))

            sendNotification_message(mock_notifications, parent)
            view.findNavController().navigate(R.id.action_chatsFragment_to_addFriends)
        }

        binding.searchChats.setOnClickListener {
            navigateToSearch()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun navigateToSearch(){
        val action = ChatsFragmentDirections.actionChatsFragmentToChatsSearchFragment()
        findNavController().navigate(action)
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

    // TODO: Modularise maybe
    fun swipeToFriends(){
        binding.chatsViewPager.apply {
            beginFakeDrag()
            fakeDragBy(-2000f)
            endFakeDrag()
        }
    }
}
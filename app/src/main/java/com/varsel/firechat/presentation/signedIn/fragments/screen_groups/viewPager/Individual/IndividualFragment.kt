package com.varsel.firechat.presentation.signedIn.fragments.screen_groups.viewPager.Individual

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.varsel.firechat.databinding.FragmentIndividualBinding
import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.MessageUtils
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.adapters.ChatListAdapter
import com.varsel.firechat.presentation.signedIn.fragments.screen_groups.bottomNav.chats_tab_page.ChatsFragment
import com.varsel.firechat.presentation.signedIn.fragments.screen_groups.bottomNav.chats_tab_page.ChatsFragmentDirections
import com.varsel.firechat.utils.ExtensionFunctions.Companion.collectLatestLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint
import java.lang.IllegalArgumentException

// TODO: Fix bugs, make sure:
    // TODO: The recycler view is populated immediately data loads
    // TODO: The recycler view is populated correctly when there is a new message
    // TODO: The recycler view is populated correctly when there is a new chat room
// TODO: Switch recycler view's mode of data population.

@AndroidEntryPoint
class IndividualFragment : Fragment() {
    private var _binding: FragmentIndividualBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private lateinit var viewModel: IndividualViewModel
    private lateinit var chatListAdapter: ChatListAdapter

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIndividualBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        parent = activity as SignedinActivity
        viewModel = ViewModelProvider(this).get(IndividualViewModel::class.java)
        collectState()

        toggleShimmerVisibility(null)
        setupChatListAdapter()

//        parent.firebaseViewModel.chatRooms.observe(viewLifecycleOwner, Observer {
//            val sorted = MessageUtils.sortChats(it as MutableList<ChatRoom>)
//
//            toggleRecyclerViewVisibility(it)
//            chatListAdapter.submitList(sorted)
//            chatListAdapter.notifyDataSetChanged()
//        })

//        parent.firebaseViewModel.currentUser.observe(viewLifecycleOwner, Observer {
//            toggleShimmerVisibility(it)
//            if(it != null){
//                binding.chatPeopleClickable.setOnClickListener {
//                    swipeToFriends()
//                }
//            }
//        })

        return view
    }

    private fun collectState() {
        collectLatestLifecycleFlow(viewModel.state) {
            val sorted = MessageUtils.sortChats(it.chatRooms)

            toggleRecyclerViewVisibility(it.chatRooms)
            chatListAdapter.submitList(sorted)
            chatListAdapter.notifyDataSetChanged()

            toggleShimmerVisibility(it.currentUser)
            if(it.currentUser != null){
                binding.chatPeopleClickable.setOnClickListener {
                    swipeToFriends()
                }
            }
        }
    }

    private fun setupChatListAdapter() {
        // adapter
        chatListAdapter = ChatListAdapter(parent, { userId, chatRoomId, user, base64 ->
            navigateToChatPage(chatRoomId, user, base64)
        }, { profileImage, user ->
            ImageUtils.displayProfilePicture(profileImage, user, parent)
        }, {
//            setUnreadIndicator(it)

        })

        binding.individualChatsRecyclerView.adapter = chatListAdapter
    }

    private fun navigateToChatPage(chatRoomId: String, user: User, base64: String?) {
        try {
            val action = ChatsFragmentDirections.actionChatsFragmentToChatPageFragment(chatRoomId, user.userUID)

            view?.findNavController()?.navigate(action)

            parent.profileImageViewModel.selectedOtherUserProfilePicChat.value = base64
            parent.firebaseViewModel.selectedChatRoomUser.value = user
        } catch (e: IllegalArgumentException){

        }
    }

    private fun setUnreadIndicator(unreadRooms: MutableMap<String, ChatRoom>){
        // TODO: Set unread indicator in Tab Layout

//        for(i in unreadRooms){
//            Log.d("LLL", "${UserUtils.getOtherUserId(i.participants!!, parent)} in unreads")
//        }
    }

    private fun toggleRecyclerViewVisibility(chats: List<ChatRoom>){
        if(chats.isNotEmpty()){
            binding.individualChatsRecyclerView.visibility = View.VISIBLE
            binding.noChats.visibility = View.GONE
        } else {
            binding.individualChatsRecyclerView.visibility = View.GONE
            binding.noChats.visibility = View.VISIBLE
        }
    }

    private fun toggleShimmerVisibility(currentUser: User?){
        if(currentUser != null){
            binding.shimmerMessages.visibility = View.GONE
        } else {
            binding.shimmerMessages.visibility = View.VISIBLE
        }
    }

    private fun swipeToFriends(){
        val chatsFragment = parentFragment as ChatsFragment

        chatsFragment.swipeToFriends()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
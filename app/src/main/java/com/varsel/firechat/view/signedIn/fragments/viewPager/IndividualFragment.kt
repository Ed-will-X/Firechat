package com.varsel.firechat.view.signedIn.fragments.viewPager

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.varsel.firechat.databinding.FragmentIndividualBinding
import com.varsel.firechat.model.Chat.ChatRoom
import com.varsel.firechat.model.User.User
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.MessageUtils
import com.varsel.firechat.utils.UserUtils
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedIn.adapters.ChatListAdapter
import com.varsel.firechat.view.signedIn.fragments.bottomNav.ChatsFragment
import com.varsel.firechat.view.signedIn.fragments.bottomNav.ChatsFragmentDirections

class IndividualFragment : Fragment() {
    private var _binding: FragmentIndividualBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIndividualBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        parent = activity as SignedinActivity

        toggleShimmerVisibility(null)

        // adapter
        val chatListAdapter = ChatListAdapter(parent, { userId, chatRoomId, user, base64 ->
            parent.profileImageViewModel.selectedOtherUserProfilePicChat.value = base64
            parent.firebaseViewModel.selectedChatRoomUser.value = user
            val action = ChatsFragmentDirections.actionChatsFragmentToChatPageFragment(chatRoomId, userId)
            view.findNavController().navigate(action)
        }, { profileImage, user ->
            ImageUtils.displayProfilePicture(profileImage, user, parent)
        }, {
            Log.d("LLL", "${it.count()}")
//            setUnreadIndicator(it)

        })

        binding.individualChatsRecyclerView.adapter = chatListAdapter

        // TODO: Add a second observer for the chat rooms
        parent.firebaseViewModel.chatRooms.observe(viewLifecycleOwner, Observer {
            val sorted = MessageUtils.sortChats(it as MutableList<ChatRoom>)

            toggleRecyclerViewVisibility(it)
            chatListAdapter.submitList(sorted)
            chatListAdapter.notifyDataSetChanged()
        })

        parent.firebaseViewModel.currentUser.observe(viewLifecycleOwner, Observer {
            toggleShimmerVisibility(it)
            if(it != null){
                binding.chatPeopleClickable.setOnClickListener {
                    swipeToFriends()
                }
            }
        })

        return view
    }

    private fun setUnreadIndicator(unreadRooms: MutableMap<String, ChatRoom>){
        // TODO: Set unread indicator in Tab Layout
        Log.d("LLL", "---------------------------------------------------")
        Log.d("LLL", "Unread count for ${parent.firebaseViewModel.currentUser.value?.name}: ${unreadRooms.count()}")

//        for(i in unreadRooms){
//            Log.d("LLL", "${UserUtils.getOtherUserId(i.participants!!, parent)} in unreads")
//        }
    }

    private fun toggleRecyclerViewVisibility(chats: MutableList<ChatRoom?>){
        val currentUser = parent.firebaseViewModel.currentUser.value
        if(chats.isNotEmpty() && currentUser != null){
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
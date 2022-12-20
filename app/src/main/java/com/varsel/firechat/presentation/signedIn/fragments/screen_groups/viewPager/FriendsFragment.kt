package com.varsel.firechat.presentation.signedIn.fragments.screen_groups.viewPager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.Observer
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentFriendsBinding
import com.varsel.firechat.data.local.User.UserEntity
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.adapters.FriendsAdapter
import com.varsel.firechat.presentation.signedIn.fragments.screen_groups.bottomNav.chats_tab_page.ChatsFragmentDirections
import java.lang.IllegalArgumentException

class FriendsFragment : Fragment() {
    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private lateinit var currentChatRoomId: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        val view = binding.root

        parent = activity as SignedinActivity

        postponeTransition()


        binding.addFriendsClickable.setOnClickListener {
            view.findNavController().navigate(R.id.action_chatsFragment_to_addFriends)
        }

        val friendsAdapter = FriendsAdapter(parent, { user, base64 ->
            navigateToProfile(user.userUID, user, base64)


        },{ user, base64 ->
            parent.profileImageViewModel.selectedOtherUserProfilePicChat.value = base64
            parent.firebaseViewModel.selectedChatRoomUser.value = user
            navigateToChats(user.userUID)
        }, { profileImage, user ->
            ImageUtils.displayProfilePicture(profileImage, user, parent)
        })
        binding.friendsRecyclerView.adapter = friendsAdapter

        parent.firebaseViewModel.friends.observe(viewLifecycleOwner, Observer {
            toggleVisibility(it)
        })

        parent.firebaseViewModel.friends.observe(viewLifecycleOwner, Observer {
            toggleShimmerVisibility(it)
            if (it != null){
                friendsAdapter.submitList(it)
            } else {
                friendsAdapter.submitList(arrayListOf<UserEntity>())
            }
        })

        return view
    }

    private fun postponeTransition(){
        postponeEnterTransition()
        view?.doOnPreDraw { startPostponedEnterTransition() }
    }

    private fun toggleVisibility(it: List<UserEntity?>){
        val currentUser = parent.firebaseViewModel.currentUser.value
        if(it.isNotEmpty() && currentUser != null){
            binding.noFriends.visibility = View.GONE
            binding.friendsRecyclerView.visibility = View.VISIBLE
        } else {
            binding.noFriends.visibility = View.VISIBLE
            binding.friendsRecyclerView.visibility = View.GONE
        }
    }

    private fun navigateToProfile(id: String, user: UserEntity, base64: String?){
        try {
            val action = ChatsFragmentDirections.actionChatsFragmentToOtherProfileFragment(id)
            binding.root.findNavController().navigate(action)

            parent.firebaseViewModel.selectedUser.value = user
            parent.profileImageViewModel.selectedOtherUserProfilePic.value = base64
        } catch (e: IllegalArgumentException) {}
    }

    private fun navigateToChats(userId: String){
        var action: NavDirections
        if(parent.signedinViewModel.determineChatroom(userId, parent.firebaseViewModel.chatRooms.value)){
            action = ChatsFragmentDirections.actionChatsFragmentToChatPageFragment(parent.signedinViewModel.currentChatRoomId.value, userId)
        }else {
            action = ChatsFragmentDirections.actionChatsFragmentToChatPageFragment(null, userId)
        }
        binding.root.findNavController().navigate(action)
    }

    private fun toggleShimmerVisibility(friends: List<UserEntity?>){
        if(friends != null){
            binding.shimmerFriends.visibility = View.GONE
        } else {
            binding.shimmerFriends.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }
}
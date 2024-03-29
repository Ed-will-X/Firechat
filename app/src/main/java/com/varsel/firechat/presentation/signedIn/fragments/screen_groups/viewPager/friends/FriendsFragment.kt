package com.varsel.firechat.presentation.signedIn.fragments.screen_groups.viewPager.friends

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentFriendsBinding
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.use_case.profile_image.DisplayProfileImage
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.adapters.FriendsAdapter
import com.varsel.firechat.presentation.signedIn.fragments.screen_groups.bottomNav.chats_tab_page.ChatsFragmentDirections
import com.varsel.firechat.common._utils.ExtensionFunctions.Companion.collectLatestLifecycleFlow
import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.domain.use_case.chat_room.DoesChatRoomExist_UseCase
import com.varsel.firechat.domain.use_case.chat_room.FindChatRoom_UseCase
import com.varsel.firechat.domain.use_case.message.GetChatRoomsRecurrentUseCase
import dagger.hilt.android.AndroidEntryPoint
import java.lang.IllegalArgumentException
import javax.inject.Inject

@AndroidEntryPoint
class FriendsFragment : Fragment() {
    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private lateinit var currentChatRoomId: String
    private lateinit var viewModel: FriendsViewModel
    private lateinit var friendsAdapter: FriendsAdapter

    @Inject
    lateinit var displayProfileImage: DisplayProfileImage

    @Inject
    lateinit var getChatRooms: GetChatRoomsRecurrentUseCase

    @Inject
    lateinit var determineChatRoom: DoesChatRoomExist_UseCase

    @Inject
    lateinit var findChatRoom: FindChatRoom_UseCase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        val view = binding.root

        viewModel = ViewModelProvider(this).get(FriendsViewModel::class.java)
        parent = activity as SignedinActivity

        postponeTransition()
        setupAdapter()
        collectFlow()


        binding.addFriendsClickable.setOnClickListener {
            view.findNavController().navigate(R.id.action_chatsFragment_to_addFriends)
        }

        return view
    }

    private fun collectFlow() {
        collectLatestLifecycleFlow(viewModel.state) {
            toggleVisibility(it.friends)
            toggleShimmerVisibility(it.isLoading)
            friendsAdapter.submitList(it.friends)
        }
    }

    private fun setupAdapter() {
        friendsAdapter = FriendsAdapter(parent, viewModel, this, { user, base64 ->
            navigateToProfile(user.userUID, user, base64)
        },{ user, base64 ->
            navigateToChats(user.userUID)
        }, { profileImage, user ->
            displayProfileImage(profileImage, user, parent)
        })
        binding.friendsRecyclerView.adapter = friendsAdapter
    }

    private fun postponeTransition(){
        postponeEnterTransition()
        view?.doOnPreDraw { startPostponedEnterTransition() }
    }

    private fun toggleVisibility(it: List<User>){
        if(it.isNotEmpty()){
            binding.noFriends.visibility = View.GONE
            binding.friendsRecyclerView.visibility = View.VISIBLE
        } else {
            binding.noFriends.visibility = View.VISIBLE
            binding.friendsRecyclerView.visibility = View.GONE
        }
    }

    private fun navigateToProfile(id: String, user: User, base64: String?){
        try {
            val action = ChatsFragmentDirections.actionChatsFragmentToOtherProfileFragment(id)
            binding.root.findNavController().navigate(action)

        } catch (e: IllegalArgumentException) {}
    }

    private fun navigateToChats(userId: String){
        var action: NavDirections
        if(determineChatRoom(userId, getChatRooms().value.data?.toList() ?: listOf<ChatRoom>())){
            val chatRoom = findChatRoom(userId, getChatRooms().value.data?.toList() ?: listOf<ChatRoom>())
            action = ChatsFragmentDirections.actionChatsFragmentToChatPageFragment(chatRoom?.roomUID, userId)
        }else {
            action = ChatsFragmentDirections.actionChatsFragmentToChatPageFragment(null, userId)
        }
        binding.root.findNavController().navigate(action)
    }

    private fun toggleShimmerVisibility(isLoading: Boolean){
        if(!isLoading){
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
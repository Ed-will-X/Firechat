package com.varsel.firechat.presentation.signedIn.fragments.screen_groups.viewPager.group

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentGroupBinding
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.use_case.chat_image.DisplayGroupImage_UseCase
import com.varsel.firechat.utils.MessageUtils
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.adapters.GroupChatsListAdapter
import com.varsel.firechat.presentation.signedIn.fragments.screen_groups.bottomNav.chats_tab_page.ChatsFragmentDirections
import com.varsel.firechat.utils.ExtensionFunctions.Companion.collectLatestLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint
import java.lang.IllegalArgumentException
import javax.inject.Inject

@AndroidEntryPoint
class GroupFragment : Fragment() {
    private var _binding: FragmentGroupBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private lateinit var adapter: GroupChatsListAdapter
    lateinit var viewModel: GroupViewModel

    @Inject
    lateinit var displayGroupImage: DisplayGroupImage_UseCase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGroupBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        parent = activity as SignedinActivity
        viewModel = ViewModelProvider(this).get(GroupViewModel::class.java)
        collectState()

        adapter = GroupChatsListAdapter(parent, requireContext(), this, this, viewModel, {
            navigateToCreateGroup()
        }, { id, image ->
            navigateToGroupChatPage(id, image)

        }, { groupImage, group ->
            displayGroupImage(groupImage, group, parent)
        })

        binding.groupChatsRecyclerView.adapter = adapter

        return view
    }

    private fun collectState() {
        collectLatestLifecycleFlow(viewModel.state) {
            submitListToAdapter(it.groupRooms)
            toggleVisibility(it.groupRooms)
            toggleShimmerVisibility(it.currentUser)
            if(it.currentUser != null){
                binding.createGroupClickable.setOnClickListener {
                    navigateToCreateGroup()
                }
            }
        }
    }

    fun navigateToCreateGroup(){
        try {
            view?.findNavController()?.navigate(R.id.action_chatsFragment_to_createGroupFragment)
        } catch (e: IllegalArgumentException) {}
    }

    private fun toggleShimmerVisibility(currentUser: User?){
        if(currentUser != null){
            binding.shimmerGroup.visibility = View.GONE
        } else {
            binding.shimmerGroup.visibility = View.VISIBLE
        }
    }

    fun navigateToGroupChatPage(groupId: String, image: ProfileImage?){
        try {
            val action = ChatsFragmentDirections.actionChatsFragmentToGroupChatPageFragment(groupId)
            view?.findNavController()?.navigate(action)
            parent.profileImageViewModel.selectedGroupImage.value = image
        } catch (e: IllegalArgumentException){ }
    }

    // TODO: Fix potential bug
    fun submitListToAdapter(groups: List<GroupRoom>?){
        val list = mutableListOf<GroupRoom>()
        if (groups != null) {
            list.addAll(groups)
        }

        val sorted = MessageUtils.sortChats(list)
        sorted?.add(0, GroupRoom("ADD_NEW_GROUP_CHAT", hashMapOf(), "", hashMapOf()))
        adapter.submitList(sorted as MutableList<GroupRoom>?)
        adapter.notifyDataSetChanged()
    }

    private fun toggleVisibility(groupRooms: List<GroupRoom?>){
        if(groupRooms.isNotEmpty()){
            binding.noGroups.visibility = View.GONE
            binding.groupChatsRecyclerView.visibility = View.VISIBLE
        } else {
            binding.noGroups.visibility = View.VISIBLE
            binding.groupChatsRecyclerView.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
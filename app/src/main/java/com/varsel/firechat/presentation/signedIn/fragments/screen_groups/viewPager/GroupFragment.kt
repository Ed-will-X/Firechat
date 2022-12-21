package com.varsel.firechat.presentation.signedIn.fragments.screen_groups.viewPager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentGroupBinding
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.MessageUtils
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.adapters.GroupChatsListAdapter
import com.varsel.firechat.presentation.signedIn.fragments.screen_groups.bottomNav.chats_tab_page.ChatsFragmentDirections
import java.lang.IllegalArgumentException

class GroupFragment : Fragment() {
    private var _binding: FragmentGroupBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private lateinit var adapter: GroupChatsListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGroupBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        parent = activity as SignedinActivity

        adapter = GroupChatsListAdapter(parent, requireContext(),{
            navigateToCreateGroup()
        }, { id, image ->
            navigateToGroupChatPage(id, image)

        }, { groupImage, group ->
            ImageUtils.displayGroupImage(groupImage, group, parent)
        })

        parent.firebaseViewModel.currentUser.observe(viewLifecycleOwner, Observer {
            toggleShimmerVisibility(it)
            if(it != null){
                binding.createGroupClickable.setOnClickListener {
                    navigateToCreateGroup()
                }
            }
        })

        binding.groupChatsRecyclerView.adapter = adapter

        parent.firebaseViewModel.groupRooms.observe(viewLifecycleOwner, Observer {
            toggleVisibility(it)


            submitListToAdapter(it as List<GroupRoom>?)
        })

        return view
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
        val currentUser = parent.firebaseViewModel.currentUser.value
        if(groupRooms.isNotEmpty() && currentUser != null){
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
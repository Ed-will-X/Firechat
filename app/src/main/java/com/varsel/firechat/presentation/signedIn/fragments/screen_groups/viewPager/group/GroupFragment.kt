package com.varsel.firechat.presentation.signedIn.fragments.screen_groups.viewPager.group

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.varsel.firechat.R
import com.varsel.firechat.common._utils.ExtensionFunctions.Companion.collectLatestLifecycleFlow
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.databinding.FragmentGroupBinding
import com.varsel.firechat.domain.use_case._util.message.SortChats_UseCase
import com.varsel.firechat.domain.use_case.chat_image.DisplayGroupImage_UseCase
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.adapters.GroupChatsListAdapter
import com.varsel.firechat.presentation.signedIn.fragments.screen_groups.bottomNav.chats_tab_page.ChatsFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
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

    @Inject
    lateinit var sortChats: SortChats_UseCase

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
        adjustOrientation()

        return view
    }

    private fun adjustOrientation() {
        val orientation = parent.resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // code for portrait mode
        } else {
            // code for landscape mode
            Log.d("LLL", "Landscape")
        }
    }

    private fun collectState() {
        viewModel.groupRooms.observe(viewLifecycleOwner, Observer {
            submitListToAdapter(it)
            toggleVisibility(it)
        })

        collectLatestLifecycleFlow(viewModel.state) {
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
        } catch (e: IllegalArgumentException){ }
    }

    // TODO: Fix potential bug
    fun submitListToAdapter(groups: List<GroupRoom>?){
        val list = mutableListOf<GroupRoom>()
        if (groups != null) {
            list.addAll(groups)
        }

        val sorted = sortChats(list)
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
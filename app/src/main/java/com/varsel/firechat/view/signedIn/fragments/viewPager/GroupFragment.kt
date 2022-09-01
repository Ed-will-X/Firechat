package com.varsel.firechat.view.signedIn.fragments.viewPager

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentGroupBinding
import com.varsel.firechat.model.Chat.GroupRoom
import com.varsel.firechat.utils.MessageUtils
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedIn.adapters.GroupChatsListAdapter
import com.varsel.firechat.view.signedIn.fragments.bottomNav.ChatsFragmentDirections

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

        adapter = GroupChatsListAdapter(parent.firebaseAuth,{
            navigateToCreateGroup()
        }, {
            navigateToGroupChatPage(it)
        })

        parent.firebaseViewModel.currentUser.observe(viewLifecycleOwner, Observer {
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
        view?.findNavController()?.navigate(R.id.action_chatsFragment_to_createGroupFragment)
    }

    fun navigateToGroupChatPage(groupId: String){
        val action = ChatsFragmentDirections.actionChatsFragmentToGroupChatPageFragment(groupId)

        view?.findNavController()?.navigate(action)
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
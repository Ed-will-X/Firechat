package com.varsel.firechat.view.signedIn.fragments.viewPager

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentFriendsBinding
import com.varsel.firechat.databinding.FragmentProfileBinding
import com.varsel.firechat.model.User.User
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedIn.adapters.FriendsAdapter
import com.varsel.firechat.view.signedIn.fragments.AddFriendsFragmentDirections
import com.varsel.firechat.view.signedIn.fragments.bottomNav.ChatsFragment
import com.varsel.firechat.view.signedIn.fragments.bottomNav.ChatsFragmentDirections
import com.varsel.firechat.view.signedIn.fragments.bottomNav.ProfileFragmentDirections

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

        toggleVisibility()

        binding.addFriendsClickable.setOnClickListener {
            view.findNavController().navigate(R.id.action_chatsFragment_to_addFriends)
        }

        val friendsAdapter = FriendsAdapter({
            if(it != null){
                navigateToProfile(it.userUID!!)
            }
        },{
            if(it != null){
                navigateToChats(it.userUID.toString())
            }
        })
        binding.friendsRecyclerView.adapter = friendsAdapter

        parent.firebaseViewModel.friends.observe(viewLifecycleOwner, Observer {
            if (it != null){
                friendsAdapter.submitList(it)
            } else {
                friendsAdapter.submitList(arrayListOf<User>())
            }
        })

        return view
    }

    private fun toggleVisibility(){
        parent.firebaseViewModel.friends.observe(viewLifecycleOwner, Observer {
            if(it.isNotEmpty()){
                binding.noFriends.visibility = View.GONE
                binding.friendsRecyclerView.visibility = View.VISIBLE
            } else {
                binding.noFriends.visibility = View.VISIBLE
                binding.friendsRecyclerView.visibility = View.GONE
            }
        })
    }

    private fun navigateToProfile(id: String){
        val action = ChatsFragmentDirections.actionChatsFragmentToOtherProfileFragment(id)
        binding.root.findNavController().navigate(action)
    }

    private fun navigateToChats(userId: String){
        var action: NavDirections
        if(determineChatroom(userId)){
            action = ChatsFragmentDirections.actionChatsFragmentToChatPageFragment(currentChatRoomId, userId)
        }else {
            action = ChatsFragmentDirections.actionChatsFragmentToChatPageFragment(null, userId)
        }
        binding.root.findNavController().navigate(action)
    }

    private fun determineChatroom(userId: String): Boolean{
        // TODO: Fix potential null pointer exception
        val chatRooms = parent.firebaseViewModel.chatRooms.value!!
        var contains: Boolean = false
        for(i in chatRooms){
            if(i!!.participants!!.contains(userId)){
                contains = true
                currentChatRoomId = i.roomUID.toString()
            }
        }
        return contains
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }
}
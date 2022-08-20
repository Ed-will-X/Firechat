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
import com.varsel.firechat.R
import com.varsel.firechat.databinding.DialogLogoutBinding
import com.varsel.firechat.databinding.FragmentIndividualBinding
import com.varsel.firechat.model.Chat.ChatRoom
import com.varsel.firechat.model.User.User
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedIn.adapters.ChatListAdapter
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

        // adapter
        val chatListAdapter = ChatListAdapter({ userId, holder ->
            getUser(userId) {
                holder.name.text = it.name
            }
        }, parent.firebaseAuth, { userId, chatRoomId ->
            val action = ChatsFragmentDirections.actionChatsFragmentToChatPageFragment(chatRoomId, userId)
            view.findNavController().navigate(action)
        }, {})

        binding.individualChatsRecyclerView.adapter = chatListAdapter

        parent.firebaseViewModel.chatRooms.observe(viewLifecycleOwner, Observer {
            // TODO: Fix state issues and crash issues
            chatListAdapter.submitList(it)
            chatListAdapter.notifyDataSetChanged()
        })

        return view
    }

    private fun getUser(id: String, afterCallback: (user: User)-> Unit) {
        lateinit var user: User
        parent.firebaseViewModel.getUserSingle(id, parent.mDbRef, {
            if (it != null) {
                user = it
            }
        }, {
            afterCallback(user)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
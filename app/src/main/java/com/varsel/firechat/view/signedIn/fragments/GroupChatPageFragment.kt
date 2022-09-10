package com.varsel.firechat.view.signedIn.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.varsel.firechat.databinding.FragmentGroupChatPageBinding
import com.varsel.firechat.model.User.User
import com.varsel.firechat.model.message.Message
import com.varsel.firechat.model.message.MessageType
import com.varsel.firechat.utils.MessageUtils
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedIn.adapters.ChatPageType
import com.varsel.firechat.view.signedIn.adapters.MessageListAdapter

class GroupChatPageFragment : Fragment() {
    private var _binding: FragmentGroupChatPageBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private lateinit var roomId: String
    private lateinit var messageAdapter: MessageListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGroupChatPageBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        parent = activity as SignedinActivity
        roomId = GroupChatPageFragmentArgs.fromBundle(requireArguments()).groupRoomId

        getChatRoom()
        getMessages()

        messageAdapter = MessageListAdapter(parent.firebaseAuth, this, ChatPageType.GROUP)
        binding.messagesRecyclerView.adapter = messageAdapter

        binding.sendMessageBtn.setOnClickListener {
            sendMessage()
            clearEditText()
        }

        binding.chatBackButton.setOnClickListener {
            popNavigation()
        }

        binding.appbar.setOnClickListener {
            navigateToGroupChatDetail(roomId)
        }

        return view
    }

    private fun navigateToGroupChatDetail(roomId: String){
        val action = GroupChatPageFragmentDirections.actionGroupChatPageFragmentToGroupChatDetailFragment(roomId)
        view?.findNavController()?.navigate(action)
    }

    private fun getMessages(){
        parent.firebaseViewModel.selectedGroupRoom.observe(viewLifecycleOwner, Observer {
            val sorted = MessageUtils.sortMessages(it)
            messageAdapter.submitList(sorted)
        })
    }

    private fun sendMessage(){
        val message = Message(MessageUtils.generateUID(50), binding.messageEditText.text.toString(), System.currentTimeMillis(), parent.firebaseAuth.currentUser?.uid, MessageType.TEXT)
        parent.firebaseViewModel.sendGroupMessage(message, roomId, parent.mDbRef, {
            clearEditText()
        }, {})
    }

    private fun clearEditText(){
        binding.messageEditText.setText("")
    }

    private fun popNavigation(){
        findNavController().navigateUp()
    }

    private fun getChatRoom(){
        parent.firebaseViewModel.getGroupChatRoomRecurrent(roomId, parent.mDbRef, {
            parent.firebaseViewModel.selectedGroupRoom.value = it
            getParticipants()
        }, {})
    }

    private fun getParticipants(){
        var users = mutableListOf<User>()
        for (i in parent.firebaseViewModel.selectedGroupRoom.value!!.participants!!.values.toList()){
            parent.firebaseViewModel.getUserSingle(i, parent.mDbRef, {
                if (it != null) {
                    users.add(it)
                }
            }, {
                parent.firebaseViewModel.selectedGroupParticipants.value = users
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
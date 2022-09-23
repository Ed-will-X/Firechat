package com.varsel.firechat.view.signedIn.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.varsel.firechat.databinding.FragmentGroupChatPageBinding
import com.varsel.firechat.model.Chat.GroupRoom
import com.varsel.firechat.model.Message.Message
import com.varsel.firechat.model.Message.MessageType
import com.varsel.firechat.utils.MessageUtils
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedIn.adapters.ChatPageType
import com.varsel.firechat.view.signedIn.adapters.MessageListAdapter
import com.varsel.firechat.viewModel.GroupChatDetailViewModel

class GroupChatPageFragment : Fragment() {
    private var _binding: FragmentGroupChatPageBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private lateinit var roomId: String
    private lateinit var messageAdapter: MessageListAdapter
    private val groupPageViewModel: GroupChatDetailViewModel by activityViewModels()

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

        messageAdapter = MessageListAdapter(parent.firebaseAuth, parent.mDbRef,this, ChatPageType.GROUP, parent.firebaseViewModel)
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
        val message = Message(MessageUtils.generateUID(50), binding.messageEditText.text.toString(), System.currentTimeMillis(), parent.firebaseAuth.currentUser!!.uid, MessageType.TEXT)
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

            if (it != null) {
                groupPageViewModel.determineGetParticipants(it, parent)
            }
        }, {})
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
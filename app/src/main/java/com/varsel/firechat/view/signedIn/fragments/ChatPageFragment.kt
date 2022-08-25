package com.varsel.firechat.view.signedIn.fragments

import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentChatPageBinding
import com.varsel.firechat.model.Chat.ChatRoom
import com.varsel.firechat.model.message.Message
import com.varsel.firechat.model.message.MessageType
import com.varsel.firechat.utils.MessageUtils
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedIn.adapters.MessageListAdapter


class ChatPageFragment : Fragment() {
    private var _binding: FragmentChatPageBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    var existingChatRoomId: String? = null
    private lateinit var userUID: String
    lateinit var newChatRoomId: String
    var firstMessageSent: Boolean? = null
    lateinit var newChatRoom: ChatRoom
    lateinit var messagesListAdapter: MessageListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChatPageBinding.inflate(inflater, container, false)
        val view = binding.root
        parent = activity as SignedinActivity

        firstMessageSent = false

        existingChatRoomId = ChatPageFragmentArgs.fromBundle(requireArguments()).chatRoom
        userUID = ChatPageFragmentArgs.fromBundle(requireArguments()).userUid

        // Chatroom initialisation
        newChatRoomId = MessageUtils.generateUID(50)
        newChatRoom = ChatRoom(newChatRoomId, hashMapOf<String, String>(userUID to userUID, parent.firebaseAuth.uid.toString() to parent.firebaseAuth.uid.toString()))

        getChatRoom()

        messagesListAdapter = MessageListAdapter(parent.firebaseAuth, this)
        binding.messagesRecyclerView.adapter = messagesListAdapter

        if(existingChatRoomId != null || firstMessageSent == true){
            parent.firebaseViewModel.selectedChatRoom.observe(viewLifecycleOwner, Observer {
                val sorted = it?.messages?.values?.sortedBy {
                    it.time
                }
                messagesListAdapter.submitList(sorted)
            })
        }

        // send button
        binding.sendMessageBtn.setOnClickListener {
            var message = Message(MessageUtils.generateUID(50), binding.messageEditText.text.toString(), System.currentTimeMillis(), parent.firebaseAuth.currentUser?.uid, MessageType.TEXT)

            if(binding.messageEditText.text.toString() != ""){
                sendMessage(message)
            }
            binding.messageEditText.setText("")

            if(parent.firebaseViewModel.selectedChatRoom.value?.messages != null){
                binding.messagesRecyclerView.scrollToPosition(parent.firebaseViewModel.selectedChatRoom.value?.messages!!.size -1)
            }
        }

        return view
    }

    private fun getChatRoom(){
        if(existingChatRoomId != null){
            parent.firebaseViewModel.getChatRoomRecurrent(existingChatRoomId!!, parent.mDbRef, {
                parent.firebaseViewModel.selectedChatRoom.value = it
            },{})
        } else {
            // Gets the chat room based on the generated id
            parent.firebaseViewModel.getChatRoomRecurrent(newChatRoomId, parent.mDbRef, {
                parent.firebaseViewModel.selectedChatRoom.value = it
            },{})
        }
    }

    private fun sendMessage(message: Message){
        if(existingChatRoomId != null){
            // Existing chat room
            parent.firebaseViewModel.sendMessage(message, existingChatRoomId!!, parent.mDbRef, {},{})

        } else {
            // create chat room in both users
            parent.firebaseViewModel.appendChatRoom(newChatRoomId, userUID, parent.firebaseAuth, parent.mDbRef, {
                if(firstMessageSent == false){
                    parent.firebaseViewModel.appendParticipants(newChatRoom, parent.mDbRef, {
                        parent.firebaseViewModel.sendMessage(message, newChatRoomId, parent.mDbRef, {
                            firstMessageSent = true
                        }, {})
                    }, {})
                } else {
                    parent.firebaseViewModel.sendMessage(message, newChatRoomId, parent.mDbRef, {}, {})
                }
            },{})
            // append to that chat room in the chats repo

        }
    }



    fun getParticipant(){
        if(existingChatRoomId != null){

        } else {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
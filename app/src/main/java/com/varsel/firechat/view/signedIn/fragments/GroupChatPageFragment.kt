package com.varsel.firechat.view.signedIn.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentGroupChatPageBinding
import com.varsel.firechat.model.Message.Message
import com.varsel.firechat.model.Message.MessageStatus
import com.varsel.firechat.model.Message.MessageType
import com.varsel.firechat.model.User.User
import com.varsel.firechat.utils.MessageUtils
import com.varsel.firechat.utils.UserUtils
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedIn.adapters.ChatPageType
import com.varsel.firechat.view.signedIn.adapters.FriendListAdapter
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

        getGroupChatRoom()
        getMessages()
        observeParticipants()

        messageAdapter = MessageListAdapter(parent.firebaseAuth, parent.mDbRef,this, requireContext(), ChatPageType.GROUP, parent.firebaseViewModel,
        { message, messageType, messageStatus ->

        }, { message, messageType, messageStatus ->
            if(messageType == MessageType.TEXT && messageStatus == MessageStatus.SYSTEM){
                val users = splitUsersString(message.message)
                showSystemMessageActionsheet(users)
            }
        })
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

    private fun observeParticipants(){
        parent.firebaseViewModel.selectedGroupParticipants.observe(viewLifecycleOwner, Observer {
            interpolateUserParticipants(it)
        })
    }

    private fun getMessages(){
        parent.firebaseViewModel.selectedGroupRoom.observe(viewLifecycleOwner, Observer {
            val sorted = MessageUtils.sortMessages(it)
            binding.groupNameText.text = it?.groupName
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

    private fun getGroupChatRoom(){
        parent.firebaseViewModel.getGroupChatRoomRecurrent(roomId, parent.mDbRef, {
            parent.firebaseViewModel.selectedGroupRoom.value = it

            if (it != null) {
                groupPageViewModel.determineGetParticipants(it, parent)
            }
        }, {})
    }

    private fun showSystemMessageActionsheet(userIds: List<String>){
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(R.layout.action_sheet_system_message)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.system_message_participant_recycler_view)
        val adapter = FriendListAdapter {
            val action = GroupChatPageFragmentDirections.actionGroupChatPageFragmentToOtherProfileFragment(it)
            binding.root.findNavController().navigate(action)
        }

        recyclerView?.adapter = adapter

        getUsers(userIds) {
            adapter.friends = it as MutableList<User>
            adapter.notifyDataSetChanged()
        }

        dialog.show()
    }

    private fun splitUsersString(users: String): List<String> {
        val userList = users.split(" ").toTypedArray()
        val currentUser = parent.firebaseAuth.currentUser!!.uid
        val returnedUsers = mutableListOf<String>()

        for(i in userList){
            if(i != currentUser){
                returnedUsers.add(i)
            }
        }

        return returnedUsers
    }

    private fun getUsers(userIds: List<String>, afterCallback: (List<User>)-> Unit){
        val users = mutableListOf<User>()

        for(i in userIds){
            parent.firebaseViewModel.getUserSingle(i, parent.mDbRef, {
                if(it != null){
                    users.add(it)
                }
            }, {
                afterCallback(users)
            })
        }
    }

    // Top bar participant list
    private fun interpolateUserParticipants(users: List<User>){
        val firstnames = mutableListOf<String>()
        val currentUser = parent.firebaseAuth.currentUser!!.uid

        if(users != null){
            for((i, v) in users.withIndex()){
                if(currentUser == v.userUID){
                    continue
                }
                firstnames.add(v.name ?: "")
            }
        }

        val usersInString = firstnames?.joinToString(
            separator = ", "
        )

        binding.participantsText.text = UserUtils.truncate(usersInString, 40)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
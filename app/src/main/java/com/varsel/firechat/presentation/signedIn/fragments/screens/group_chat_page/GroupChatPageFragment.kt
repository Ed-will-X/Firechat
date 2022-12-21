package com.varsel.firechat.presentation.signedIn.fragments.screens.group_chat_page

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentGroupChatPageBinding
import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.Message.Message
import com.varsel.firechat.data.local.Message.MessageStatus
import com.varsel.firechat.data.local.Message.MessageType
import com.varsel.firechat.data.local.ReadReceipt.ReadReceipt
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.LifecycleUtils
import com.varsel.firechat.utils.MessageUtils
import com.varsel.firechat.utils.UserUtils
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.adapters.ChatPageType
import com.varsel.firechat.presentation.signedIn.adapters.FriendListAdapter
import com.varsel.firechat.presentation.signedIn.adapters.MessageListAdapter
import com.varsel.firechat.presentation.signedIn.fragments.screens.chat_page.ChatPageViewModel
import com.varsel.firechat.presentation.signedIn.fragments.screens.group_chat_detail.GroupChatDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class GroupChatPageFragment : Fragment() {
    private var _binding: FragmentGroupChatPageBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private lateinit var roomId: String
    private lateinit var messageAdapter: MessageListAdapter
    private val groupPageViewModel: GroupChatDetailViewModel by activityViewModels()
    private val chatPageViewModel: ChatPageViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateReadReceipt()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGroupChatPageBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        parent = activity as SignedinActivity
        roomId = GroupChatPageFragmentArgs.fromBundle(requireArguments()).groupRoomId

        LifecycleUtils.observeInternetStatus(parent, this, {
            binding.sendMessageBtn.isEnabled = true
        }, {
            binding.sendMessageBtn.isEnabled = false
        })

        observeGroupImage()

//        getGroupChatRoom()
        getGroupRoomFromMemory()
        observeParticipants()


        val fragment = this
        lifecycleScope.launch(Dispatchers.Main) {
            delay(300)
            messageAdapter = MessageListAdapter(roomId, parent,fragment, requireContext(), ChatPageType.GROUP, parent.firebaseViewModel,
                { message, image ->
                    ImageUtils.displayImageMessage_group(image, message, parent)
                }, { message, messageType, messageStatus ->
                    if(messageType == MessageType.TEXT && messageStatus == MessageStatus.SYSTEM){
                        val users = splitUsersString(message.message)
                        showSystemMessageActionsheet(users)
                    }
                }, { profileImage, user ->
                    ImageUtils.displayProfilePicture(profileImage, user, parent)
                })

            binding.messagesRecyclerView.adapter = messageAdapter

            getMessages()
        }



        chatPageViewModel.actionBarVisibility.observe(viewLifecycleOwner, Observer {
            if(it){
                binding.messageActionBar.visibility = View.VISIBLE
            } else {
                binding.messageActionBar.visibility = View.GONE
            }
        })


        binding.sendMessageBtn.setOnClickListener {
            sendMessage {
                updateReadReceipt()
            }
            clearEditText()
        }

        binding.chatBackButton.setOnClickListener {
            popNavigation()
        }

        binding.appbar.setOnClickListener {
            navigateToGroupChatDetail(roomId)
        }

        binding.actionBarSwitch.setOnClickListener {
            chatPageViewModel.toggleActionbarVisibility()
        }

        binding.gallery.setOnClickListener {
            ImageUtils.openImagePicker(this)
        }

        binding.profileImage.setOnClickListener {
            val selectedGroupImage = parent.profileImageViewModel.selectedGroupImage.value
            val selectedGroupRoom = parent.firebaseViewModel.selectedGroupRoom.value
            if(selectedGroupImage != null && selectedGroupRoom != null){
                ImageUtils.displayGroupImage(selectedGroupImage, selectedGroupRoom, parent)
            }
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        ImageUtils.handleOnActivityResult(requireContext(), requestCode, resultCode, data, {
            ImageUtils.uploadChatImage(it, roomId, parent) { message, image ->
                parent.imageViewModel.storeImage(image) {
                    sendImgMessage(message) {
                        updateReadReceipt()
                    }
                }
            }
        }, {})
    }

    private fun updateReadReceipt(){
        val receipt = ReadReceipt("${roomId}:${parent.firebaseAuth.currentUser!!.uid}", System.currentTimeMillis())
        parent.readReceiptViewModel.storeReceipt(receipt)
    }

    private fun observeGroupImage(){
//        parent.profileImageViewModel.selectedGroupImageEncoded.observe(viewLifecycleOwner, Observer {
//            if(it != null){
//                ImageUtils.setProfilePic(it, binding.profileImage, binding.profileImageParent)
//                binding.profileImageParent.visibility = View.VISIBLE
//            }
//        })

        parent.profileImageViewModel.selectedGroupImage.observe(viewLifecycleOwner, Observer { groupImage ->
            if(groupImage != null && groupImage.image != null){
                ImageUtils.setProfilePic(groupImage.image!!, binding.profileImage, binding.profileImageParent, parent)
                binding.profileImageParent.visibility = View.VISIBLE
            }
        })
    }

    private fun setShimmerVisibility(chatRoom: ChatRoom?){
        if(chatRoom == null){
            binding.shimmerMessages.visibility = View.VISIBLE
        } else {
            binding.shimmerMessages.visibility = View.GONE
        }
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
            if(it?.roomUID == roomId){
                setShimmerVisibility(it)

                val sorted = MessageUtils.sortMessages(it)
                binding.groupNameText.text = it?.groupName

                messageAdapter.submitList(sorted)
                messageAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun sendImgMessage(message: Message, success: ()-> Unit){
        parent.firebaseViewModel.sendGroupMessage(message, roomId, parent.mDbRef, {
            success()
        }, {})
    }

    private fun sendMessage(success: () -> Unit){
        val messageText = binding.messageEditText.text.toString().trim()
        val message = Message(MessageUtils.generateUID(30), messageText, System.currentTimeMillis(), parent.firebaseAuth.currentUser!!.uid, MessageType.TEXT)
        parent.firebaseViewModel.sendGroupMessage(message, roomId, parent.mDbRef, {
            success()
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

    private fun getGroupRoomFromMemory(){
        parent.firebaseViewModel.groupRooms.observe(viewLifecycleOwner, Observer {
            MessageUtils.findGroupRoom(it, roomId) {
                parent.firebaseViewModel.selectedGroupRoom.value = it

                groupPageViewModel.determineGetParticipants(it, parent)
            }
        })
    }

    private fun showSystemMessageActionsheet(userIds: List<String>){
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(R.layout.action_sheet_system_message)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.system_message_participant_recycler_view)
        val adapter = FriendListAdapter(parent, { id, user, base64 ->
            dialog.dismiss()
            navigateToOtherProfilePage(id, user, base64)
        }, { profileImage, user ->
            ImageUtils.displayProfilePicture(profileImage, user, parent)
            dialog.dismiss()
        })

        recyclerView?.adapter = adapter

        getUsers(userIds) {
            adapter.friends = it as MutableList<User>
            adapter.notifyDataSetChanged()
        }

        dialog.show()
    }

    private fun navigateToOtherProfilePage(id: String, user: User, base64: String?) {
        try {
            val action = GroupChatPageFragmentDirections.actionGroupChatPageFragmentToOtherProfileFragment(id)
            binding.root.findNavController().navigate(action)

            parent.firebaseViewModel.selectedUser.value = user
            parent.profileImageViewModel.selectedOtherUserProfilePic.value = base64
        } catch (e: IllegalArgumentException){ }
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

        updateReadReceipt()

        _binding = null
    }
}
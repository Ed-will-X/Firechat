package com.varsel.firechat.presentation.signedIn.fragments.screens.group_chat_page

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.*
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.common.Response
import com.varsel.firechat.databinding.FragmentGroupChatPageBinding
import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.Message.Message
import com.varsel.firechat.data.local.Message.MessageStatus
import com.varsel.firechat.data.local.Message.MessageType
import com.varsel.firechat.data.local.ReadReceipt.ReadReceipt
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.domain.use_case._util.message.SortMessages_UseCase
import com.varsel.firechat.domain.use_case.chat_image.DisplayGroupImage_UseCase
import com.varsel.firechat.domain.use_case.chat_image.DisplayImageMessageGroup_UseCase
import com.varsel.firechat.domain.use_case.chat_image.StoreChatImageUseCase
import com.varsel.firechat.domain.use_case.chat_image.UploadChatImage_UseCase
import com.varsel.firechat.domain.use_case.current_user.CheckServerConnectionUseCase
import com.varsel.firechat.domain.use_case.group_chat.InterpolateGroupParticipantsUseCase
import com.varsel.firechat.domain.use_case.group_chat.SendGroupMessage_UseCase
import com.varsel.firechat.domain.use_case.image.HandleOnActivityResult_UseCase
import com.varsel.firechat.domain.use_case.image.OpenImagePicker_UseCase
import com.varsel.firechat.domain.use_case.other_user.GetListOfUsers_UseCase
import com.varsel.firechat.domain.use_case.profile_image.DisplayProfileImage
import com.varsel.firechat.domain.use_case.profile_image.SetProfilePicUseCase
import com.varsel.firechat.domain.use_case.read_receipt_temp.StoreReceipt_UseCase
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.adapters.ChatPageType
import com.varsel.firechat.presentation.signedIn.adapters.FriendListAdapter
import com.varsel.firechat.presentation.signedIn.adapters.MessageListAdapter
import com.varsel.firechat.presentation.signedIn.fragments.screens.chat_page.ChatPageViewModel
import com.varsel.firechat.presentation.signedIn.fragments.screens.group_chat_detail.GroupChatDetailViewModel
import com.varsel.firechat.presentation.viewModel.FriendListFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import javax.inject.Inject

@AndroidEntryPoint
class GroupChatPageFragment : Fragment() {
    private var _binding: FragmentGroupChatPageBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private lateinit var roomId: String
    private lateinit var messageAdapter: MessageListAdapter
    private val groupPageViewModel: GroupChatDetailViewModel by activityViewModels()
    private val chatPageViewModel: ChatPageViewModel by activityViewModels()
    private lateinit var viewModel: GroupChatPageViewModel
    private lateinit var friendsViewModel: FriendListFragmentViewModel

    @Inject
    lateinit var displayProfileImage: DisplayProfileImage

    @Inject
    lateinit var checkServerConnection: CheckServerConnectionUseCase

    @Inject
    lateinit var interpolateParticipants: InterpolateGroupParticipantsUseCase

    @Inject
    lateinit var sendMessageUseCase: SendGroupMessage_UseCase

    @Inject
    lateinit var getlistofusers: GetListOfUsers_UseCase

    @Inject
    lateinit var openImagePicker: OpenImagePicker_UseCase

    @Inject
    lateinit var handleOnActivityResult: HandleOnActivityResult_UseCase

    @Inject
    lateinit var uploadChatImage: UploadChatImage_UseCase

    @Inject
    lateinit var displayGroupImage: DisplayGroupImage_UseCase

    @Inject
    lateinit var displayImageMessage: DisplayImageMessageGroup_UseCase

    @Inject
    lateinit var setProfilePic: SetProfilePicUseCase

    @Inject
    lateinit var sortMessages: SortMessages_UseCase

    @Inject
    lateinit var storeImage_UseCase: StoreChatImageUseCase

    @Inject
    lateinit var storeReceipt: StoreReceipt_UseCase

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

        friendsViewModel = ViewModelProvider(this).get(FriendListFragmentViewModel::class.java)
        viewModel = ViewModelProvider(this).get(GroupChatPageViewModel::class.java)

//        viewModel.getGroupChat(roomId)
        initialiseAdapter()
        collectState(roomId)

        checkServerConnection().onEach {
            try {
                binding.sendMessageBtn.isEnabled = it
            } catch (e: Exception) {  }
        }.launchIn(lifecycleScope)

        chatPageViewModel.actionBarVisibility.observe(viewLifecycleOwner, Observer {
            if(it){
                binding.messageActionBar.visibility = View.VISIBLE
            } else {
                binding.messageActionBar.visibility = View.GONE
            }
        })


        binding.sendMessageBtn.setOnClickListener {
            if(binding.messageEditText.text.isNotEmpty()) {
                sendMessage()

            } else {
                // TODO: Show empty text input infobar
            }
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
            openImagePicker(this)
        }

        binding.profileImage.setOnClickListener {
            if(viewModel.state.value?.selectedRoom != null) {
                val selectedGroupImage = viewModel.state.value?.groupImage
                val selectedGroupRoom = viewModel.state.value?.selectedRoom
                if(selectedGroupImage != null && selectedGroupRoom != null){
                    displayGroupImage(selectedGroupImage, selectedGroupRoom, parent)
                }
            }
        }

        return view
    }

    private fun collectState(roomId: String) {

        viewModel.state.observe(viewLifecycleOwner, Observer {
            viewModel.getGroupChat(roomId)

            if(it.selectedRoom != null) {
                getMessages(it.selectedRoom)

                if(viewModel._hasRun.value == false) {
                    viewModel.getGroupImage(it.selectedRoom)

                    viewModel._hasRun.value = true
                }
            }

            // TODO: Observe in realtime. When current user removes group, it should take effect on this page also
            observeGroupImage(it.groupImage)
        })

        viewModel.participsnts.observe(viewLifecycleOwner, Observer {
            interpolateParticipants(it, binding.participantsText)
        })
    }



    private fun initialiseAdapter() {
        messageAdapter = MessageListAdapter(roomId, parent, this, requireContext(), this, chatPageViewModel, viewModel, ChatPageType.GROUP,
            { message, image ->
                displayImageMessage(image, message, viewModel.participsnts.value, parent)
            }, { message, messageType, messageStatus ->
                if(messageType == MessageType.TEXT && messageStatus == MessageStatus.SYSTEM){
                    val users = splitUsersString(message.message)
                    showSystemMessageActionsheet(users)
                }
            }, { profileImage, user ->
                displayProfileImage(profileImage, user, parent)
            }, { message, user ->  
                
            })

        binding.messagesRecyclerView.adapter = messageAdapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        handleOnActivityResult(requestCode, resultCode, data, {
            uploadChatImage(it, roomId, parent) { message, image ->

                lifecycleScope.launch {
                    storeImage_UseCase(image)
                    sendImgMessage(message) {
                        updateReadReceipt()
                    }
                }
            }
        }, {})
    }

    private fun updateReadReceipt(){
        val receipt = ReadReceipt("${roomId}:${viewModel.getCurrentUserId()}", System.currentTimeMillis())
        lifecycleScope.launch {
            storeReceipt(receipt)
        }
    }

    private fun observeGroupImage(profileImage: ProfileImage?){
        if(profileImage?.image != null){
            setProfilePic(profileImage.image!!, binding.profileImage, binding.profileImageParent, parent)
            binding.profileImageParent.visibility = View.VISIBLE
        } else {
            binding.profileImageParent.visibility = View.GONE
        }
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

    private fun getMessages(groupRoom: GroupRoom){
        if(groupRoom.roomUID == roomId){
            setShimmerVisibility(groupRoom)

            val sorted = sortMessages(groupRoom)
            binding.groupNameText.text = groupRoom.groupName

            messageAdapter.submitList(sorted)
            messageAdapter.notifyDataSetChanged()
        }
    }

    private fun sendImgMessage(message: Message, success: ()-> Unit){
        sendMessageUseCase(roomId, message).onEach {
            when(it) {
                is Response.Success -> {
                    success()
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun sendMessage(){
        sendMessageUseCase(roomId, binding.messageEditText).onEach {
            when(it) {
                is Response.Success -> {
                    updateReadReceipt()
                }
                is Response.Fail -> {
                    // TODO: Show failure infobar in failure check

                }
            }
        }.launchIn(lifecycleScope)
        clearEditText()
    }

    private fun clearEditText(){
        binding.messageEditText.setText("")
    }

    private fun popNavigation(){
        findNavController().navigateUp()
    }


    private fun showSystemMessageActionsheet(userIds: List<String>){
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(R.layout.action_sheet_system_message)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.system_message_participant_recycler_view)
        val adapter = FriendListAdapter(parent, this, friendsViewModel, { id, user, base64 ->
            dialog.dismiss()
            navigateToOtherProfilePage(id, user, base64)
        }, { profileImage, user ->
            displayProfileImage(profileImage, user, parent)
            dialog.dismiss()
        })

        recyclerView?.adapter = adapter

        getlistofusers(userIds).onEach {
            adapter.friends = it as MutableList<User>
            adapter.notifyDataSetChanged()
        }.launchIn(lifecycleScope)

        dialog.show()
    }

    private fun navigateToOtherProfilePage(id: String, user: User, base64: String?) {
        try {
            val action = GroupChatPageFragmentDirections.actionGroupChatPageFragmentToOtherProfileFragment(id)
            binding.root.findNavController().navigate(action)

        } catch (e: IllegalArgumentException){ }
    }

    // TODO: Modularise
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

    override fun onDestroyView() {
        super.onDestroyView()

        updateReadReceipt()

        _binding = null
    }
}
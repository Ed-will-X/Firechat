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
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.common.Resource
import com.varsel.firechat.common.Response
import com.varsel.firechat.common._utils.ImageUtils
import com.varsel.firechat.databinding.FragmentGroupChatPageBinding
import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.Message.Message
import com.varsel.firechat.data.local.Message.MessageStatus
import com.varsel.firechat.data.local.Message.MessageType
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.databinding.ActionSheetMessageOptionsBinding
import com.varsel.firechat.domain.use_case._util.message.FormatStampMessageDetail_UseCase
import com.varsel.firechat.domain.use_case._util.message.SortMessages_UseCase
import com.varsel.firechat.domain.use_case._util.string.Truncate_UseCase
import com.varsel.firechat.domain.use_case._util.user.ExcludeCurrentUserIdFromList_UseCase
import com.varsel.firechat.domain.use_case._util.user.ExcludeCurrentUserIdFromMap_UseCase
import com.varsel.firechat.domain.use_case.chat_image.*
import com.varsel.firechat.domain.use_case.current_user.CheckServerConnectionUseCase
import com.varsel.firechat.domain.use_case.group_chat.InterpolateGroupParticipantsUseCase
import com.varsel.firechat.domain.use_case.group_chat.SendGroupMessage_UseCase
import com.varsel.firechat.domain.use_case.image.HandleOnActivityResult_image_UseCase
import com.varsel.firechat.domain.use_case.image.OpenImagePicker_UseCase
import com.varsel.firechat.domain.use_case.message.DeleteMessageForAll_GroupRoom_UseCase
import com.varsel.firechat.domain.use_case.message.DeleteMessage_Group_UseCase
import com.varsel.firechat.domain.use_case.other_user.GetListOfUsers_UseCase
import com.varsel.firechat.domain.use_case.profile_image.DisplayProfileImage
import com.varsel.firechat.domain.use_case.profile_image.SetProfilePicUseCase
import com.varsel.firechat.domain.use_case.read_receipt_temp.StoreReceipt_UseCase
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.adapters.ChatPageType
import com.varsel.firechat.presentation.signedIn.adapters.FriendListAdapter
import com.varsel.firechat.presentation.signedIn.adapters.MessageListAdapter
import com.varsel.firechat.presentation.signedIn.adapters.ReadByAdapter
import com.varsel.firechat.presentation.signedIn.fragments.screens.chat_page.ChatPageFragmentDirections
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
    lateinit var handleOnActivityResult: HandleOnActivityResult_image_UseCase

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

    @Inject
    lateinit var formatStampTime: FormatStampMessageDetail_UseCase

    @Inject
    lateinit var truncate: Truncate_UseCase

    @Inject
    lateinit var checkChatImageInDb: CheckChatImageInDb

    @Inject
    lateinit var excludeUserId: ExcludeCurrentUserIdFromList_UseCase

    @Inject
    lateinit var excludeUserIdFromMap: ExcludeCurrentUserIdFromMap_UseCase

    @Inject
    lateinit var deleteForAll: DeleteMessageForAll_GroupRoom_UseCase

    @Inject
    lateinit var deleteMessage: DeleteMessage_Group_UseCase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        updateReadReceipt()
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

        binding.darkOverlay.setOnClickListener {
            binding.darkOverlay.visibility = View.GONE
            binding.messageDetailParent.visibility = View.GONE
            binding.readByParent.visibility = View.GONE
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

    private fun showMessageOptionsActionsheet(message: Message, user: User?) {
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = ActionSheetMessageOptionsBinding.inflate(layoutInflater, binding.root, false)
        dialog.setContentView(dialogBinding.root)

        if(user != null) {
            setDialogBindings(dialog, dialogBinding, message, user)
        } else {
            viewModel.getOtherUserSingle(message.sender).onEach {
                when(it) {
                    is Resource.Success -> {
                        if(it.data != null) {
                            setDialogBindings(dialog, dialogBinding, message, it.data)
                        }
                    }
                }
            }.launchIn(lifecycleScope)
        }


        dialogBinding.cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupReadByAdapter(message: Message, userIDs: List<Pair<String, Long>>) {
        binding.readByRecyclerView.visibility = View.GONE
        binding.noViews.visibility = View.GONE

        val viewedByCount = userIDs.count()
        val adapter = ReadByAdapter(message, this, parent.signedinViewModel, parent) {
            val action = ChatPageFragmentDirections.actionChatPageFragmentToOtherProfileFragment(it.userUID)
            findNavController().navigate(action)
        }
        binding.readByRecyclerView.adapter = adapter

        binding.viewedByCount.setText(if(viewedByCount == 1) parent.getString(R.string.viewed_by_1_contact) else parent.getString(R.string.viewed_by_int_contacts, viewedByCount))
        binding.readByRecyclerView.visibility = if(viewedByCount > 0 && !message.deletedBySender) View.VISIBLE else View.GONE
        binding.noViews.visibility = if(viewedByCount < 1 || message.deletedBySender) View.VISIBLE else View.GONE
        binding.viewedByCount.visibility = if(viewedByCount > 0 && !message.deletedBySender) View.VISIBLE else View.GONE

        adapter.items = userIDs
    }

    private fun setDialogBindings(dialog: BottomSheetDialog, dialogBinding: ActionSheetMessageOptionsBinding, message: Message, user: User) {
        dialogBinding.name.setText(if(user.userUID == viewModel.getCurrentUserId()) parent.getString(R.string.you) else user.name)
        dialogBinding.deleteForAllParent.visibility = if(user.userUID == viewModel.getCurrentUserId()) View.VISIBLE else View.GONE
        dialogBinding.readByParent.visibility = if(message.sender == viewModel.getCurrentUserId()) View.VISIBLE else View.GONE
        dialogBinding.deleteForAllParent.visibility = if(message.deletedBySender == true) View.GONE else View.VISIBLE
        dialogBinding.replyParent.visibility = View.GONE
        dialogBinding.forwardParent.visibility = View.GONE

        dialogBinding.deleteParent.setOnClickListener {
            deleteMessage(message, roomId).onEach {
                when(it) {
                    is Response.Success -> {
//                        Log.d("LLL", "Deleted")
                    }
                    is Response.Loading -> {
//                        Log.d("LLL", "Deleting")
                    }
                    is Response.Fail -> {
//                        Log.d("LLL", "Failed to delete")
                    }
                }
            }.launchIn(lifecycleScope)
            dialog.dismiss()
        }

        dialogBinding.detailsParent.setOnClickListener {
            showMessageDetailOverlay(message, user)
            dialog.dismiss()
        }

        dialogBinding.readByParent.setOnClickListener {
            binding.readByParent.visibility = View.VISIBLE
            binding.darkOverlay.visibility = View.VISIBLE

            val filteredMap = excludeUserIdFromMap(message.readBy)
            val filteredPair = filteredMap.map { Pair(it.key, it.value) }

            setupReadByAdapter(message, filteredPair)

            dialog.dismiss()
        }

        dialogBinding.deleteForAllParent.setOnClickListener {
            deleteForAll(message, roomId).onEach {
                when(it) {
                    is Response.Success -> {
//                        Log.d("LLL", "Deleted")
                    }
                    is Response.Loading -> {
//                        Log.d("LLL", "Deleting")
                    }
                    is Response.Fail -> {
//                        Log.d("LLL", "Failed to delete")
                    }
                }
            }.launchIn(lifecycleScope)

            dialog.dismiss()
        }
    }


    private fun showMessageDetailOverlay(message: Message, user: User) {
        // Reset
        binding.overlayDetailMessageParent.visibility = View.GONE
        binding.overlayDetailMessageImageParent.visibility = View.GONE
        binding.overlayDetailMessageTypeParent.visibility = View.GONE


        binding.overlayDetailName.setText(if(user.userUID == viewModel.getCurrentUserId()) parent.getString(R.string.you) else user.name)
        binding.overlayDetailTime.setText(formatStampTime(message.time))
        binding.overlayDetailMessageId.setText(message.messageUID)

        if(message.deletedBySender == false) {
            binding.overlayDetailMessageType.setText(returnTextFromMessageType(message.type))
            binding.overlayDetailMessageTypeParent.visibility = View.VISIBLE

            if(message.type == MessageType.TEXT) {
                binding.overlayDetailMessage.setText(truncate(message.message, 150))
                binding.overlayDetailMessageParent.visibility = View.VISIBLE
            } else if(message.type == MessageType.IMAGE) {
                lifecycleScope.launch {
                    binding.overlayDetailMessageImageParent.visibility = View.VISIBLE

                    checkChatImageInDb(message.message).onEach {
                        when(it) {
                            is Resource.Success -> {
                                if(it.data != null) {
                                    val bitmap = ImageUtils.base64ToBitmap(it.data.image!!)
                                    Glide.with(parent).load(bitmap).dontAnimate().into(binding.overlayDetailImage)
                                }
                            }
                            is Resource.Loading -> {
                                // TODO: Show loading spinner
                            }
                            is Resource.Error -> {
                                // TODO: Show error image
                            }
                        }
                    }.launchIn(lifecycleScope)
                }
            }
        }

        binding.darkOverlay.visibility = View.VISIBLE
        binding.messageDetailParent.visibility = View.VISIBLE
    }

    private fun returnTextFromMessageType(messageType: Int): String {
        return when(messageType) {
            MessageType.TEXT -> "Text"
            MessageType.IMAGE -> "Image"
            else -> "Undefined"
        }
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
                showMessageOptionsActionsheet(message, user)
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
//                        updateReadReceipt()
                    }
                }
            }
        }, {})
    }

//    private fun updateReadReceipt(){
//        val receipt = ReadReceipt("${roomId}:${viewModel.getCurrentUserId()}", System.currentTimeMillis())
//        lifecycleScope.launch {
//            storeReceipt(receipt)
//        }
//    }

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
//                    updateReadReceipt()
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

//        updateReadReceipt()

        _binding = null
    }
}
package com.varsel.firechat.presentation.signedIn.fragments.screens.chat_page

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.common.Resource
import com.varsel.firechat.common.Response
import com.varsel.firechat.common._utils.ExtensionFunctions.Companion.observeOnce
import com.varsel.firechat.common._utils.ImageUtils
import com.varsel.firechat.databinding.FragmentChatPageBinding
import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.Message.Message
import com.varsel.firechat.data.local.Message.MessageType
import com.varsel.firechat.domain.use_case.chat_image.DisplayChatImage_UseCase
import com.varsel.firechat.domain.use_case.chat_image.UploadChatImage_UseCase
import com.varsel.firechat.domain.use_case.current_user.CheckServerConnectionUseCase
import com.varsel.firechat.domain.use_case.image.HandleOnActivityResult_UseCase
import com.varsel.firechat.domain.use_case.image.OpenImagePicker_UseCase
import com.varsel.firechat.domain.use_case.profile_image.DisplayProfileImage
import com.varsel.firechat.common._utils.MessageUtils
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.databinding.ActionSheetMessageOptionsBinding
import com.varsel.firechat.domain.use_case._util.message.FormatStampMessageDetail_UseCase
import com.varsel.firechat.domain.use_case._util.string.Truncate_UseCase
import com.varsel.firechat.domain.use_case._util.user.ExcludeCurrentUserIdFromList_UseCase
import com.varsel.firechat.domain.use_case._util.user.ExcludeCurrentUserIdFromMap_UseCase
import com.varsel.firechat.domain.use_case.chat_image.CheckChatImageInDb
import com.varsel.firechat.domain.use_case.chat_image.StoreChatImageUseCase
import com.varsel.firechat.domain.use_case.chat_room.FindChatRoom_UseCase
import com.varsel.firechat.domain.use_case.last_online.CheckLastOnline_UseCase
import com.varsel.firechat.domain.use_case.message.DeleteMessageForAll_ChatRoom_UseCase
import com.varsel.firechat.domain.use_case.message.DeleteMessage_Chat_UseCase
import com.varsel.firechat.domain.use_case.read_receipt_temp.StoreReceipt_UseCase
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.adapters.ChatPageType
import com.varsel.firechat.presentation.signedIn.adapters.MessageListAdapter
import com.varsel.firechat.presentation.signedIn.adapters.ReadByAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
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
    private lateinit var viewModel: ChatPageViewModel

    @Inject
    lateinit var displayProfileImage: DisplayProfileImage

    @Inject
    lateinit var checkServerConnection: CheckServerConnectionUseCase

    @Inject
    lateinit var displayChatImage: DisplayChatImage_UseCase

    @Inject
    lateinit var openImagePicker: OpenImagePicker_UseCase

    @Inject
    lateinit var handleOnActivityResult: HandleOnActivityResult_UseCase

    @Inject
    lateinit var uploadChatImage: UploadChatImage_UseCase

    @Inject
    lateinit var storeImage_UseCase: StoreChatImageUseCase

    @Inject
    lateinit var storeReceipt: StoreReceipt_UseCase

    @Inject
    lateinit var findChatRoom: FindChatRoom_UseCase

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
    lateinit var deleteForAll: DeleteMessageForAll_ChatRoom_UseCase

    @Inject
    lateinit var deleteMessage: DeleteMessage_Chat_UseCase

    @Inject
    lateinit var checkLastOnline: CheckLastOnline_UseCase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        updateReadReceipt()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChatPageBinding.inflate(inflater, container, false)
        val view = binding.root
        parent = activity as SignedinActivity
        viewModel = ViewModelProvider(this).get(ChatPageViewModel::class.java)

        existingChatRoomId = ChatPageFragmentArgs.fromBundle(requireArguments()).chatRoom
        userUID = ChatPageFragmentArgs.fromBundle(requireArguments()).userUid

        viewModel.getOtherUser(userUID)
        setupAdapter()

        checkServerConnection().onEach {
            binding.sendMessageBtn.isEnabled = it
        }.launchIn(lifecycleScope)

        viewModel.actionBarVisibility.value = false

        firstMessageSent = false

        binding.darkOverlay.setOnClickListener {
            binding.darkOverlay.visibility = View.GONE
            binding.messageDetailParent.visibility = View.GONE
            binding.readByParent.visibility = View.GONE
        }

        viewModel.actionBarVisibility.observe(viewLifecycleOwner, Observer {
            if(it){
                binding.messageActionBar.visibility = View.VISIBLE
            } else {
                binding.messageActionBar.visibility = View.GONE
            }
        })

        // Chatroom initialisation
        newChatRoomId = MessageUtils.generateUID()
        newChatRoom = ChatRoom(newChatRoomId, hashMapOf<String, String>(userUID to userUID, viewModel.getCurrentUserId() to viewModel.getCurrentUserId()))

        if(existingChatRoomId == null) {
            binding.shimmerMessages.visibility = View.GONE
        }

        binding.chatBackButton.setOnClickListener {
            popNavigation()
        }

        binding.navTopClickable.setOnClickListener {
            navigateToDetail()
        }

        binding.actionBarSwitch.setOnClickListener {
            viewModel.toggleActionbarVisibility()
        }

        // send button
        binding.sendMessageBtn.setOnClickListener {
            val messageText = binding.messageEditText.text.toString().trim()

            // TODO: Amend to accommodate other message types
            val message = Message(MessageUtils.generateUID(), messageText, System.currentTimeMillis(), viewModel.getCurrentUserId(), MessageType.TEXT)

            if(binding.messageEditText.text.toString() != ""){
                viewModel.handleSendMessage(message, existingChatRoomId, newChatRoom) {
//                    updateReadReceipt()
                }
            }

            clearEditText()

            if(viewModel.state.value?.selectedChatRoom?.messages != null){
                binding.messagesRecyclerView.scrollToPosition(viewModel.state.value?.selectedChatRoom?.messages!!.size -1)
            }
        }

        binding.gallery.setOnClickListener {
            openImagePicker(this)
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        handleOnlineStatus()
    }

    private fun handleOnlineStatus(count: Int = 0) {
        Log.d("LLL", "Handle online status ran")
        if(count >= 10) {
            return
        }
        lifecycleScope.launch {
            if(viewModel.state.value?.selectedChatRoom?.participants != null) {
                checkLastOnline(parent, this@ChatPageFragment, viewModel.getOtherUserFromParticipants(viewModel.state.value!!.selectedChatRoom!!.participants), binding.root) {
                    binding.onlineStatus.setText(it)
                }
            } else {
                delay(3 * 1000L)
                handleOnlineStatus(count + 1)
            }
        }
    }

    override fun onPause() {
        super.onPause()
    }



    private fun collectState() {
        viewModel.state.observe(viewLifecycleOwner, Observer {
            if(existingChatRoomId != null) {
                viewModel.getChatRoom(existingChatRoomId!!)
            }

            if(it.selectedChatRoom != null) {
                getMessages(it.selectedChatRoom)
            }
        })

        viewModel.user.observe(viewLifecycleOwner, Observer {
            binding.nameText.text = it?.name
        })

        viewModel.state.observe(viewLifecycleOwner, Observer {

            if(existingChatRoomId == null){
                listenForSimultaneousFirstInitialisation(it.chatRooms)
            }
        })

    }

    private fun setupAdapter() {
        val fragment = this
        lifecycleScope.launch(Dispatchers.Main) {
            delay(300)
            messagesListAdapter = MessageListAdapter(existingChatRoomId ?: newChatRoomId, parent, fragment, requireContext(), this@ChatPageFragment, viewModel, null, ChatPageType.INDIVIDUAL,
                { message, image ->
//                    ImageUtils.displayImageMessage(image, message, parent)
                    displayChatImage(image, message, viewModel.user.value, parent)
                }, { _, _, _ ->

                }, { profileImage, user ->
                    displayProfileImage(profileImage, user, parent)
                }, { message, user ->
                    showMessageOptionsActionsheet(message, user)
                })
            binding.messagesRecyclerView.adapter = messagesListAdapter

            collectState()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        handleOnActivityResult(requestCode, resultCode, data, {
            uploadChatImage(it, existingChatRoomId ?: newChatRoomId, parent) { message, image ->
                lifecycleScope.launch {
                    storeImage_UseCase(image)
                    viewModel.handleSendMessage(message, existingChatRoomId, newChatRoom) {
//                        updateReadReceipt()
                    }
                }
            }
        },{})
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
            if(existingChatRoomId != null) {
                deleteMessage(message, existingChatRoomId!!).onEach {
                    when(it) {
                        is Response.Success -> {
//                            Log.d("LLL", "Deleted")
                        }
                        is Response.Loading -> {
//                            Log.d("LLL", "Deleting")
                        }
                        is Response.Fail -> {
//                            Log.d("LLL", "Failed to delete")
                        }
                    }
                }.launchIn(lifecycleScope)
            }
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
            if(existingChatRoomId != null) {
                deleteForAll(message, existingChatRoomId!!).onEach {
                    when(it) {
                        is Response.Success -> {
//                            Log.d("LLL", "Deleted")
                        }
                        is Response.Loading -> {
//                            Log.d("LLL", "Deleting")
                        }
                        is Response.Fail -> {
//                            Log.d("LLL", "Failed to delete")
                        }
                    }
                }.launchIn(lifecycleScope)
            }
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

    private fun scrollToBottom(){
        binding.messagesRecyclerView.scrollToPosition(messagesListAdapter.itemCount - 1)
    }

    private fun popNavigation(){
        viewModel.actionBarVisibility.value = false
        findNavController().navigateUp()
    }

    private fun getMessages(it: ChatRoom?){
        if(existingChatRoomId != null){
            val sorted = it?.messages?.values?.sortedBy {
                it.time
            }

            setShimmerVisibility(it)

            messagesListAdapter.submitList(sorted)
            messagesListAdapter.notifyDataSetChanged()
        }
    }

    private fun setShimmerVisibility(chatRoom: ChatRoom?){
        if(chatRoom == null){
            binding.shimmerMessages.visibility = View.VISIBLE
        } else {
            binding.shimmerMessages.visibility = View.GONE
        }
    }

    private fun getMessagesDelayed(chatRoom: ChatRoom?){
        if(existingChatRoomId != null){
            val sorted = chatRoom?.messages?.values?.sortedBy {
                it.time
            }
            messagesListAdapter.submitList(sorted)
        }
    }

    private fun navigateToDetail(){
        if(existingChatRoomId != null){
            val action = ChatPageFragmentDirections.actionChatPageFragmentToAboutUserFragment(existingChatRoomId!!, userUID)
            findNavController().navigate(action)
        } else {
            val action = ChatPageFragmentDirections.actionChatPageFragmentToAboutUserFragment(null, userUID)
            findNavController().navigate(action)
        }
    }

    // This function listens for messages from the other end if the chat page is open without messages
    // only fires up when there is a change to the current user
    private fun listenForSimultaneousFirstInitialisation(chatRooms: List<ChatRoom>){
//        Log.d("LLL", "Listen for simultaneous init ran")
        // check chatrooms which have both the current user
        val chatRoom = findChatRoom(userUID, chatRooms)
        if(chatRoom != null){
            // if found, set the existingChatRoomId to that chatroom
            existingChatRoomId = chatRoom.roomUID
        }
    }

    private fun clearEditText(){
        binding.messageEditText.setText("")
    }

    override fun onDestroyView() {
        super.onDestroyView()

//        updateReadReceipt()

        _binding = null
    }
}
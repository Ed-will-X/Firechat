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
import com.varsel.firechat.databinding.FragmentChatPageBinding
import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.Message.Message
import com.varsel.firechat.data.local.Message.MessageType
import com.varsel.firechat.data.local.ReadReceipt.ReadReceipt
import com.varsel.firechat.domain.use_case.chat_image.DisplayChatImage_UseCase
import com.varsel.firechat.domain.use_case.chat_image.UploadChatImage_UseCase
import com.varsel.firechat.domain.use_case.current_user.CheckServerConnectionUseCase
import com.varsel.firechat.domain.use_case.image.HandleOnActivityResult_UseCase
import com.varsel.firechat.domain.use_case.image.OpenImagePicker_UseCase
import com.varsel.firechat.domain.use_case.profile_image.DisplayProfileImage
import com.varsel.firechat.common._utils.MessageUtils
import com.varsel.firechat.domain.use_case.chat_image.StoreChatImageUseCase
import com.varsel.firechat.domain.use_case.chat_room.FindChatRoom_UseCase
import com.varsel.firechat.domain.use_case.read_receipt.FetchReceipt_UseCase
import com.varsel.firechat.domain.use_case.read_receipt.StoreReceipt_UseCase
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.adapters.ChatPageType
import com.varsel.firechat.presentation.signedIn.adapters.MessageListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateReadReceipt()
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
                    updateReadReceipt()
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
            messagesListAdapter = MessageListAdapter(existingChatRoomId ?: newChatRoomId, parent, fragment, requireContext(), this@ChatPageFragment, viewModel, ChatPageType.INDIVIDUAL,
                { message, image ->
//                    ImageUtils.displayImageMessage(image, message, parent)
                    displayChatImage(image, message, viewModel.user.value, parent)
                }, { _, _, _ ->

                }, { profileImage, user ->
                    displayProfileImage(profileImage, user, parent)
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
                        updateReadReceipt()
                    }
                }
            }
        },{})
    }

    private fun updateReadReceipt(){
        if(existingChatRoomId != null){
            val receipt = ReadReceipt("${existingChatRoomId}:${viewModel.getCurrentUserId()}", System.currentTimeMillis())
            lifecycleScope.launch {
                storeReceipt(receipt)
            }
        } else {
            if(firstMessageSent == true){
                val receipt = ReadReceipt("${newChatRoomId}:${viewModel.getCurrentUserId()}", System.currentTimeMillis())
                lifecycleScope.launch {
                    storeReceipt(receipt)
                }
            }
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
        Log.d("LLL", "Listen for simultaneous init ran")
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

        updateReadReceipt()

        _binding = null
    }
}
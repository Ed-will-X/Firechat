package com.varsel.firechat.view.signedIn.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.varsel.firechat.databinding.FragmentChatPageBinding
import com.varsel.firechat.model.Chat.ChatRoom
import com.varsel.firechat.model.Message.Message
import com.varsel.firechat.model.Message.MessageType
import com.varsel.firechat.model.ReadReceipt.ReadReceipt
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.LifecycleUtils
import com.varsel.firechat.utils.MessageUtils
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedIn.adapters.ChatPageType
import com.varsel.firechat.view.signedIn.adapters.MessageListAdapter
import com.varsel.firechat.viewModel.ChatPageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener


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
    val chatPageViewModel: ChatPageViewModel by activityViewModels()

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

        LifecycleUtils.observeInternetStatus(parent.firebaseViewModel, this, {
            binding.sendMessageBtn.isEnabled = true
        }, {
            binding.sendMessageBtn.isEnabled = false
        })

        chatPageViewModel.actionBarVisibility.value = false

        firstMessageSent = false

        existingChatRoomId = ChatPageFragmentArgs.fromBundle(requireArguments()).chatRoom
        userUID = ChatPageFragmentArgs.fromBundle(requireArguments()).userUid

        getChatRoom()
        observeUserProps()

        KeyboardVisibilityEvent.setEventListener(
            parent,
            object : KeyboardVisibilityEventListener {
                override fun onVisibilityChanged(isOpen: Boolean) {

                }
            }
        )

        parent.firebaseViewModel.chatRooms.observe(viewLifecycleOwner, Observer {
            // Listen for first message from the other user
            if(existingChatRoomId == null){
                listenForSimultaneousFirstInitialisation(it)
            }
        })

        chatPageViewModel.actionBarVisibility.observe(viewLifecycleOwner, Observer {
            if(it){
                binding.messageActionBar.visibility = View.VISIBLE
            } else {
                binding.messageActionBar.visibility = View.GONE
            }
        })

        // Chatroom initialisation
        newChatRoomId = MessageUtils.generateUID(30)
        newChatRoom = ChatRoom(newChatRoomId, hashMapOf<String, String>(userUID to userUID, parent.firebaseAuth.uid.toString() to parent.firebaseAuth.uid.toString()))

        val fragment = this
        lifecycleScope.launch(Dispatchers.Main) {
            delay(300)
            messagesListAdapter = MessageListAdapter(existingChatRoomId ?: newChatRoomId, parent, fragment, requireContext(), ChatPageType.INDIVIDUAL, parent.firebaseViewModel,
                { message, image ->
                    ImageUtils.displayImageMessage(image, message, parent)
                }, { _, _, _ ->

                }, { profileImage, user ->
                    ImageUtils.displayProfilePicture(profileImage, user, parent)
                })
            binding.messagesRecyclerView.adapter = messagesListAdapter

            parent.firebaseViewModel.selectedChatRoom.observe(viewLifecycleOwner, Observer {
                getMessages(it)
            })
        }

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
            chatPageViewModel.toggleActionbarVisibility()
        }

        // send button
        binding.sendMessageBtn.setOnClickListener {
            val messageText = binding.messageEditText.text.toString().trim()

            // TODO: Amend to accommodate other message types
            val message = Message(MessageUtils.generateUID(30), messageText, System.currentTimeMillis(), parent.firebaseAuth.currentUser!!.uid, MessageType.TEXT)

            if(binding.messageEditText.text.toString() != ""){
                sendMessage(message) {
                    updateReadReceipt()
                }
            }

            clearEditText()

            if(parent.firebaseViewModel.selectedChatRoom.value?.messages != null){
                binding.messagesRecyclerView.scrollToPosition(parent.firebaseViewModel.selectedChatRoom.value?.messages!!.size -1)
            }
        }

        binding.gallery.setOnClickListener {
            ImageUtils.openImagePicker(this)
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        ImageUtils.handleOnActivityResult(requireContext(), requestCode, resultCode, data, {
            ImageUtils.uploadChatImage(it, existingChatRoomId ?: newChatRoomId, parent) { message, image ->
                parent.imageViewModel.storeImage(image) {
                    sendMessage(message) {
                        updateReadReceipt()
                    }
                }
            }
        },{})
    }

    private fun updateReadReceipt(){
        if(existingChatRoomId != null){
            val receipt = ReadReceipt(existingChatRoomId!!, System.currentTimeMillis(), parent.firebaseAuth.currentUser!!.uid)
            parent.readReceiptViewModel.storeReceipt(receipt)
        } else {
            if(firstMessageSent == true){
                val receipt = ReadReceipt(newChatRoomId, System.currentTimeMillis(), parent.firebaseAuth.currentUser!!.uid)
                parent.readReceiptViewModel.storeReceipt(receipt)
            }
        }
    }

    private fun scrollToBottom(){
        binding.messagesRecyclerView.scrollToPosition(messagesListAdapter.itemCount - 1)
    }


    private fun popNavigation(){
        chatPageViewModel.actionBarVisibility.value = false
        findNavController().navigateUp()
    }
    private fun observeUserProps(){
//        parent.profileImageViewModel.selectedOtherUserProfilePicChat.observe(viewLifecycleOwner, Observer {
//            if(it != null){
//                ImageUtils.setProfilePic(it, binding.profileImage, binding.profileImageParent)
//            }
//        })

        parent.firebaseViewModel.selectedChatRoomUser.observe(viewLifecycleOwner, Observer {
            binding.nameText.text = it?.name
        })
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

    private fun getChatRoom(){
        if(existingChatRoomId != null){
            parent.firebaseViewModel.getChatRoomRecurrent(existingChatRoomId!!, parent.mDbRef, {
                parent.firebaseViewModel.selectedChatRoom.value = it
            },{})
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

    private fun getChatRoomFirst(){
        // Gets the chat room based on the generated id
        parent.firebaseViewModel.getChatRoomRecurrent(newChatRoomId, parent.mDbRef, {
            parent.firebaseViewModel.selectedChatRoom.value = it
        },{})
    }

    private fun sendMessage(message: Message, success: ()-> Unit){
        if(existingChatRoomId != null){
            // Existing chat room
            parent.firebaseViewModel.sendMessage(message, existingChatRoomId!!, parent.mDbRef, {
                success()
            },{})

        } else {
            // create chat room in both users
            parent.firebaseViewModel.appendChatRoom(newChatRoomId, userUID, parent.firebaseAuth, parent.mDbRef, {
                if(firstMessageSent == false){
                    parent.firebaseViewModel.appendParticipants(newChatRoom, parent.mDbRef, {
                        parent.firebaseViewModel.sendMessage(message, newChatRoomId, parent.mDbRef, {
                            success()
                            firstMessageSent = true
                            getChatRoomFirst()
                        }, {})
                    }, {})
                } else {
                    parent.firebaseViewModel.sendMessage(message, newChatRoomId, parent.mDbRef, {
                        success()
                    }, {})
                }
            },{})
            // append to that chat room in the chats repo

        }
    }

    // This function listens for messages from the other end if the chat page is open without messages
    // only fires up when there is a change to the current user
    private fun listenForSimultaneousFirstInitialisation(chatRooms: List<ChatRoom?>){
        // check chatrooms which have both the current user
        val chatRoom = parent.signedinViewModel.findChatRoom(userUID, chatRooms as MutableList<ChatRoom?>)
        if(chatRoom != null){
            // if found, set the existingChatRoomId to that chatroom
            existingChatRoomId = chatRoom.roomUID
            getChatRoom()
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
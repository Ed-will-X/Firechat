package com.varsel.firechat.presentation.signedIn.fragments.screens.chat_search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.varsel.firechat.databinding.FragmentChatsSearchBinding
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.common._utils.UserUtils
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.adapters.ChatSearchAdapter
import java.lang.IllegalArgumentException

class ChatsSearchFragment : Fragment() {
    private var _binding: FragmentChatsSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatsSearchAdapter: ChatSearchAdapter
    private lateinit var parent: SignedinActivity
    private val chatSearchViewModel: ChatSearchViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatsSearchBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        parent = activity as SignedinActivity

        chatsSearchAdapter = ChatSearchAdapter(parent, this, { chatRoom, profileImage, user ->
            navigateToChatPage(chatRoom.roomUID, user, profileImage?.image)
        }, { groupRoom, profileImage ->
           navigateToGroupChatPage(groupRoom.roomUID, profileImage)
        }, { profileImage, user ->
            ImageUtils.displayProfilePicture(profileImage, user, parent)
        }, { profileImage, groupRoom ->
            ImageUtils.displayGroupImage(profileImage, groupRoom, parent)
        })
        binding.chatSearchRecyclerView.adapter = chatsSearchAdapter

        binding.clearText.setOnClickListener {
            binding.searchBox.setText("")
        }

        setPropsInVM {
            setupSearchBar()
        }

        return view
    }

    private fun clearResults() {
        chatSearchViewModel.results.value = mutableListOf()
        chatsSearchAdapter.results = mutableListOf()
        binding.searchBox.setText("")

        chatsSearchAdapter.notifyDataSetChanged()
    }

    private fun navigateToGroupChatPage(groupId: String, image: ProfileImage?){
        try {
            val action = ChatsSearchFragmentDirections.actionChatsSearchFragmentToGroupChatPageFragment(groupId)
            view?.findNavController()?.navigate(action)
            parent.profileImageViewModel.selectedGroupImage.value = image
        } catch (e: IllegalArgumentException){ }
    }

    private fun navigateToChatPage(chatRoomId: String, user: User, base64: String?) {
        try {
            val action = ChatsSearchFragmentDirections.actionChatsSearchFragmentToChatPageFragment(chatRoomId, user.userUID)

            view?.findNavController()?.navigate(action)

            parent.profileImageViewModel.selectedOtherUserProfilePicChat.value = base64
            parent.firebaseViewModel.selectedChatRoomUser.value = user
        } catch (e: IllegalArgumentException){

        }
    }

    private fun setPropsInVM(afterCallback: ()-> Unit){
        val users = mutableListOf<User>()
        val chatRooms = parent.firebaseViewModel.chatRooms.value
        if (chatRooms != null) {
            for(i in chatRooms){
                val otherUserId = UserUtils.getOtherUserId(i.participants ?: hashMapOf(), parent)

                UserUtils.getUser(otherUserId, parent, {
                    users.add(it)
                }, {
                    chatSearchViewModel.searchables.value?.addAll(users ?: mutableListOf())
                    afterCallback()
                })
            }
        }

        chatSearchViewModel.searchables.value = mutableListOf()
        chatSearchViewModel.searchables.value?.addAll(parent.firebaseViewModel.groupRooms.value ?: mutableListOf())
    }

    private fun setupSearchBar(){
        val searchables = chatSearchViewModel.searchables.value
        binding.searchBox.doAfterTextChanged {
            chatSearchViewModel.results.value = mutableListOf()
            toggleClearTextVisibility(it?.length ?: 0)

            if (it.toString() == ""){
                chatsSearchAdapter.results = mutableListOf()
                chatsSearchAdapter.notifyDataSetChanged()
                chatSearchViewModel.results.value = mutableListOf()

                return@doAfterTextChanged
            }

//            Log.d("LLL", "${searchables?.count()}")
            for(i in searchables ?: mutableListOf()){
                chatSearchViewModel.results.value = UserUtils.searchListOfUsers_andGroups(it.toString(), searchables ?: mutableListOf())
                submitResults()
            }

        }
    }

    private fun submitResults(){
        val results = chatSearchViewModel.results.value
        for(i in results ?: mutableListOf()){
            if (i is User){
                findChatRoom(i)
            }
        }
        chatsSearchAdapter.results = mutableListOf()

        chatsSearchAdapter.results = results?.toMutableList() ?: mutableListOf()
        chatsSearchAdapter.notifyDataSetChanged()
    }

    private fun findChatRoom(user: User){
        val chatRooms = parent.firebaseViewModel.chatRooms.value
        val results = chatSearchViewModel.results.value

        for(i in chatRooms ?: mutableListOf()){
            if(UserUtils.getOtherUserId(i.participants ?: hashMapOf(), parent) == user.userUID){
                results?.remove(user)
                results?.add(i)
            }
        }
    }

    private fun toggleClearTextVisibility(wordCount: Int){
        if(wordCount > 0){
            binding.clearText.visibility = View.VISIBLE
        } else {
            binding.clearText.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}
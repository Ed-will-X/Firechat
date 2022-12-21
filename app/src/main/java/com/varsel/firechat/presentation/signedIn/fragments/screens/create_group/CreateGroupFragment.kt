package com.varsel.firechat.presentation.signedIn.fragments.screens.create_group

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.databinding.ActionsheetCreateGroupBinding
import com.varsel.firechat.databinding.FragmentCreateGroupBinding
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.LifecycleUtils
import com.varsel.firechat.utils.MessageUtils
import com.varsel.firechat.utils.SearchUtils
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.adapters.CreateGroupAdapter

class CreateGroupFragment : Fragment() {
    private var _binding: FragmentCreateGroupBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private lateinit var adapter: CreateGroupAdapter
    private val createGroupViewModel: CreateGroupViewModel by activityViewModels()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCreateGroupBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        parent = activity as SignedinActivity

        observeInternetStatus()

        SearchUtils.setupSearchBar(
            binding.clearText,
            binding.searchBox,
            this,
            binding.noFriends,
            binding.noMatch,
            binding.friendsRecyclerView,
            parent.firebaseViewModel.friends,
            {

            },
            {
                submitListToAdapter(it)
            }
        )


        adapter = CreateGroupAdapter(parent, {
            toggleBtnEnable()
        }, { profileImage, user ->
            ImageUtils.displayProfilePicture(profileImage, user, parent)
        })
        binding.friendsRecyclerView.adapter = adapter

        // set selected group image to null
//        parent.profileImageViewModel.selectedGroupImageEncoded.value = null
        parent.profileImageViewModel.selectedGroupImage.value = null

        val friends = parent.firebaseViewModel.friends.value
        if(friends != null && friends?.isNotEmpty() == true){
            submitListToAdapter(parent.firebaseViewModel.friends.value!!)
        } else {

        }

        binding.createGroupBtn.setOnClickListener {
            showActionsheet()
        }

        binding.doneClickable.setOnClickListener {
            showActionsheet()
        }

        binding.backButton.setOnClickListener {
            popNavigation()
        }

        return view
    }

    private fun submitListToAdapter(list: List<User?>){
        adapter.friends = arrayListOf()

        if(list != null && list.isNotEmpty()){
            adapter.friends = list.toMutableList()

//            binding.friendsRecyclerView.visibility = View.VISIBLE
//            binding.noMatch.visibility = View.GONE
//            binding.noFriends.visibility = View.GONE


            adapter?.notifyDataSetChanged()
        }
    }

    private fun observeInternetStatus(){
        LifecycleUtils.observeInternetStatus(parent, this, {
            if(adapter.selected.count() > 0){
                binding.createGroupBtn.isEnabled = true
            }
        }, {
            binding.createGroupBtn.isEnabled = false
        })
    }

    private fun toggleBtnEnable(){
        if(parent.firebaseViewModel.isConnectedToDatabase.value == true){
            if(adapter.selected.count() > 0){
                binding.createGroupBtn.isEnabled = true
                binding.doneClickable.visibility = View.VISIBLE
            } else {
                binding.createGroupBtn.isEnabled = false
                binding.doneClickable.visibility = View.GONE
            }
        }
    }

    private fun popNavigation(){
        findNavController().navigateUp()
    }

    private fun showActionsheet(){
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = ActionsheetCreateGroupBinding.inflate(layoutInflater, binding.root, false)
        dialog.setContentView(dialogBinding.root)

        LifecycleUtils.observeInternetStatus(parent, this, {
            dialogBinding.btnCreateGroup.isEnabled = true
        }, {
            dialogBinding.btnCreateGroup.isEnabled = false
        })

        dialogBinding.groupName.doAfterTextChanged {
            if(createGroupViewModel.hasBtnBeenClicked.value == false){
                dialogBinding.btnCreateGroup.isEnabled = !dialogBinding.groupName.text.isEmpty()
            } else {
                dialogBinding.btnCreateGroup.isEnabled = false
            }
        }

        dialogBinding.btnCreateGroup.setOnClickListener {
            dialogBinding.btnCreateGroup.isEnabled = false
            createGroupViewModel.hasBtnBeenClicked.value = true

            val newRoomId = MessageUtils.generateUID(30)
            val participants = addParticipants()
            val groupNameText = dialogBinding.groupName.text.toString().trim()
            val group = GroupRoom(newRoomId, participants, groupNameText, makeCurrentUserAdmin())

            appendRoomIdToUsers(participants.values.toList(), newRoomId) {
                parent.firebaseViewModel.createGroup(group, parent.mDbRef, parent.firebaseAuth, {
                    navigateToGroupPage(newRoomId)
                    dialogBinding.btnCreateGroup.isEnabled = true
                    createGroupViewModel.hasBtnBeenClicked.value = false

                    dialog.dismiss()
                }, {
                    dialogBinding.btnCreateGroup.isEnabled = true
                    createGroupViewModel.hasBtnBeenClicked.value = false
                })
            }
        }

        dialog.show()
    }

    private fun addParticipants(): HashMap<String, String>{
        val participants = adapter.selected.associateWith { it } as HashMap<String, String>

        val currentUser: String = parent.firebaseAuth.currentUser?.uid.toString()
        participants.put(currentUser, currentUser)

        return participants
    }

    private fun navigateToGroupPage(newRoomId: String){
        val action = CreateGroupFragmentDirections.actionCreateGroupFragmentToGroupChatPageFragment(newRoomId)
        view?.findNavController()?.navigate(action)
    }

    private fun makeCurrentUserAdmin(): HashMap<String, String>{
        val admins: HashMap<String, String> = HashMap()
        val currentUser: String = parent.firebaseAuth.currentUser?.uid.toString()

        admins.put(currentUser, currentUser)

        return admins
    }

    private fun appendRoomIdToUsers(users: List<String>, newRoomId: String, afterCallback: ()-> Unit){
        for (i in users){
            parent.firebaseViewModel.appendGroupRoomsToUser(newRoomId, i, parent.firebaseAuth, parent.mDbRef, {}, {})
        }
        afterCallback()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
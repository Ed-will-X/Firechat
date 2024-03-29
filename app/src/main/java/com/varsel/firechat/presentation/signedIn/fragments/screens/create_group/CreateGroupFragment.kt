package com.varsel.firechat.presentation.signedIn.fragments.screens.create_group

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.databinding.ActionsheetCreateGroupBinding
import com.varsel.firechat.databinding.FragmentCreateGroupBinding
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.use_case.current_user.CheckServerConnectionUseCase
import com.varsel.firechat.domain.use_case.profile_image.DisplayProfileImage
import com.varsel.firechat.common._utils.MessageUtils
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.adapters.CreateGroupAdapter
import com.varsel.firechat.common._utils.ExtensionFunctions.Companion.collectLatestLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class CreateGroupFragment : Fragment() {
    private var _binding: FragmentCreateGroupBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private lateinit var adapter: CreateGroupAdapter
    private lateinit var viewModel: CreateGroupViewModel

    @Inject
    lateinit var displayProfileImage: DisplayProfileImage

    @Inject
    lateinit var checkServerConnection: CheckServerConnectionUseCase

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCreateGroupBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        parent = activity as SignedinActivity

        viewModel = ViewModelProvider(this).get(CreateGroupViewModel::class.java)

        initialiseAdapter(viewModel.getFriends().value.data ?: listOf())
        observeInternetStatus()
        collectState()
        setupSearchBar()
        setClickListeners()

        return view
    }

    private fun collectState() {
        collectLatestLifecycleFlow(viewModel.state) {
            if(it.friends != null) {

            }
        }
    }

    private fun setClickListeners() {
        binding.createGroupBtn.setOnClickListener {
            showActionsheet()
        }

        binding.doneClickable.setOnClickListener {
            showActionsheet()
        }

        binding.backButton.setOnClickListener {
            popNavigation()
        }
    }

    private fun setupSearchBar() {
        viewModel.setupSearchBarUseCase(
            binding.clearText,
            binding.searchBox,
            this,
            binding.noFriends,
            binding.noMatch,
            binding.friendsRecyclerView,
            viewModel.friends,
            {

            },
            {
                submitListToAdapter(it)
            }
        )
    }

    private fun initialiseAdapter(friends: List<User>) {
        adapter = CreateGroupAdapter(parent, this, viewModel, {
            toggleBtnEnable()
        }, { profileImage, user ->
            displayProfileImage(profileImage, user, parent)
        })
        binding.friendsRecyclerView.adapter = adapter

        submitListToAdapter(friends)
    }

    private fun submitListToAdapter(list: List<User?>){
        adapter.friends = arrayListOf()

        if(list.isNotEmpty()){
            adapter.friends = list.toMutableList()

            adapter.notifyDataSetChanged()
        }
    }

    private fun observeInternetStatus(){
        checkServerConnection().onEach {
            if(it){
                if(viewModel.selected.value?.count()!! > 0){
                    binding.createGroupBtn.isEnabled = true
                } else {
                    binding.createGroupBtn.isEnabled = false
                }
            } else {
                binding.createGroupBtn.isEnabled = false
            }
        }.launchIn(lifecycleScope)
    }

    private fun toggleBtnEnable(){
        if(viewModel.checkServerConnectionUseCase().value == true){
            if(viewModel.selected.value!!.count() > 0){
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

        checkServerConnection().onEach {
            dialogBinding.btnCreateGroup.isEnabled = it
        }.launchIn(lifecycleScope)

        dialogBinding.groupName.doAfterTextChanged {
            if(viewModel.hasBtnBeenClicked.value == false){
                dialogBinding.btnCreateGroup.isEnabled = !dialogBinding.groupName.text.isEmpty()
            } else {
                dialogBinding.btnCreateGroup.isEnabled = false
            }
        }

        dialogBinding.btnCreateGroup.setOnClickListener {
            dialogBinding.btnCreateGroup.isEnabled = false
            viewModel.hasBtnBeenClicked.value = true

            val newRoomId = MessageUtils.generateUID()
            val participants = addParticipants()
            val groupNameText = dialogBinding.groupName.text.toString().trim()
            val group = GroupRoom(newRoomId, participants, groupNameText, makeCurrentUserAdmin())

            appendRoomIdToUsers(participants.values.toList(), group) {
                viewModel.createGroup(group, {
                    navigateToGroupPage(newRoomId)
                    dialogBinding.btnCreateGroup.isEnabled = true
                    viewModel.hasBtnBeenClicked.value = false

                    dialog.dismiss()
                }, {
                    dialogBinding.btnCreateGroup.isEnabled = true
                    viewModel.hasBtnBeenClicked.value = false
                })
            }
        }

        dialog.show()
    }

    private fun addParticipants(): HashMap<String, String>{
        val participants = viewModel.selected.value!!.associateWith { it } as HashMap<String, String>

        val currentUser: String = viewModel.getCurrentUserId()
        participants.put(currentUser, currentUser)

        return participants
    }

    private fun navigateToGroupPage(newRoomId: String){
        val action = CreateGroupFragmentDirections.actionCreateGroupFragmentToGroupChatPageFragment(newRoomId)
        view?.findNavController()?.navigate(action)
    }

    private fun makeCurrentUserAdmin(): HashMap<String, String>{
        val admins: HashMap<String, String> = HashMap()
        val currentUser: String = viewModel.getCurrentUserId()

        admins.put(currentUser, currentUser)

        return admins
    }

    private fun appendRoomIdToUsers(users: List<String>, groupRoom: GroupRoom, afterCallback: ()-> Unit){
        for (i in users){
            viewModel.appendGroupIdToUser(groupRoom, i)
        }
        afterCallback()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
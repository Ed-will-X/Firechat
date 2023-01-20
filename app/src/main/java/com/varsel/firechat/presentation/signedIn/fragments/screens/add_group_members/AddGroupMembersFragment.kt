package com.varsel.firechat.presentation.signedIn.fragments.screens.add_group_members

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.varsel.firechat.common.Response
import com.varsel.firechat.databinding.FragmentAddGroupMembersBinding
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.use_case.profile_image.DisplayProfileImage
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.adapters.AddGroupMembersAdapter
import com.varsel.firechat.common._utils.ExtensionFunctions.Companion.collectLatestLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class AddGroupMembersFragment : Fragment() {
    private var _binding: FragmentAddGroupMembersBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AddGroupMembersAdapter
    private lateinit var parent: SignedinActivity
    lateinit var groupId: String
    lateinit var viewModel: AddGroupMembersViewModel

    @Inject
    lateinit var displayProfileImage: DisplayProfileImage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddGroupMembersBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        groupId = AddGroupMembersFragmentArgs.fromBundle(requireArguments()).groupId
        parent = activity as SignedinActivity
        viewModel = ViewModelProvider(this).get(AddGroupMembersViewModel::class.java)

        viewModel.getGroupChat(groupId)
        setupAdapter()

        collectState()
        setupSearchBar()

        setOnClickListeners()

        return view
    }

    private fun collectState() {
//        viewModel.non_participants.observe(viewLifecycleOwner, Observer {
//            addToRecyclerView(it)
//        })

        collectLatestLifecycleFlow(viewModel.non_participants) {
            addToRecyclerView(it)
        }
    }

    private fun setupAdapter() {
        adapter = AddGroupMembersAdapter(parent, this, viewModel, {
            toggleBtnEnable()
        }, { profileImage, user ->
            displayProfileImage(profileImage, user, parent)
        })
        binding.usersRecyclerView.adapter = adapter

        addToRecyclerView(viewModel.non_participants.value)
    }

    private fun setupSearchBar() {
        viewModel.setupSearchBarUseCase(
            binding.addFriendsCancelButton,
            binding.searchBox,
            this,
            binding.noFriends,
            binding.noMatch,
            binding.usersRecyclerView,
            viewModel.non_participants,
            {},{
                addToRecyclerView(it)
            }
        )
    }

    private fun addToRecyclerView(non_participants: List<User>){
        adapter.users = arrayListOf()
        if(non_participants.isNotEmpty()){
            adapter.users = non_participants.toMutableList()

//            binding.usersRecyclerView.visibility = View.VISIBLE
//            binding.noMatch.visibility = View.GONE
//            binding.noFriends.visibility = View.GONE

            adapter.notifyDataSetChanged()
        }
    }

    private fun setOnClickListeners(){
        binding.addMembersBtn.setOnClickListener {
            addParticipants()
        }

        binding.doneClickable.setOnClickListener {
            addParticipants()
        }

        binding.backButton.setOnClickListener {
            popNavigation()
        }
    }

    private fun toggleBtnEnable(){
        if(adapter.selected.count() > 0){
            binding.addMembersBtn.isEnabled = true
            binding.doneClickable.visibility = View.VISIBLE
        } else {
            binding.addMembersBtn.isEnabled = false
            binding.doneClickable.visibility = View.GONE
        }
    }

    private fun addParticipants(){
        val selected_copy = adapter.selected.toList()
        binding.addMembersBtn.isEnabled = false
        binding.darkOverlay.visibility = View.VISIBLE

        viewModel.addGroupMembers(selected_copy, groupId).onEach {
            when(it) {
                is Response.Success -> {
                    popNavigation()
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun popNavigation(){
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
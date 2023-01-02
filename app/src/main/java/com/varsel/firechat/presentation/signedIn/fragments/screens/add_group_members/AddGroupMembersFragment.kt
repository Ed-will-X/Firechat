package com.varsel.firechat.presentation.signedIn.fragments.screens.add_group_members

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.varsel.firechat.databinding.FragmentAddGroupMembersBinding
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.SearchUtils
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.adapters.AddGroupMembersAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddGroupMembersFragment : Fragment() {
    private var _binding: FragmentAddGroupMembersBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AddGroupMembersAdapter
    private lateinit var parent: SignedinActivity
    lateinit var groupId: String
    lateinit var viewModel: AddGroupMembersViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddGroupMembersBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        groupId = AddGroupMembersFragmentArgs.fromBundle(requireArguments()).groupId
        parent = activity as SignedinActivity
        viewModel = ViewModelProvider(this).get(AddGroupMembersViewModel::class.java)

        SearchUtils.setupSearchBar(
            binding.addFriendsCancelButton,
            binding.searchBox,
            this,
            binding.noFriends,
            binding.noMatch,
            binding.usersRecyclerView,
            parent.firebaseViewModel.selectedGroup_nonParticipants,
            {},{
                addToRecyclerView(it)
            }
        )

        adapter = AddGroupMembersAdapter(parent, this, viewModel, {
            toggleBtnEnable()
        }, { profileImage, user ->
            ImageUtils.displayProfilePicture(profileImage, user, parent)
        })
        binding.usersRecyclerView.adapter = adapter

        val non_participants = parent.firebaseViewModel.selectedGroup_nonParticipants.value
        if (non_participants != null) {
            addToRecyclerView(non_participants)
        }

        setOnClickListeners()

        return view
    }

    private fun addToRecyclerView(non_participants: List<User?>){
        adapter.users = arrayListOf()
        if(non_participants != null && non_participants.isNotEmpty()){
            adapter.users.addAll(non_participants)

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

        parent.firebaseViewModel.addGroupMembers(selected_copy, parent.firebaseAuth.uid!!, groupId, parent.mDbRef, {
        }, {
           binding.addMembersBtn.isEnabled = true
            binding.darkOverlay.visibility = View.GONE
        }, {
            popNavigation()
        })
    }

    private fun popNavigation(){
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
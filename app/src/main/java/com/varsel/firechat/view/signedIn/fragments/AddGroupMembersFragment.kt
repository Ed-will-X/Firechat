package com.varsel.firechat.view.signedIn.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.varsel.firechat.databinding.FragmentAddGroupMembersBinding
import com.varsel.firechat.model.User.User
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedIn.adapters.AddGroupMembersAdapter

class AddGroupMembersFragment : Fragment() {
    private var _binding: FragmentAddGroupMembersBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AddGroupMembersAdapter
    private lateinit var parent: SignedinActivity
    lateinit var groupId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddGroupMembersBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        groupId = AddGroupMembersFragmentArgs.fromBundle(requireArguments()).groupId
        parent = activity as SignedinActivity
        adapter = AddGroupMembersAdapter {
            toggleBtnEnable()
        }
        binding.usersRecyclerView.adapter = adapter

        parent.firebaseViewModel.friends.observe(viewLifecycleOwner, Observer {
            if(it != null){
                val non_participants = getNonParticipants()
                adapter.users.addAll(non_participants)
                adapter.notifyDataSetChanged()
            }
        })

        setOnClickListeners()

        return view
    }

    // TODO: Only add the non-participants to the list
    private fun getNonParticipants(): List<User>{
        val participants: List<String> = parent.firebaseViewModel.selectedGroupRoom.value!!.participants?.values!!.toList()
        val friends: MutableList<User> = parent.firebaseViewModel.friends.value as MutableList<User>
        val non_participants = mutableListOf<User>()

        for((i_index, i_value) in friends.withIndex()){
            for((j_index, j_value) in participants.withIndex()){
                if(i_value.userUID == j_value){
                    break
                } else if(j_index == participants.size -1) {
                    non_participants.add(i_value)
                }
            }
        }

        return non_participants

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
        Log.d("LLL", "${adapter.selected}")
        parent.firebaseViewModel.addGroupMembers(adapter.selected, parent.firebaseAuth.uid!!, groupId, parent.mDbRef, {
           Log.d("LLL", "${it} - successful")
        }, {
            Log.d("LLL", "${it} - failure")
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
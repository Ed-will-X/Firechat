package com.varsel.firechat.view.signedIn.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentCreateGroupBinding
import com.varsel.firechat.model.Chat.GroupRoom
import com.varsel.firechat.model.User.User
import com.varsel.firechat.utils.LifecycleUtils
import com.varsel.firechat.utils.MessageUtils
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedIn.adapters.CreateGroupAdapter

class CreateGroupFragment : Fragment() {
    private var _binding: FragmentCreateGroupBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private lateinit var adapter: CreateGroupAdapter

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCreateGroupBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        parent = activity as SignedinActivity

        LifecycleUtils.observeInternetStatus(parent.firebaseViewModel, this, {
            if(adapter.selected.count() > 0){
                binding.createGroupBtn.isEnabled = true
            }
        }, {
            binding.createGroupBtn.isEnabled = false
        })

        adapter = CreateGroupAdapter(parent) {
            toggleBtnEnable()
        }
        binding.friendsRecyclerView.adapter = adapter

        // set selected group image to null
        parent.profileImageViewModel.selectedGroupImageEncoded.value = null

        if(parent.firebaseViewModel.friends.value?.isNotEmpty() == true){
            adapter.friends = parent.firebaseViewModel.friends.value as ArrayList<User?>
            adapter.notifyDataSetChanged()
        } else {

        }

        binding.createGroupBtn.setOnClickListener {
            showActionsheet()
        }

        binding.doneClickable.setOnClickListener {
            showActionsheet()
        }

        return view
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

    private fun showActionsheet(){
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(R.layout.actionsheet_create_group)
        val groupName = dialog.findViewById<EditText>(R.id.group_name)
        val btn = dialog.findViewById<Button>(R.id.btn_create_group)

        groupName?.doAfterTextChanged {
            btn?.isEnabled = !groupName.text.isEmpty()
        }

        // TODO: Disable if <1 is selected
        btn?.setOnClickListener {
            val newRoomId = MessageUtils.generateUID(50)
            val participants = addParticipants()
            val groupNameText = groupName?.text.toString().trim()
            val group = GroupRoom(newRoomId, participants, groupNameText, makeCurrentUserAdmin())

            appendRoomIdToUsers(participants.values.toList(), newRoomId) {
                parent.firebaseViewModel.createGroup(group, parent.mDbRef, parent.firebaseAuth, {
                    navigateToGroupPage(newRoomId)

                    dialog.dismiss()
                }, {})
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
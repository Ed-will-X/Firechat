package com.varsel.firechat.view.signedIn.fragments

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.databinding.ActionSheetParticipantActionsBinding
import com.varsel.firechat.databinding.FragmentGroupChatDetailBinding
import com.varsel.firechat.model.Chat.GroupRoom
import com.varsel.firechat.utils.UserUtils
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedIn.adapters.ParticipantsListAdapter
import com.varsel.firechat.viewModel.GroupChatDetailViewModel


class GroupChatDetailFragment : Fragment() {
    private var _binding: FragmentGroupChatDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var groupId: String
    private lateinit var parent: SignedinActivity
    private var recyclerViewVisible: Boolean = false
    private lateinit var participantAdapter: ParticipantsListAdapter
    private val fragmentViewModel: GroupChatDetailViewModel by activityViewModels()
    private val groupPageViewModel: GroupChatDetailViewModel by activityViewModels()

    override fun onResume() {
        super.onResume()
        recyclerViewVisible = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGroupChatDetailBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        parent = activity as SignedinActivity

        // adapter init
        parent.firebaseViewModel.selectedGroupRoom.observe(viewLifecycleOwner, Observer {
            binding.groupName.text = it?.groupName
            binding.groupSubject.text = it?.subject ?: getString(R.string.no_subject)

            binding.editGroupNameClickable.setOnClickListener { button ->
                if (it != null) {
                    showEditGroupActionsheet(it)
                }
            }

            if (it != null) {
                groupPageViewModel.determineGetParticipants(it, parent)
            }
            if(it!= null){
                participantAdapter = ParticipantsListAdapter(requireContext(), parent.firebaseAuth, it, {
                    navigateToOtherProfileFragment(it)
                },{
                    showParticipantOptionsActonsheet(it)
                })

                binding.partipantsRecyclerView.adapter = participantAdapter
            }
        })

        groupId = GroupChatDetailFragmentArgs.fromBundle(requireArguments()).groupId

        binding.backButton.setOnClickListener {
            popNavigation()
        }

        parent.firebaseViewModel.selectedGroupParticipants.observe(viewLifecycleOwner, Observer {
            binding.groupMembersCount.text = "(${it.size})"
            val admins = parent.firebaseViewModel.selectedGroupRoom.value!!.admins!!.values.toList()
            val currentUser = parent.firebaseAuth.currentUser!!.uid
            val sorted = UserUtils.sortUsersByNameInGroup(it, admins, currentUser)

            participantAdapter.submitList(sorted)
            participantAdapter.notifyDataSetChanged()

            toggleAddMemberVisibility()
        })

        binding.groupMembersClickable.setOnClickListener {
            setRecyclerViewVisible()
        }

        return view
    }


    private fun toggleAddMemberVisibility(){
        if(checkAdminStatus()){
            binding.addMemberClickable.visibility = View.VISIBLE
            binding.addMemberClickable.setOnClickListener {
                val action = GroupChatDetailFragmentDirections.actionGroupChatDetailFragmentToAddGroupMembersFragment(groupId)
                findNavController().navigate(action)
            }
        } else {
            binding.addMemberClickable.visibility = View.GONE
            binding.addMemberClickable.setOnClickListener(null)
        }
    }

    private fun showEditGroupActionsheet(group: GroupRoom){
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(R.layout.action_sheet_edit_group)
        val groupNameEditText = dialog.findViewById<EditText>(R.id.group_name_edit_text)
        val editGroupBtn = dialog.findViewById<Button>(R.id.edit_group_btn_actionsheet)
        val subjectEditText = dialog.findViewById<EditText>(R.id.group_subject_edit_text)

        groupNameEditText?.setText(group.groupName)
        subjectEditText?.setText(group.subject)

        editGroupBtn?.setOnClickListener {
            editGroup(groupNameEditText?.text.toString(), subjectEditText?.text.toString())
            dialog.dismiss()
        }

        groupNameEditText?.doAfterTextChanged {
            if(groupNameEditText.text.toString().isEmpty()){
                editGroupBtn?.isEnabled = false
            } else {
                editGroupBtn?.isEnabled = true
            }
        }

        dialog.show()
    }

    private fun navigateToOtherProfileFragment(userId: String){
        val action = GroupChatDetailFragmentDirections.actionGroupChatDetailFragmentToOtherProfileFragment(userId)
        findNavController().navigate(action)
    }

    private fun editGroup(groupName: String, subject: String){
        if(groupName.isNotEmpty()){
            parent.firebaseViewModel.editGroup("groupName", groupName, groupId, parent.mDbRef)
        }
        parent.firebaseViewModel.editGroup("subject", subject, groupId, parent.mDbRef)
    }

    private fun setRecyclerViewVisible(){
        recyclerViewVisible = !recyclerViewVisible
        toggleRecyclerViewVisible()
    }

    private fun toggleRecyclerViewVisible(){
        if(recyclerViewVisible){
            binding.participantsRecyclerViewParent.visibility = View.VISIBLE
            rotateIcon()
        } else {
            binding.participantsRecyclerViewParent.visibility = View.GONE
            rotateBack()
        }
    }

    private fun makeAdmin(userId: String){
        parent.firebaseViewModel.makeAdmin(userId, groupId, parent.mDbRef, {
           Toast.makeText(requireContext(), "Successful", Toast.LENGTH_LONG).show()
        }, {
            Toast.makeText(requireContext(), "Failure", Toast.LENGTH_LONG).show()
        })
    }

    private fun removeAdmin(userId: String){
        parent.firebaseViewModel.removeAdmin(userId, groupId, parent.firebaseAuth, parent.mDbRef, {
            Toast.makeText(requireContext(), "Successful", Toast.LENGTH_LONG).show()
        }, {
            Toast.makeText(requireContext(), "Failure", Toast.LENGTH_LONG).show()
        })
    }

    private fun removeFromGroup(userId: String){
        parent.firebaseViewModel.removeFromGroup(parent.firebaseAuth, userId, groupId, parent.mDbRef, {
            Toast.makeText(requireContext(), "Removed", Toast.LENGTH_LONG).show()
        }, {
            Toast.makeText(requireContext(), "Failure", Toast.LENGTH_LONG).show()
        })
    }

    private fun isAdmin(userId: String): Boolean{
        for (i in parent.firebaseViewModel.selectedGroupRoom.value!!.admins!!.values){
            if(i == userId){
                return true
            }
        }

        return false
    }

    private fun scaleOpen(){
//        binding.participantsRecyclerViewParent.animate().translationY(binding.participantsRecyclerViewParent.height.toFloat())
        val animate = TranslateAnimation(
            0f,  // fromXDelta
            0f,  // toXDelta
            0f,  // fromYDelta
            binding.participantsRecyclerViewParent.height.toFloat()
        ) // toYDelta

        animate.duration = 500
        animate.fillAfter = true
        binding.participantsRecyclerViewParent.startAnimation(animate)
    }

    private fun rotateIcon(){
        val animator = ObjectAnimator.ofFloat(binding.groupMembersIconAnimatable, View.ROTATION, 90f)
        animator.duration = 300
        animator.start()
    }

    private fun rotateBack(){
        val animator = ObjectAnimator.ofFloat(binding.groupMembersIconAnimatable, View.ROTATION, 0f)
        animator.duration = 300
        animator.start()
    }

    private fun popNavigation(){
        findNavController().navigateUp()
    }

    private fun checkAdminStatus(): Boolean{
        for(i in parent.firebaseViewModel.selectedGroupRoom.value?.admins?.values!!){
            if(i == parent.firebaseAuth.currentUser?.uid){
                return true
            }
        }

        return false
    }

    private fun showParticipantOptionsActonsheet(userId: String){
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = ActionSheetParticipantActionsBinding.inflate(layoutInflater, this.binding.root, false)
        val view = dialogBinding.root
        dialog.setContentView(view)

        fragmentViewModel.actionSheetUserId.value = userId

        setUserActionButtonVisibilities(dialogBinding, dialog)

        dialog.show()
    }

    private fun setUserActionButtonVisibilities(binding: ActionSheetParticipantActionsBinding, dialog: BottomSheetDialog){
        val currentUserId = parent.firebaseAuth.currentUser!!.uid
        parent.firebaseViewModel.selectedGroupRoom.observe(viewLifecycleOwner, Observer {
            if(it?.admins?.containsValue(currentUserId) == true){
                binding.makeAdminBtn.visibility = View.VISIBLE
                binding.removeAdminBtn.visibility = View.VISIBLE
                binding.removeBtn.visibility = View.VISIBLE

                if(isAdmin(fragmentViewModel.actionSheetUserId.value!!)){
                    binding.makeAdminBtn.visibility = View.GONE
                    binding.removeAdminBtn.visibility = View.VISIBLE
                } else {
                    binding.makeAdminBtn.visibility = View.VISIBLE
                    binding.removeAdminBtn.visibility = View.GONE
                }
            } else {
                binding.makeAdminBtn.visibility = View.GONE
                binding.removeAdminBtn.visibility = View.GONE
                binding.removeBtn.visibility = View.GONE
            }

            binding.sendMessageBtn.setOnClickListener {
                navigateToChats()
                dialog.dismiss()
            }

            if(currentUserId == fragmentViewModel.actionSheetUserId.value){
                binding.leaveBtn.visibility = View.VISIBLE
                binding.makeAdminBtn.visibility = View.GONE
                binding.removeBtn.visibility = View.GONE
//                binding.removeAdminBtn.visibility = View.GONE
                binding.sendMessageBtn.visibility = View.GONE
            } else {
                binding.leaveBtn.visibility = View.GONE
            }

            binding.makeAdminBtn.setOnClickListener {
                makeAdmin(fragmentViewModel.actionSheetUserId.value!!)
                dialog.dismiss()
            }

            binding.removeAdminBtn.setOnClickListener {
                removeAdmin(fragmentViewModel.actionSheetUserId.value!!)
                dialog.dismiss()
            }

            binding.removeBtn.setOnClickListener {
                removeFromGroup(fragmentViewModel.actionSheetUserId.value!!)
                dialog.dismiss()
            }

            binding.leaveBtn.setOnClickListener {
                exitGroup()
                dialog.dismiss()
            }
        })
    }

    private fun navigateToChats(){
        var action: NavDirections
        if(parent.signedinViewModel.determineChatroom(fragmentViewModel.actionSheetUserId.value!!, parent.firebaseViewModel.chatRooms.value)){
            action = GroupChatDetailFragmentDirections.actionGroupChatDetailFragmentToChatPageFragment(parent.signedinViewModel.currentChatRoomId.value, fragmentViewModel.actionSheetUserId.value!!)
        } else {
            action = GroupChatDetailFragmentDirections.actionGroupChatDetailFragmentToChatPageFragment(null, fragmentViewModel.actionSheetUserId.value!!)

        }
        binding.root.findNavController().navigate(action)
    }

    private fun exitGroup(){
        parent.firebaseViewModel.leaveGroup(groupId, parent.mDbRef, parent.firebaseAuth, {
            navigateToHome()
        }, {
            Toast.makeText(context, "Exit denied", Toast.LENGTH_LONG).show()
        })
    }

    private fun navigateToHome(){
        lifecycleScope.launchWhenResumed {
            findNavController().navigateUp()
            findNavController().navigateUp()
        }
    }

    // TODO: Re-implement for other pages
    private fun getUserSingle(){
        parent.signedinViewModel.getCurrentUserSingle(parent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
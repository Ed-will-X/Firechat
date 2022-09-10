package com.varsel.firechat.view.signedIn.fragments

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.databinding.ActionSheetParticipantActionsBinding
import com.varsel.firechat.databinding.FragmentGroupChatDetailBinding
import com.varsel.firechat.model.Chat.GroupRoom
import com.varsel.firechat.model.User.User
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedIn.adapters.ParticipantsListAdapter
import com.varsel.firechat.view.signedIn.fragments.bottomNav.ChatsFragmentDirections
import com.varsel.firechat.viewModel.GroupChatDetailViewModel


class GroupChatDetailFragment : Fragment() {
    private var _binding: FragmentGroupChatDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var groupId: String
    private lateinit var parent: SignedinActivity
    private var recyclerViewVisible: Boolean = false
    private lateinit var participantAdapter: ParticipantsListAdapter
    private val fragmentViewModel: GroupChatDetailViewModel by activityViewModels()

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
        participantAdapter = ParticipantsListAdapter(requireContext(), parent.firebaseAuth, parent.firebaseViewModel, {
            navigateToOtherProfileFragment(it)
        },{
            showParticipantOptionsActonsheet(it)
        })
        binding.partipantsRecyclerView.adapter = participantAdapter

        groupId = GroupChatDetailFragmentArgs.fromBundle(requireArguments()).groupId

        binding.backButton.setOnClickListener {
            popNavigation()
        }

        toggleAddMemberVisibility()

        parent.firebaseViewModel.selectedGroupParticipants.observe(viewLifecycleOwner, Observer {
            binding.groupMembersCount.text = "(${it.size})"
            participantAdapter.submitList(it)
        })

        parent.firebaseViewModel.selectedGroupRoom.observe(viewLifecycleOwner, Observer {
            binding.groupName.text = it?.groupName
            binding.groupSubject.text = it?.subject ?: getString(R.string.no_subject)

            binding.editGroupNameClickable.setOnClickListener { button ->
                if (it != null) {
                    showEditGroupActionsheet(it)
                }
            }
        })

        binding.groupMembersClickable.setOnClickListener {
            setRecyclerViewVisible()
        }

        return view
    }


    private fun toggleAddMemberVisibility(){
        if(checkAdminStatus()){
            binding.addMemberClickable.visibility = View.VISIBLE
        } else {
            binding.addMemberClickable.visibility = View.GONE
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

        setUserActionButtonVisibilities(dialogBinding, dialog)

        fragmentViewModel.actionSheetUserId.value = userId

        dialog.show()
    }

    private fun setUserActionButtonVisibilities(binding: ActionSheetParticipantActionsBinding, dialog: BottomSheetDialog){
        val currentUserId = parent.firebaseAuth.currentUser!!.uid
        parent.firebaseViewModel.selectedGroupRoom.observe(viewLifecycleOwner, Observer {
            if(it?.admins?.containsValue(currentUserId) == true){
                binding.makeAdminBtn.visibility = View.VISIBLE
                binding.removeAdminBtn.visibility = View.VISIBLE
                binding.removeBtn.visibility = View.VISIBLE
            } else {
                binding.makeAdminBtn.visibility = View.GONE
                binding.removeAdminBtn.visibility = View.GONE
                binding.removeBtn.visibility = View.GONE
            }

            binding.sendMessageBtn.setOnClickListener {
                navigateToChats()
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

    // TODO: Re-implement for other pages
    private fun getUserSingle(){
        parent.signedinViewModel.getCurrentUserSingle(parent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
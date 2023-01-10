package com.varsel.firechat.presentation.signedIn.fragments.screens.group_chat_detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.common.Response
import com.varsel.firechat.common._utils.UserUtils
import com.varsel.firechat.databinding.ActionSheetParticipantActionsBinding
import com.varsel.firechat.databinding.ActionSheetProfileImageBinding
import com.varsel.firechat.databinding.FragmentGroupChatDetailBinding
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.use_case._util.animation.Direction
import com.varsel.firechat.domain.use_case._util.animation.Rotate90UseCase
import com.varsel.firechat.domain.use_case.current_user.CheckServerConnectionUseCase
import com.varsel.firechat.domain.use_case.profile_image.DisplayProfileImage
import com.varsel.firechat.utils.*
import com.varsel.firechat.utils.ExtensionFunctions.Companion.observeOnce
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.adapters.ParticipantsListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.lang.IllegalArgumentException
import javax.inject.Inject

@AndroidEntryPoint
class GroupChatDetailFragment : Fragment() {
    private var _binding: FragmentGroupChatDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var groupId: String
    private lateinit var parent: SignedinActivity
    private var recyclerViewVisible: Boolean = false
    private lateinit var participantAdapter: ParticipantsListAdapter
    private lateinit var viewModel: GroupChatDetailViewModel

    @Inject
    lateinit var displayProfileImage: DisplayProfileImage

    @Inject
    lateinit var checkServerConnection: CheckServerConnectionUseCase

    @Inject
    lateinit var rotate90UseCase: Rotate90UseCase

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
        groupId = GroupChatDetailFragmentArgs.fromBundle(requireArguments()).groupId

        viewModel = ViewModelProvider(this).get(GroupChatDetailViewModel::class.java)
        viewModel.getGroupChat(groupId)
        collectState()

        observeGroupImage()

        checkServerConnection().onEach {
            if(it) {
                binding.addMemberClickable.isEnabled = true
            } else {
                binding.addMemberClickable.isEnabled = false
            }
        }.launchIn(lifecycleScope)


        binding.backButton.setOnClickListener {
            popNavigation()
        }

        binding.groupMembersClickable.setOnClickListener {
            setRecyclerViewVisible()
        }

        return view
    }

    private fun collectState() {
        viewModel.state.observe(viewLifecycleOwner, Observer {
            binding.groupName.text = it.selectedGroup?.groupName
            binding.groupSubject.text = it.selectedGroup?.subject ?: parent.getString(R.string.no_subject)

            if(checkAdminStatus()){
                binding.editIcon.visibility = View.VISIBLE
            } else {
                binding.editIcon.visibility = View.GONE
            }

            binding.editGroupNameClickable.setOnClickListener { button ->
                if (it.selectedGroup != null && checkAdminStatus()) {
                    showEditGroupActionsheet(it.selectedGroup)
                }
            }

            if (it.selectedGroup != null) {
//                viewModel.determineGetParticipants(it.selectedGroup, parent)
//                viewModel.getNonParticipants(parent)
            }

            if(it.selectedGroup != null){
                participantAdapter = ParticipantsListAdapter(parent, requireContext(), this, viewModel, it.selectedGroup, { id, user, base64 ->
                    navigateToOtherProfileFragment(id, user, base64)

                },{ id, user, base64 ->
                    showParticipantOptionsActonsheet(id)
                    parent.firebaseViewModel.selectedChatRoomUser.value = user
                    parent.profileImageViewModel.selectedOtherUserProfilePicChat.value = base64
                }, { image, user ->
                    displayProfileImage(image, user, parent)
                })

                binding.partipantsRecyclerView.adapter = participantAdapter
            }

            if(checkAdminStatus()){
                binding.profileImageSilhouette.setOnClickListener {
                    showImageOptionsActionsheet()
                }
            } else {
                binding.profileImage.setOnClickListener {
                    displayImage()
                }
            }
        })

        viewModel.participsnts.observe(viewLifecycleOwner, Observer {
            Log.d("CLEAN", "Participant Count: ${it.size}")
            binding.groupMembersCount.text = "(${it.size})"
            val admins = viewModel.state.value?.selectedGroup?.admins?.values?.toList() ?: listOf()
            val currentUser = viewModel.getCurrentUserIdUseCase()
            val sorted = UserUtils.sortUsersByNameInGroup(it, admins, currentUser)

            participantAdapter.submitList(sorted)
            participantAdapter.notifyDataSetChanged()

            toggleAddMemberVisibility()
        })
    }

    private fun displayImage(){
        val selectedGroupImage = parent.profileImageViewModel.selectedGroupImage.value
        val selectedGroupRoom = parent.firebaseViewModel.selectedGroupRoom.value
        if(selectedGroupImage != null && selectedGroupRoom != null){
            ImageUtils.displayGroupImage(selectedGroupImage, selectedGroupRoom, parent)
        }
    }

    private fun observeGroupImage(){
        parent.profileImageViewModel.selectedGroupImage.observe(viewLifecycleOwner, Observer { profileImage ->
            if(profileImage != null && profileImage.image != null){
                ImageUtils.setProfilePic(profileImage.image!!, binding.profileImage, binding.profileImageParent, parent)
                binding.profileImageParent.visibility = View.VISIBLE
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        ImageUtils.handleOnActivityResult(requireContext(), requestCode, resultCode, data, {
            uploadImage(it, {})
        }, {
            if(it != null){
                uploadImage(it, {})
            }
        })
    }

    // gallery
    private fun uploadImage(uri: Uri, successCallback: ()-> Unit){
        val base64: String? = ImageUtils.encodeUri(uri, parent)
        if(base64 != null){
            val timestamp = System.currentTimeMillis()
            val profileImage = ProfileImage(groupId, timestamp)

            parent.infobarController.showBottomInfobar(parent.getString(R.string.uploading_group_image), InfobarColors.UPLOADING)

            parent.firebaseViewModel.uploadProfileImage(profileImage, base64, parent.firebaseStorage, parent.mDbRef, groupId, {
                parent.firebaseViewModel.appendGroupImageTimestamp(groupId, parent.mDbRef, timestamp, {
                    val profileImage_withBase64 = ProfileImage(profileImage, base64)

                    parent.profileImageViewModel.storeImage(profileImage_withBase64)
//                    parent.profileImageViewModel.selectedGroupImageEncoded.value = profileImage.image
                    parent.profileImageViewModel.selectedGroupImage.value = profileImage_withBase64

                    parent.infobarController.showBottomInfobar(parent.getString(R.string.group_image_upload_successful), InfobarColors.SUCCESS)

                    successCallback()
                }, {
                    parent.infobarController.showBottomInfobar(parent.getString(R.string.group_image_upload_error), InfobarColors.FAILURE)
                })
            }, {
                parent.infobarController.showBottomInfobar(parent.getString(R.string.group_image_upload_error), InfobarColors.FAILURE)
            })
        }
    }

    // camera
    private fun uploadImage(base64: String, successCallback: () -> Unit){
        val timestamp = System.currentTimeMillis()
        val profileImage =
            ProfileImage( groupId, timestamp)

        parent.infobarController.showBottomInfobar(parent.getString(R.string.uploading_group_image), InfobarColors.UPLOADING)

        parent.firebaseViewModel.uploadProfileImage(profileImage, base64, parent.firebaseStorage, parent.mDbRef, groupId, {
            parent.firebaseViewModel.appendGroupImageTimestamp(groupId, parent.mDbRef, timestamp, {
                val profileImage_withBase64 = ProfileImage(profileImage, base64)

                parent.profileImageViewModel.storeImage(profileImage_withBase64)
                parent.profileImageViewModel.selectedGroupImage.value = profileImage_withBase64

                parent.infobarController.showBottomInfobar(parent.getString(R.string.group_image_upload_successful), InfobarColors.SUCCESS)

                successCallback()
            }, {
                parent.infobarController.showBottomInfobar(parent.getString(R.string.group_image_upload_error), InfobarColors.FAILURE)
            })
        }, {
            parent.infobarController.showBottomInfobar(parent.getString(R.string.group_image_upload_error), InfobarColors.FAILURE)
        })
    }

    private fun removeImage(successCallback: () -> Unit){
        val timestamp = System.currentTimeMillis()

        parent.infobarController.showBottomInfobar(parent.getString(R.string.removing_group_image), InfobarColors.UPLOADING)

        parent.firebaseViewModel.removeProfileImage(parent.mDbRef, groupId, parent.firebaseStorage, {
            parent.firebaseViewModel.appendGroupImageTimestamp(groupId, parent.mDbRef, timestamp, {
                val image = parent.profileImageViewModel.getImageById(groupId)

                parent.infobarController.showBottomInfobar(parent.getString(R.string.remove_group_image_successful), InfobarColors.SUCCESS)

                image.observeOnce(viewLifecycleOwner, Observer {
                    if(it != null){
                        parent.profileImageViewModel.nullifyImageInRoom(groupId)

//                        parent.profileImageViewModel.selectedGroupImageEncoded.value = null
                        parent.profileImageViewModel.selectedGroupImage.value = null
                    }
                })
                successCallback()
            }, {
                parent.infobarController.showBottomInfobar(parent.getString(R.string.remove_group_image_error), InfobarColors.FAILURE)
            })
        }, {
            parent.infobarController.showBottomInfobar(parent.getString(R.string.remove_group_image_error), InfobarColors.FAILURE)
        })
    }

    private fun showImageOptionsActionsheet(){
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = ActionSheetProfileImageBinding.inflate(layoutInflater, this.binding.root, false)
        val view = dialogBinding.root

        dialog.setContentView(view)

        checkServerConnection().onEach {
            if(it) {
                dialogBinding.pickImage.isEnabled = true
                dialogBinding.openCamera.isEnabled = true
                dialogBinding.removeImage.isEnabled = true
            } else {
                dialogBinding.pickImage.isEnabled = false
                dialogBinding.openCamera.isEnabled = false
                dialogBinding.removeImage.isEnabled = false
            }
        }.launchIn(lifecycleScope)
//        LifecycleUtils.observeInternetStatus(parent, this, {
//            dialogBinding.pickImage.isEnabled = true
//            dialogBinding.openCamera.isEnabled = true
//            dialogBinding.removeImage.isEnabled = true
//        }, {
//            dialogBinding.pickImage.isEnabled = false
//            dialogBinding.openCamera.isEnabled = false
//            dialogBinding.removeImage.isEnabled = false
//        })

        dialogBinding.expand.setOnClickListener {
            displayImage()
            dialog.dismiss()
        }

        dialogBinding.pickImage.setOnClickListener {
            ImageUtils.openImagePicker(this)
            dialog.dismiss()
        }

        dialogBinding.openCamera.setOnClickListener {
            ImageUtils.openCamera(requireContext(), this, parent)
            dialog.dismiss()
        }

        dialogBinding.removeImage.setOnClickListener {
            dialog.dismiss()
            removeImage {
                binding.profileImage.setImageURI(null)
                binding.profileImageParent.visibility = View.GONE
            }
        }

        viewModel.changeDialogDimAmountUseCase(dialog, 0f)
        dialog.show()
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
            val groupNameSanitised = groupNameEditText?.text.toString().trim()
            val subjectSanitised = subjectEditText?.text.toString().trim()

            editGroup(groupNameSanitised, subjectSanitised)
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

    private fun navigateToOtherProfileFragment(userId: String, user: User, base64: String?){
        try {
            val action = GroupChatDetailFragmentDirections.actionGroupChatDetailFragmentToOtherProfileFragment(userId)
            parent.profileImageViewModel.selectedOtherUserProfilePic.value = base64
            findNavController().navigate(action)
        } catch(e: IllegalArgumentException) { }
    }

    private fun editGroup(groupName: String, subject: String){
        if(groupName.isNotEmpty()){
            viewModel.editGroupUseCase("groupName", groupName, groupId).onEach {
                when(it) {
                    is Response.Success -> {
                        viewModel.getGroupChat(groupId)
                    }
                    is Response.Loading -> {

                    }
                    is Response.Fail -> {

                    }
                }
            }.launchIn(lifecycleScope)
        }

        viewModel.editGroupUseCase("subject", subject, groupId).onEach {
            when(it) {
                is Response.Success -> {
                    viewModel.getGroupChat(groupId)
                }
                is Response.Loading -> {

                }
                is Response.Fail -> {

                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun setRecyclerViewVisible(){
        recyclerViewVisible = !recyclerViewVisible
        toggleRecyclerViewVisible()
    }

    private fun toggleRecyclerViewVisible(){
        if(recyclerViewVisible){
            binding.participantsRecyclerViewParent.visibility = View.VISIBLE
            rotate90UseCase(binding.groupMembersIconAnimatable, Direction.Forward())
        } else {
            binding.participantsRecyclerViewParent.visibility = View.GONE
            rotate90UseCase(binding.groupMembersIconAnimatable, Direction.Reverse())

        }
    }

    private fun makeAdmin(userId: String){
        viewModel.makeAdminUseCase(userId, viewModel.state.value?.selectedGroup!!).onEach {
            when(it) {
                is Response.Success -> {
                    viewModel.getGroupChat(groupId)
                    Toast.makeText(requireContext(), "Successful", Toast.LENGTH_LONG).show()
                }
                is Response.Loading -> {

                }
                is Response.Fail -> {
                    Toast.makeText(requireContext(), "Failure", Toast.LENGTH_LONG).show()
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun removeAdmin(userId: String){
        viewModel.removeAdminUseCase(userId, viewModel.state.value?.selectedGroup!!).onEach {
            when(it) {
                is Response.Success -> {
                    viewModel.getGroupChat(groupId)
                    Toast.makeText(requireContext(), "Successful", Toast.LENGTH_LONG).show()
                }
                is Response.Loading -> {

                }
                is Response.Fail -> {
                    Toast.makeText(requireContext(), "Failure", Toast.LENGTH_LONG).show()
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun removeFromGroup(userId: String){
        viewModel.removefromgroupUsecase(userId, viewModel.state.value?.selectedGroup!!).onEach {
            when(it) {
                is Response.Success -> {
                    viewModel.getGroupChat(groupId)
                    Toast.makeText(requireContext(), "Successful", Toast.LENGTH_LONG).show()
                }
                is Response.Loading -> {

                }
                is Response.Fail -> {
                    Toast.makeText(requireContext(), "Failure", Toast.LENGTH_LONG).show()
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun isAdmin(userId: String): Boolean{
        for (i in viewModel.state.value?.selectedGroup?.admins?.values ?: listOf()){
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



    private fun popNavigation(){
        findNavController().navigateUp()
    }

    /*
    *   Returns true if the current user is an admin
    * */
    private fun checkAdminStatus(): Boolean{
        for(i in viewModel.state.value?.selectedGroup?.admins?.values!!){
            if(i == viewModel.getCurrentUserIdUseCase()){
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

        viewModel.actionSheetUserId.value = userId

        setUserActionButtonVisibilities(dialogBinding, dialog)

        dialog.show()
    }

    private fun setUserActionButtonVisibilities(binding: ActionSheetParticipantActionsBinding, dialog: BottomSheetDialog){
        val currentUserId = viewModel.getCurrentUserIdUseCase()
        viewModel.state.observe(viewLifecycleOwner, Observer {
            if(it.selectedGroup?.admins?.containsValue(currentUserId) == true){
                binding.makeAdminBtn.visibility = View.VISIBLE
                binding.removeAdminBtn.visibility = View.VISIBLE
                binding.removeBtn.visibility = View.VISIBLE

                if(isAdmin(viewModel.actionSheetUserId.value!!)){
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

            if(currentUserId == viewModel.actionSheetUserId.value){
                binding.leaveBtn.visibility = View.VISIBLE
                binding.makeAdminBtn.visibility = View.GONE
                binding.removeBtn.visibility = View.GONE
//                binding.removeAdminBtn.visibility = View.GONE
                binding.sendMessageBtn.visibility = View.GONE
            } else {
                binding.leaveBtn.visibility = View.GONE
            }

            binding.makeAdminBtn.setOnClickListener {
                makeAdmin(viewModel.actionSheetUserId.value!!)
                dialog.dismiss()
            }

            binding.removeAdminBtn.setOnClickListener {
                removeAdmin(viewModel.actionSheetUserId.value!!)
                dialog.dismiss()
            }

            binding.removeBtn.setOnClickListener {
                removeFromGroup(viewModel.actionSheetUserId.value!!)
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
        if(parent.signedinViewModel.determineChatroom(viewModel.actionSheetUserId.value!!, parent.firebaseViewModel.chatRooms.value)){
            action = GroupChatDetailFragmentDirections.actionGroupChatDetailFragmentToChatPageFragment(parent.signedinViewModel.currentChatRoomId.value, viewModel.actionSheetUserId.value!!)
        } else {
            action = GroupChatDetailFragmentDirections.actionGroupChatDetailFragmentToChatPageFragment(null, viewModel.actionSheetUserId.value!!)

        }
        binding.root.findNavController().navigate(action)
    }

    private fun exitGroup(){
        viewModel.exitgroupUsecase(viewModel.state.value?.selectedGroup!!).onEach {
            when(it) {
                is Response.Success -> {
                    navigateToHome()
                    Toast.makeText(requireContext(), "Successful", Toast.LENGTH_LONG).show()
                }
                is Response.Loading -> {

                }
                is Response.Fail -> {
                    Toast.makeText(requireContext(), "Failure", Toast.LENGTH_LONG).show()
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun navigateToHome(){
        lifecycleScope.launchWhenResumed {
            findNavController().navigateUp()
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
//package com.varsel.firechat.domain.use_case.message
//
//import android.util.Log
//import android.view.View
//import androidx.lifecycle.lifecycleScope
//import androidx.navigation.fragment.findNavController
//import com.bumptech.glide.Glide
//import com.google.android.material.bottomsheet.BottomSheetDialog
//import com.varsel.firechat.R
//import com.varsel.firechat.common.Resource
//import com.varsel.firechat.common.Response
//import com.varsel.firechat.common._utils.ImageUtils
//import com.varsel.firechat.data.local.Message.Message
//import com.varsel.firechat.data.local.Message.MessageType
//import com.varsel.firechat.data.local.User.User
//import com.varsel.firechat.databinding.ActionSheetMessageOptionsBinding
//import com.varsel.firechat.domain.use_case._util.message.FormatStampMessageDetail_UseCase
//import com.varsel.firechat.domain.use_case._util.string.Truncate_UseCase
//import com.varsel.firechat.domain.use_case._util.user.ExcludeCurrentUserIdFromMap_UseCase
//import com.varsel.firechat.domain.use_case.chat_image.CheckChatImageInDb
//import com.varsel.firechat.presentation.signedIn.SignedinActivity
//import com.varsel.firechat.presentation.signedIn.adapters.ReadByAdapter
//import com.varsel.firechat.presentation.signedIn.fragments.screens.chat_page.ChatPageFragment
//import com.varsel.firechat.presentation.signedIn.fragments.screens.chat_page.ChatPageFragmentDirections
//import kotlinx.coroutines.flow.launchIn
//import kotlinx.coroutines.flow.onEach
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//class SetupMessageActions_UseCase @Inject constructor(
//    val excludeUserIdFromMap: ExcludeCurrentUserIdFromMap_UseCase,
//    val deleteMessage: DeleteMessage_Chat_UseCase,
//    val deleteForAll: DeleteMessageForAll_ChatRoom_UseCase,
//    val formatStampTime: FormatStampMessageDetail_UseCase,
//    val truncate: Truncate_UseCase,
//    val checkChatImageInDb: CheckChatImageInDb
//) {
//    operator fun invoke(message: Message, user: User?, fragment: ChatPageFragment) {
//        val dialog = BottomSheetDialog(fragment.requireContext())
//        val dialogBinding = ActionSheetMessageOptionsBinding.inflate(fragment.layoutInflater, fragment.binding.root, false)
//        dialog.setContentView(dialogBinding.root)
//
//        if(user != null) {
//            setDialogBindings(dialog, dialogBinding, message, user)
//        } else {
//            fragment.viewModel.getOtherUserSingle(message.sender).onEach {
//                when(it) {
//                    is Resource.Success -> {
//                        if(it.data != null) {
//                            setDialogBindings(dialog, dialogBinding, message, it.data)
//                        }
//                    }
//                }
//            }.launchIn(fragment.lifecycleScope)
//        }
//
//
//        dialogBinding.cancelBtn.setOnClickListener {
//            dialog.dismiss()
//        }
//
//        dialog.show()
//    }
//
//    private fun setDialogBindings(
//        dialog: BottomSheetDialog,
//        dialogBinding: ActionSheetMessageOptionsBinding,
//        message: Message,
//        user: User,
//        fragment: ChatPageFragment,
//        activity: SignedinActivity,
//        existingChatRoomId: String?,
//
//    ) {
//        dialogBinding.name.setText(if(user.userUID == fragment.viewModel.getCurrentUserId()) activity.getString(R.string.you) else user.name)
//        dialogBinding.deleteForAllParent.visibility = if(user.userUID == fragment.viewModel.getCurrentUserId()) View.VISIBLE else View.GONE
//        dialogBinding.readByParent.visibility = if(message.sender == fragment.viewModel.getCurrentUserId()) View.VISIBLE else View.GONE
//        dialogBinding.deleteForAllParent.visibility = if(message.deletedBySender == true) View.GONE else View.VISIBLE
//        dialogBinding.replyParent.visibility = View.GONE
//        dialogBinding.forwardParent.visibility = View.GONE
//
//        dialogBinding.deleteParent.setOnClickListener {
//            if(existingChatRoomId != null) {
//                deleteMessage(message, existingChatRoomId).onEach {
//                    when(it) {
//                        is Response.Success -> {
//                            Log.d("LLL", "Deleted")
//                        }
//                        is Response.Loading -> {
//                            Log.d("LLL", "Deleting")
//                        }
//                        is Response.Fail -> {
//                            Log.d("LLL", "Failed to delete")
//                        }
//                    }
//                }.launchIn(fragment.lifecycleScope)
//            }
//            dialog.dismiss()
//        }
//
//        dialogBinding.detailsParent.setOnClickListener {
//            showMessageDetailOverlay(message, user)
//            dialog.dismiss()
//        }
//
//        dialogBinding.readByParent.setOnClickListener {
//            fragment.binding.readByParent.visibility = View.VISIBLE
//            fragment.binding.darkOverlay.visibility = View.VISIBLE
//
//            val filteredMap = excludeUserIdFromMap(message.readBy)
//            val filteredPair = filteredMap.map { Pair(it.key, it.value) }
//
//            setupReadByAdapter(message, filteredPair)
//
//            dialog.dismiss()
//        }
//
//        dialogBinding.deleteForAllParent.setOnClickListener {
//            if(existingChatRoomId != null) {
//                deleteForAll(message, existingChatRoomId!!).onEach {
//                    when(it) {
//                        is Response.Success -> {
//                            Log.d("LLL", "Deleted")
//                        }
//                        is Response.Loading -> {
//                            Log.d("LLL", "Deleting")
//                        }
//                        is Response.Fail -> {
//                            Log.d("LLL", "Failed to delete")
//                        }
//                    }
//                }.launchIn(fragment.lifecycleScope)
//            }
//
//            dialog.dismiss()
//        }
//    }
//
//    private fun setupReadByAdapter(
//        message: Message,
//        userIDs: List<Pair<String, Long>>,
//        fragment: ChatPageFragment,
//        activity: SignedinActivity
//    ) {
//        fragment.binding.readByRecyclerView.visibility = View.GONE
//        fragment.binding.noViews.visibility = View.GONE
//
//        val viewedByCount = userIDs.count()
//        val adapter = ReadByAdapter(message, fragment, activity.signedinViewModel, activity) {
//            val action = ChatPageFragmentDirections.actionChatPageFragmentToOtherProfileFragment(it.userUID)
//            fragment.findNavController().navigate(action)
//        }
//        fragment.binding.readByRecyclerView.adapter = adapter
//
//        fragment.binding.viewedByCount.setText(if(viewedByCount == 1) activity.getString(R.string.viewed_by_1_contact) else activity.getString(R.string.viewed_by_int_contacts, viewedByCount))
//        fragment.binding.readByRecyclerView.visibility = if(viewedByCount > 0 && !message.deletedBySender) View.VISIBLE else View.GONE
//        fragment.binding.noViews.visibility = if(viewedByCount < 1 || message.deletedBySender) View.VISIBLE else View.GONE
//        fragment.binding.viewedByCount.visibility = if(viewedByCount > 0 && !message.deletedBySender) View.VISIBLE else View.GONE
//
//        adapter.items = userIDs
//    }
//
//    private fun showMessageDetailOverlay(message: Message, user: User, fragment: ChatPageFragment, activity: SignedinActivity) {
//        // Reset
//        fragment.binding.overlayDetailMessageParent.visibility = View.GONE
//        fragment.binding.overlayDetailMessageImageParent.visibility = View.GONE
//        fragment.binding.overlayDetailMessageTypeParent.visibility = View.GONE
//
//
//        fragment.binding.overlayDetailName.setText(if(user.userUID == fragment.viewModel.getCurrentUserId()) activity.getString(R.string.you) else user.name)
//        fragment.binding.overlayDetailTime.setText(formatStampTime(message.time))
//        fragment.binding.overlayDetailMessageId.setText(message.messageUID)
//
//        if(message.deletedBySender == false) {
//            fragment.binding.overlayDetailMessageType.setText(returnTextFromMessageType(message.type))
//            fragment.binding.overlayDetailMessageTypeParent.visibility = View.VISIBLE
//
//            if(message.type == MessageType.TEXT) {
//                fragment.binding.overlayDetailMessage.setText(truncate(message.message, 150))
//                fragment.binding.overlayDetailMessageParent.visibility = View.VISIBLE
//            } else if(message.type == MessageType.IMAGE) {
//                fragment.lifecycleScope.launch {
//                    fragment.binding.overlayDetailMessageImageParent.visibility = View.VISIBLE
//
//                    checkChatImageInDb(message.message).onEach {
//                        when(it) {
//                            is Resource.Success -> {
//                                if(it.data != null) {
//                                    val bitmap = ImageUtils.base64ToBitmap(it.data.image!!)
//                                    Glide.with(activity).load(bitmap).dontAnimate().into(fragment.binding.overlayDetailImage)
//                                }
//                            }
//                            is Resource.Loading -> {
//                                // TODO: Show loading spinner
//                            }
//                            is Resource.Error -> {
//                                // TODO: Show error image
//                            }
//                        }
//                    }.launchIn(fragment.lifecycleScope)
//                }
//            }
//        }
//
//        fragment.binding.darkOverlay.visibility = View.VISIBLE
//        fragment.binding.messageDetailParent.visibility = View.VISIBLE
//    }
//
//    fun returnTextFromMessageType(messageType: Int): String {
//        return when(messageType) {
//            MessageType.TEXT -> "Text"
//            MessageType.IMAGE -> "Image"
//            else -> "Undefined"
//        }
//    }
//}
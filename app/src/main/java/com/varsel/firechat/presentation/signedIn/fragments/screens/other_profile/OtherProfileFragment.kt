package com.varsel.firechat.presentation.signedIn.fragments.screens.other_profile

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.databinding.ActionSheetUnfriendBinding
import com.varsel.firechat.databinding.ActionsheetOtherUserPublicPostsBinding
import com.varsel.firechat.databinding.FragmentOtherProfileBinding
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.domain.use_case.current_user.CheckServerConnectionUseCase
import com.varsel.firechat.domain.use_case.profile_image.DisplayProfileImage
import com.varsel.firechat.domain.use_case.public_post.DisplayPublicPostImage_UseCase
import com.varsel.firechat.domain.use_case.public_post.DoesPostExistUseCase
import com.varsel.firechat.domain.use_case.public_post.GetPublicPostUseCase
import com.varsel.firechat.domain.use_case.public_post.SortPublicPostReversedUseCase
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.adapters.PublicPostAdapter
import com.varsel.firechat.presentation.signedIn.adapters.PublicPostAdapterShapes
import com.varsel.firechat.common._utils.ExtensionFunctions.Companion.collectLatestLifecycleFlow
import com.varsel.firechat.domain.use_case._util.string.Truncate_UseCase
import com.varsel.firechat.domain.use_case._util.user.GetFirstName_UseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OtherProfileFragment : Fragment() {
    private var _binding: FragmentOtherProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private lateinit var viewModel: OtherProfileViewModel
    private lateinit var postAdapter: PublicPostAdapter

    @Inject
    lateinit var displayProfileImage: DisplayProfileImage

    @Inject
    lateinit var doesPostExist: DoesPostExistUseCase

    @Inject
    lateinit var getPublicPostUseCase: GetPublicPostUseCase

    @Inject
    lateinit var sortPublicPosts: SortPublicPostReversedUseCase

    @Inject
    lateinit var checkServerConnection: CheckServerConnectionUseCase

    @Inject
    lateinit var displayPublicPostImage: DisplayPublicPostImage_UseCase

    @Inject
    lateinit var getFirstName: GetFirstName_UseCase

    @Inject
    lateinit var truncate: Truncate_UseCase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentOtherProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        viewModel = ViewModelProvider(this).get(OtherProfileViewModel::class.java)

        parent = activity as SignedinActivity

        checkServerConnection().onEach {
            if(it) {
                binding.addFriendBtn.isEnabled = true
                binding.revokeBtn.isEnabled = true
                binding.unfriendBtn.isEnabled = true
            } else {
                binding.addFriendBtn.isEnabled = false
                binding.revokeBtn.isEnabled = false
                binding.unfriendBtn.isEnabled = false
            }
        }.launchIn(lifecycleScope)

        val uid = OtherProfileFragmentArgs.fromBundle(requireArguments()).userId
        viewModel.getOtherUser(uid)
        collectState()

        binding.backIcon.setOnClickListener {
            findNavController().navigateUp()
        }


        return view
    }

    private fun collectState() {
        collectLatestLifecycleFlow(viewModel.state) {
            if (it.user != null && !it.isLoading) {

                if(!viewModel._hasRun.value){
                    // Runs only once
                    viewModel.getProfileImage(it.user)

                    viewModel._hasRun.value = true
                }
                setBindings(it.user)
                setPublicPostRecyclerView(it.user)
                setClickListeners(it.user)
                determineBtn(it.user)
                setProfileImage(it.profileImage, it.user)

            } else if(it.isLoading) {
                // TODO: Handle Loading
            } else if(!it.isLoading && it.user == null) {
                // TODO: Handle User Null
            }
        }
    }

    private fun setProfileImage(profileImage: ProfileImage?, user: User) {
        if(profileImage?.image != null){
            viewModel.setProfilePicUseCase(profileImage.image!!, binding.profileImage, binding.profileImageParent, parent)
            binding.profileImage.setOnClickListener { it2 ->
                displayProfileImage(profileImage, user, parent)
            }
        } else {
            binding.profileImageParent.visibility = View.GONE
        }
    }

    private fun setClickListeners(selectedUser: User) {
        binding.publicPostsSeeAll.setOnClickListener {
            showPublicPostActionsheet(selectedUser)
        }

        binding.addFriendBtn.setOnClickListener {
            showSendRequestActionSheet(selectedUser)
        }

        binding.revokeBtn.setOnClickListener {
            showRevokeRequestActionsheet(selectedUser)
        }

        binding.unfriendBtn.setOnClickListener {
            showUnfriendActionsheet(selectedUser)
        }
    }

    private fun setPublicPostRecyclerView(otherUser: User){
        lifecycleScope.launch(Dispatchers.Main) {
            delay(300)
            postAdapter = PublicPostAdapter(parent, PublicPostAdapterShapes.RECTANGLE_SMALL, this@OtherProfileFragment, doesPostExist, getPublicPostUseCase) {
//                ImageUtils.displayPublicPostImage(it, otherUser, parent)
                displayPublicPostImage(it, otherUser, parent)
            }

            binding.miniPublicPostsRecyclerView.adapter = postAdapter

            val otherUserPosts = otherUser.public_posts?.keys?.toList()

            if(otherUserPosts != null && otherUserPosts.isNotEmpty()){
                val reversed = sortPublicPosts(otherUserPosts).take(4)

                postAdapter.publicPostStrings = reversed
                binding.shimmerPublicPosts.visibility = View.GONE
                binding.miniPublicPostsRecyclerView.visibility = View.VISIBLE
            } else {
                binding.shimmerPublicPosts.visibility = View.GONE
                binding.noPublicPosts.visibility = View.VISIBLE
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setBindings(user: User){
        binding.aboutTextHeader.text = getFirstName(user.name, this)

        if(user.occupation != null){
            binding.firstName.setText(user.name)
            binding.occupation.setText(user.occupation)
            binding.userProps.visibility = View.VISIBLE
            binding.nameWithoutOccupation.visibility = View.GONE
        } else {
            binding.nameWithoutOccupation.text = user.name
            binding.nameWithoutOccupation.visibility = View.VISIBLE
            binding.userProps.visibility = View.GONE
        }

        if (user.about != null){
            binding.aboutTextBody.setText(truncate(user.about!!, 150))
            binding.moreAboutClickable.setOnClickListener { it2 ->
                showAboutActionSheet(getFirstName(user.name, this), user.about, user)
            }
            binding.aboutTextBody.visibility = View.VISIBLE
        } else {
            binding.aboutTextBody.visibility = View.GONE

            binding.moreAboutClickable.setOnClickListener {
                showAboutActionSheet(getFirstName(user.name, this), null, user)
            }

            // TODO: Show no about ux thingy
            binding.noAbout.visibility = View.VISIBLE
        }
    }


    fun showPublicPostActionsheet(selectedUser: User){
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = ActionsheetOtherUserPublicPostsBinding.inflate(layoutInflater, this.binding.root, false)
        dialog.setContentView(dialogBinding.root)

        lifecycleScope.launch(Dispatchers.Main){
            delay(300)

            val adapter = PublicPostAdapter(parent, PublicPostAdapterShapes.RECTANCLE, this@OtherProfileFragment, doesPostExist, getPublicPostUseCase) {
                dialog.dismiss()
//                ImageUtils.displayPublicPostImage(it, selectedUser, parent)
                displayPublicPostImage(it, selectedUser, parent)
            }
            dialogBinding.otherUserPublicPostsRecyclerViewFull.adapter = adapter

            val otherUserPosts = selectedUser.public_posts?.keys?.toList()
            if(otherUserPosts != null && otherUserPosts.isNotEmpty()){
                val reversed = sortPublicPosts(otherUserPosts)
                adapter.publicPostStrings = reversed
                dialogBinding.otherUserPublicPostsRecyclerViewFull.visibility = View.VISIBLE
                dialogBinding.shimmerPublicPosts.visibility = View.GONE

            } else {
                dialogBinding.shimmerPublicPosts.visibility = View.GONE
                dialogBinding.noPublicPosts.visibility = View.VISIBLE

                dialogBinding.noPublicPostsYetBody.setText(getString(R.string.no_public_post_yet__body, selectedUser?.name))
            }
        }

        dialog.show()
    }

    fun showAboutActionSheet(headerText: String, bodyText: String?, selectedUser: User){
        val dialog = BottomSheetDialog(parent)
        dialog.setContentView(R.layout.action_sheet_about_user)

        val header = dialog.findViewById<TextView>(R.id.dialog_about_header)
        val body = dialog.findViewById<TextView>(R.id.dialog_about_body)
        val no_about = dialog.findViewById<LinearLayout>(R.id.no_about)
        val no_about_body = dialog.findViewById<TextView>(R.id.no_about_body)

        header?.text = headerText

        if (bodyText == null){
            // TODO: Display UX thingy
            no_about?.visibility = View.VISIBLE
            no_about_body?.setText(getString(R.string.no_about_body, selectedUser?.name))
        } else {
            body?.text = bodyText
            no_about?.visibility = View.GONE
        }

        dialog.show()
    }

    private fun showSendRequestActionSheet(user: User){
        val dialog = BottomSheetDialog(parent)
        dialog.setContentView(R.layout.action_sheet_send_friend_request)

        val sendRequestBody = dialog.findViewById<TextView>(R.id.send_request_body)
        val yesBtn = dialog.findViewById<Button>(R.id.send_request_yes)
        val noBtn = dialog.findViewById<Button>(R.id.send_request_no)

        sendRequestBody?.text = getString(R.string.send_request_confirmation_body, user?.name)
        yesBtn?.setOnClickListener {
            viewModel.sendFriendRequest(user)
            dialog.dismiss()
        }

        noBtn?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showRevokeRequestActionsheet(user: User){
        val dialog = BottomSheetDialog(parent)
        dialog.setContentView(R.layout.action_sheet_revoke_request)

        val yes = dialog.findViewById<Button>(R.id.revoke_request_yes)
        val no = dialog.findViewById<Button>(R.id.revoke_request_no)
        val body = dialog.findViewById<TextView>(R.id.revoke_request_body)

        body?.text = getString(R.string.revoke_request_confirmation_body, user?.name)

        no?.setOnClickListener {
            dialog.dismiss()
        }

        yes?.setOnClickListener {
            viewModel.revokeFriendRequest(user)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showUnfriendActionsheet(user: User){
        val dialog = BottomSheetDialog(parent)
        val dialogBinding = ActionSheetUnfriendBinding.inflate(layoutInflater, binding.root, false)
        val view = dialogBinding.root
        dialog.setContentView(view)

        dialogBinding.unfriendUserBody.text = getString(R.string.unfriend_user_body, user.name)

        // TODO: Implement unfriend user
        dialogBinding.yesBtn.setOnClickListener {
            viewModel.unfriendUser(user)
            dialog.dismiss()
        }

        dialogBinding.noBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun determineBtn(user: User){
        binding.revokeBtn.visibility = View.GONE
        binding.unfriendBtn.visibility = View.GONE
        binding.addFriendBtn.visibility = View.GONE

        if(user?.friendRequests?.contains(parent.firebaseAuth.currentUser?.uid) == true){
            binding.revokeBtn.visibility = View.VISIBLE
        } else if(user?.friends?.contains(parent.firebaseAuth.currentUser?.uid) == true){
            binding.unfriendBtn.visibility = View.VISIBLE
        }
        else {
            binding.addFriendBtn.visibility = View.VISIBLE
        }
    }
}
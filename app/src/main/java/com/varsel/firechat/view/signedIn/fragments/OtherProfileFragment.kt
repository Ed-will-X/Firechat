package com.varsel.firechat.view.signedIn.fragments

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.databinding.ActionSheetUnfriendBinding
import com.varsel.firechat.databinding.ActionsheetOtherUserPublicPostsBinding
import com.varsel.firechat.databinding.FragmentOtherProfileBinding
import com.varsel.firechat.model.User.User
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.LifecycleUtils
import com.varsel.firechat.utils.PostUtils
import com.varsel.firechat.utils.UserUtils
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedIn.adapters.PublicPostAdapter
import com.varsel.firechat.view.signedIn.adapters.PublicPostAdapterShapes
import com.varsel.firechat.viewModel.FirebaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OtherProfileFragment : Fragment() {
    private var _binding: FragmentOtherProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private val firebaseViewModel: FirebaseViewModel by activityViewModels()
    private lateinit var userUtils: UserUtils
    private lateinit var postAdapter: PublicPostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentOtherProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        parent = activity as SignedinActivity

        LifecycleUtils.observeInternetStatus(parent.firebaseViewModel, this, {
            binding.addFriendBtn.isEnabled = true
        }, {
            binding.addFriendBtn.isEnabled = false
        })


        val uid = OtherProfileFragmentArgs.fromBundle(requireArguments()).userId

        userUtils = UserUtils(this)

        binding.backIcon.setOnClickListener {
            findNavController().navigateUp()
        }

        // sets props to page
        firebaseViewModel.selectedUser.observe(viewLifecycleOwner, Observer {
            setBindings(it)
            if (it != null) {
                ImageUtils.setProfilePicOtherUser_fullObject(it, binding.profileImage, binding.profileImageParent, parent) { profileImage ->
                    parent.profileImageViewModel.selectedOtherUserProfilePic.value = profileImage?.image

                    if(profileImage != null){
                        binding.profileImage.setOnClickListener { it2 ->
                            ImageUtils.displayProfilePicture(profileImage, it, parent)
                        }
                    }
                }
            }
        })

        setPublicPostRecyclerView()

        return view
    }

    private fun setPublicPostRecyclerView(){
        lifecycleScope.launch(Dispatchers.Main) {
            delay(300)
            postAdapter = PublicPostAdapter(parent, PublicPostAdapterShapes.RECTANGLE_SMALL) {
                val otherUser = parent.firebaseViewModel.selectedUser.value
                if(otherUser != null){
                    ImageUtils.displayPublicPostImage(it, otherUser, parent)
                }
            }

            binding.publicPostsSeeAll.setOnClickListener {
                showPublicPostActionsheet()
            }
            binding.miniPublicPostsRecyclerView.adapter = postAdapter

            val otherUserPosts = parent.firebaseViewModel.selectedUser.value?.public_posts?.values?.toList()

            if(otherUserPosts != null && otherUserPosts.isNotEmpty()){
                val reversed = PostUtils.sortPublicPosts_reversed(otherUserPosts).take(4)

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
        firebaseViewModel.selectedUser.value = null
        parent.profileImageViewModel.selectedOtherUserProfilePic.value = null
        _binding = null
    }

    fun setBindings(user: User?){
        if(user?.name != null){
            binding.aboutTextHeader.text = userUtils.getFirstName(user.name)
        }

        if(user?.occupation != null){
            binding.firstName.setText(user?.name)
            binding.occupation.setText(user?.occupation)
            binding.userProps.visibility = View.VISIBLE
            binding.nameWithoutOccupation.visibility = View.GONE
        } else {
            binding.nameWithoutOccupation.text = user?.name
            binding.nameWithoutOccupation.visibility = View.VISIBLE
            binding.userProps.visibility = View.GONE
        }

        if (user?.about != null){
            binding.aboutTextBody.setText(UserUtils.truncate(user.about!!, 150))
            binding.moreAboutClickable.setOnClickListener { it2 ->
                showAboutActionSheet(userUtils.getFirstName(user.name), user.about)
            }
            binding.aboutTextBody.visibility = View.VISIBLE
        } else {
            binding.aboutTextBody.visibility = View.GONE

            binding.moreAboutClickable.setOnClickListener {
                showAboutActionSheet(userUtils.getFirstName(user?.name ?: ""), null)
            }

            // TODO: Show no about ux thingy
            binding.noAbout.visibility = View.VISIBLE
        }

        if(user != null){
            binding.addFriendBtn.setOnClickListener {
                determineActionsheet(user)
            }
        }
    }

    // TODO: Implement show and hide spinner by switching visibilities
    private fun showSpinner(){

    }

    private fun hideSpinner(){

    }

    fun showPublicPostActionsheet(){
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = ActionsheetOtherUserPublicPostsBinding.inflate(layoutInflater, this.binding.root, false)
        dialog.setContentView(dialogBinding.root)

        lifecycleScope.launch(Dispatchers.Main){
            delay(300)

            val adapter = PublicPostAdapter(parent, PublicPostAdapterShapes.RECTANCLE) {
                val otherUser: User? = parent.firebaseViewModel.selectedUser.value
                if(otherUser != null){
                    dialog.dismiss()
                    ImageUtils.displayPublicPostImage(it, otherUser, parent)
                }
            }
            dialogBinding.otherUserPublicPostsRecyclerViewFull.adapter = adapter

            val otherUserPosts = parent.firebaseViewModel.selectedUser.value?.public_posts?.values?.toList()
            if(otherUserPosts != null && otherUserPosts.isNotEmpty()){
                val reversed = PostUtils.sortPublicPosts_reversed(otherUserPosts)
                adapter.publicPostStrings = reversed
                dialogBinding.otherUserPublicPostsRecyclerViewFull.visibility = View.VISIBLE
                dialogBinding.shimmerPublicPosts.visibility = View.GONE

            } else {
                dialogBinding.shimmerPublicPosts.visibility = View.GONE
                dialogBinding.noPublicPosts.visibility = View.VISIBLE

                val selectedUser = parent.firebaseViewModel.selectedUser.value
                dialogBinding.noPublicPostsYetBody.setText(getString(R.string.no_public_post_yet__body, selectedUser?.name))
            }
        }

        dialog.show()
    }

    fun showAboutActionSheet(headerText: String, bodyText: String?){
        val dialog = BottomSheetDialog(parent)
        dialog.setContentView(R.layout.action_sheet_about_user)

        val header = dialog.findViewById<TextView>(R.id.dialog_about_header)
        val body = dialog.findViewById<TextView>(R.id.dialog_about_body)
        val no_about = dialog.findViewById<LinearLayout>(R.id.no_about)
        val no_about_body = dialog.findViewById<TextView>(R.id.no_about_body)

        val selectedUser = parent.firebaseViewModel.selectedUser.value

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

    private fun showSendRequestActionSheet(user: User?){
        val dialog = BottomSheetDialog(parent)
        dialog.setContentView(R.layout.action_sheet_send_friend_request)

        val sendRequestBody = dialog.findViewById<TextView>(R.id.send_request_body)
        val yesBtn = dialog.findViewById<Button>(R.id.send_request_yes)
        val noBtn = dialog.findViewById<Button>(R.id.send_request_no)

        sendRequestBody?.text = getString(R.string.send_request_confirmation_body, user?.name)
        yesBtn?.setOnClickListener {
            sendFriendRequest(user)
            dialog.dismiss()
        }

        noBtn?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showRevokeRequest(user: User?){
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
            revokeFriendRequest(user)
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
            parent.firebaseViewModel.unfriendUser(user, parent.firebaseAuth, parent.mDbRef)
            dialog.dismiss()
        }

        dialogBinding.noBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun determineActionsheet(user: User){
        if(user?.friendRequests?.contains(parent.firebaseAuth.currentUser?.uid) == true){
            showRevokeRequest(user)

            // TODO: Fix lifecycle bug Here
        } else if(parent.firebaseViewModel.currentUser.value?.friends?.contains(user.userUID) == true){
            showUnfriendActionsheet(user)
        }
        else {
            showSendRequestActionSheet(user)
        }
    }

    private fun determineBtn(user: User?){
        if(user?.friendRequests?.contains(parent.firebaseAuth.currentUser?.uid) == true){
            // TODO: Show revoke request button
        } else if(user?.friends?.contains(parent.firebaseAuth.currentUser?.uid) == true){
            // TODO: Show unfriend user button
        }
        else {
            // TODO: Show send request button
        }
    }

    //  TODO: make status bar transparent on this page
    private fun setTransparent(){

    }

    private fun removeTransparent(){

    }

    private fun revokeFriendRequest(user: User?){
        if (user != null) {
            firebaseViewModel.revokeFriendRequest(parent.firebaseAuth.currentUser?.uid.toString(), user, parent.mDbRef, {
                firebaseViewModel.getUserById(user.userUID!!, parent.mDbRef, {

                }, { })

            }, {

            })
        }
    }

    private fun sendFriendRequest(user: User?){
        if (user != null) {
            firebaseViewModel.sendFriendRequest(parent.firebaseAuth.currentUser?.uid.toString(), user, parent.mDbRef, {
                firebaseViewModel.getUserById(user.userUID!!, parent.mDbRef, {

                }, { })
            }, {

            })
        }
    }
}
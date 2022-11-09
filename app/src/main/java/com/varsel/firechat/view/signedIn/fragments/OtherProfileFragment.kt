package com.varsel.firechat.view.signedIn.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentOtherProfileBinding
import com.varsel.firechat.model.PublicPost.PublicPost
import com.varsel.firechat.model.User.User
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.LifecycleUtils
import com.varsel.firechat.utils.UserUtils
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedIn.adapters.PublicPostAdapter
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

    private fun getPublicPosts_first_8(selectedUser: User, postCallback: (post: PublicPost)-> Unit, afterCallback: ()-> Unit){
        if(selectedUser.public_posts != null && selectedUser.public_posts!!.isNotEmpty()){
            val first_8 = selectedUser.public_posts!!.values.take(8)
            for(i in first_8){
                parent.determinePublicPostFetchMethod_fullObject(i) {
                    if(it != null){
                        postCallback(it)
                    }
                }
            }

            afterCallback()
        }
    }

    private fun setPublicPostRecyclerView(){
        lifecycleScope.launch(Dispatchers.Main) {
            delay(300)
            postAdapter = PublicPostAdapter(parent) {

            }
            binding.miniPublicPostsRecyclerView.adapter = postAdapter

            val first_8 = mutableListOf<PublicPost>()
            getPublicPosts_first_8(parent.firebaseViewModel.selectedUser.value!!, {
                Log.d("LLL", "${it.postTimestamp}")
                first_8.add(it)
            }, {
                Log.d("LLL", "${first_8.count()}")

                postAdapter.submitList(first_8)
                postAdapter.notifyDataSetChanged()
            })


        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        firebaseViewModel.selectedUser.value = null
        parent.profileImageViewModel.selectedOtherUserProfilePic.value = null
        _binding = null
    }

    fun setBindings(user: User?){
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
            binding.aboutTextHeader.text = userUtils.getFirstName(user.name)
            binding.aboutTextBody.setText(UserUtils.truncate(user.about!!, 150))
            binding.moreAboutClickable.setOnClickListener { it2 ->
                showAboutActionSheet(userUtils.getFirstName(user.name), user?.about!!)
            }
            binding.aboutTextBody.visibility = View.VISIBLE
            binding.aboutTextHeader.visibility = View.VISIBLE
            binding.moreAboutClickable.visibility = View.VISIBLE
        } else {
            binding.aboutTextBody.visibility = View.GONE
            binding.aboutTextHeader.visibility = View.GONE
            binding.moreAboutClickable.visibility = View.GONE
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

    fun showAboutActionSheet(headerText: String, bodyText: String){
        val dialog = BottomSheetDialog(parent)
        dialog.setContentView(R.layout.action_sheet_about_user)

        val header = dialog.findViewById<TextView>(R.id.dialog_about_header)
        val body = dialog.findViewById<TextView>(R.id.dialog_about_body)

        header?.text = headerText
        body?.text = bodyText

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
        dialog.setContentView(R.layout.action_sheet_unfriend)
        val unfriendUserBody = dialog.findViewById<TextView>(R.id.unfriend_user_body)
        unfriendUserBody?.text = getString(R.string.unfriend_user_body, user.name)


        dialog.show()
    }

    private fun determineActionsheet(user: User?){
        if(user?.friendRequests?.contains(parent.firebaseAuth.currentUser?.uid) == true){
            showRevokeRequest(user)
        } else if(user?.friends?.contains(parent.firebaseAuth.currentUser?.uid) == true){
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
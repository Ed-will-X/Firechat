package com.varsel.firechat.view.signedIn.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentOtherProfileBinding
import com.varsel.firechat.model.User.User
import com.varsel.firechat.utils.UserUtils
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.viewModel.FirebaseViewModel

class OtherProfileFragment : Fragment() {
    private var _binding: FragmentOtherProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private val firebaseViewModel: FirebaseViewModel by activityViewModels()
    private lateinit var userUtils: UserUtils

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentOtherProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        parent = activity as SignedinActivity


        val uid = OtherProfileFragmentArgs.fromBundle(requireArguments()).userId

        userUtils = UserUtils(this)

        // fetches user from db
        firebaseViewModel.getUserById(uid, parent.mDbRef, {

        }, {

        })

        binding.backIcon.setOnClickListener {
            findNavController().navigateUp()
        }

        // sets props to page
        firebaseViewModel.selectedUser.observe(viewLifecycleOwner, Observer {
            setBindings(it)

        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
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
            binding.aboutTextHeader.text = userUtils.getFirstName(user?.name!!)
            binding.aboutTextBody.setText(UserUtils.truncate(user?.about!!, 150))
            binding.moreAboutClickable.setOnClickListener { it2 ->
                showAboutActionSheet(userUtils.getFirstName(user?.name!!), user?.about!!)
            }
            binding.aboutTextBody.visibility = View.VISIBLE
            binding.aboutTextHeader.visibility = View.VISIBLE
            binding.moreAboutClickable.visibility = View.VISIBLE
        } else {
            binding.aboutTextBody.visibility = View.GONE
            binding.aboutTextHeader.visibility = View.GONE
            binding.moreAboutClickable.visibility = View.GONE
        }

        binding.addFriendBtn.setOnClickListener {
            determineActionsheet(user)
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

    private fun determineActionsheet(user: User?){
        if(user?.friendRequests?.contains(parent.firebaseAuth.currentUser?.uid) == true){
            showRevokeRequest(user)
        } else {
            showSendRequestActionSheet(user)
        }
    }

    //  TODO: make status bar transparent on this page
    private fun setTransparent(){

    }

    private fun removeTransparent(){

    }

    fun revokeFriendRequest(user: User?){
        if (user != null) {
            firebaseViewModel.revokeFriendRequest(parent.firebaseAuth.currentUser?.uid.toString(), user, parent.mDbRef, {
                firebaseViewModel.getUserById(user.userUID!!, parent.mDbRef, {

                }, { })

            }, {

            })
        }
    }

    fun sendFriendRequest(user: User?){
        if (user != null) {
            firebaseViewModel.sendFriendRequest(parent.firebaseAuth.currentUser?.uid.toString(), user, parent.mDbRef, {
                firebaseViewModel.getUserById(user.userUID!!, parent.mDbRef, {

                }, { })
            }, {

            })
        }
    }
}
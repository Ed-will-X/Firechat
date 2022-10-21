package com.varsel.firechat.view.signedIn.fragments.bottomNav

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentProfileBinding
import com.varsel.firechat.model.User.User
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.UserUtils
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedIn.adapters.FriendRequestsAdapter
import com.varsel.firechat.viewModel.FirebaseViewModel

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private val firebaseViewModel: FirebaseViewModel by activityViewModels()
    private lateinit var userUtils: UserUtils
    private lateinit var adapter: FriendRequestsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        parent = activity as SignedinActivity

        userUtils = UserUtils(this)

        observeProfileImage()

        firebaseViewModel.currentUser.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                setUser(it)
            }
        })

        return view
    }


    private fun setUser(user: User){
        if(user.name != null){
            binding.name.text = user?.name
        }
        if(user.about != null){
            binding.aboutTextBody.text = UserUtils.truncate(user?.about!!, 150)
        }

        if(user.posts != null){
            binding.postCount.text = user?.posts?.count().toString()
        } else {
            binding.postCount.text = getString(R.string.zero)
        }

        if(user.friends != null){
            binding.friendCount.text = user?.friends?.count().toString()
        } else {
            binding.friendCount.text = getString(R.string.zero)
        }

        if(user.friendRequests != null){
            binding.friendRequestsCount.text = getString(R.string.friend_request_count, user?.friendRequests?.count().toString())
        } else {
            binding.friendRequestsCount.text = getString(R.string.zero_in_brackets)
        }

        if(user?.occupation != null){
            binding.occupation.text = user?.occupation
            binding.nameWithoutOccupation.visibility = View.GONE
            binding.userProps.visibility = View.VISIBLE
        } else {
            binding.nameWithoutOccupation.visibility = View.VISIBLE
            binding.userProps.visibility = View.GONE
            binding.nameWithoutOccupation.text = user.name
        }

        binding.friendRequestsClickable.setOnClickListener {
            showFriendRequestsActionsheet()
        }

        binding.friendsClickable.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_profileFragment_to_friendListFragment)
        }

        binding.editProfile.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_profileFragment_to_editProfilePage)
        }
    }

    private fun observeProfileImage(){
        parent.profileImageViewModel.profileImageEncodedCurrentUser.observe(viewLifecycleOwner, Observer {
            if(it != null){
                ImageUtils.setProfilePic(it, binding.profileImage, binding.profileImageParent)
            }
        })
    }

    private fun showFriendRequestsActionsheet(){
        val dialog = BottomSheetDialog(parent)
        dialog.setContentView(R.layout.action_sheet_friend_requests)

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.friend_requests_recycler_view)
        adapter = FriendRequestsAdapter(parent, { id, user, base64 ->
            val action = ProfileFragmentDirections.actionProfileFragmentToOtherProfileFragment(id)
            parent.firebaseViewModel.selectedUser.value = user
            parent.profileImageViewModel.selectedOtherUserProfilePic.value = base64

            dialog.dismiss()
            binding.root.findNavController().navigate(action)

        }, { user ->
            firebaseViewModel.acceptFriendRequest(user, parent.mDbRef, parent.firebaseAuth)
            refreshRecyclerView()
        })
        recyclerView?.adapter = adapter

        firebaseViewModel.friendRequests.observe(viewLifecycleOwner, Observer {
            if(it != null){
                adapter.run {
                    adapter.users = arrayListOf<User>()
                    adapter.users = it as ArrayList<User>
                    adapter.notifyDataSetChanged()
                }
            }
        })

        dialog.show()
    }

    private fun refreshRecyclerView(){
        adapter.notifyDataSetChanged()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
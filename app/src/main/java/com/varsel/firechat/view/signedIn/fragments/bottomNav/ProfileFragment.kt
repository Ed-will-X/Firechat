package com.varsel.firechat.view.signedIn.fragments.bottomNav

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.databinding.ActionSheetPublicPostBinding
import com.varsel.firechat.databinding.FragmentProfileBinding
import com.varsel.firechat.model.PublicPost.PublicPost
import com.varsel.firechat.model.PublicPost.PublicPostType
import com.varsel.firechat.model.User.User
import com.varsel.firechat.utils.*
import com.varsel.firechat.utils.gestures.FriendRequestSwipeGesture
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedIn.adapters.FriendRequestsAdapter
import com.varsel.firechat.view.signedIn.adapters.PublicPostAdapter
import com.varsel.firechat.view.signedIn.adapters.PublicPostAdapterShapes
import com.varsel.firechat.viewModel.FirebaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException


class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private val firebaseViewModel: FirebaseViewModel by activityViewModels()
    private lateinit var userUtils: UserUtils
    private lateinit var friendRequestsAdapter: FriendRequestsAdapter
    private lateinit var publicPostAdapter: PublicPostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        parent = activity as SignedinActivity

        userUtils = UserUtils(this)

        LifecycleUtils.observeInternetStatus(parent.firebaseViewModel, this, {
            binding.friendRequestsClickable.isEnabled = true
        }, {
            binding.friendRequestsClickable.isEnabled = false
        })

        lifecycleScope.launch(Dispatchers.Main){
            delay(300)

            publicPostAdapter = PublicPostAdapter(parent, PublicPostAdapterShapes.SQUARE) {
                showPublicPostActionSheet()
            }

            binding.publicPostRecyclerView.adapter = publicPostAdapter

            /*
            *   Adds the first 4 public post string IDs to the recycler view
            *
            * */
            val IDs = parent.firebaseViewModel.currentUser.value?.public_posts?.values?.toList()
            if(IDs != null && IDs.isNotEmpty()){
                val reversed = PostUtils.sortPublicPosts_reversed(IDs).take(4)

                publicPostAdapter.publicPostStrings = reversed
                publicPostAdapter.notifyDataSetChanged()
                binding.postsShimmer.visibility = View.GONE
                binding.publicPostRecyclerView.visibility = View.VISIBLE
            } else {
                binding.postsShimmer.visibility = View.GONE
                binding.noPublicPosts.visibility = View.VISIBLE
            }


            binding.publicPostsClickable.setOnClickListener {
                showPublicPostActionSheet()
            }
        }

        observeProfileImage()

        firebaseViewModel.currentUser.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                setUser(it)
            }
        })

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ImageUtils.handleOnActivityResult(requireContext(), requestCode, resultCode, data, {
                // TODO: Show caption actionsheet before image upload
            uploadPublicPost_image(it, null)
        }, {})

    }

    /*
    *   Embedded both the timestamp and the post type in the ID
    *   Timestamp is located at the start of the ID prefixed with a ":" and suffixed with a "-"
    *   The public post type is located at the end of the ID prefixed with a ":"
    * */
    private fun uploadPublicPost_image(uri: Uri, caption: String?){
        val encoded = ImageUtils.encodeUri(uri, parent)
        if(encoded != null){
            val uid = ":${System.currentTimeMillis()}-${MessageUtils.generateUID(15)}:${PublicPostType.IMAGE}"
            val timestamp = System.currentTimeMillis()
            val currentUserId = parent.firebaseAuth.currentUser!!.uid
            val publicPost = PublicPost(currentUserId, uid, PublicPostType.IMAGE, caption, encoded, timestamp)
            parent.firebaseViewModel.uploadPublicPost(publicPost, parent.mDbRef, {
                parent.firebaseViewModel.appendPublicPostIdToUser(parent.firebaseAuth, parent.mDbRef, uid, {
                    parent.publicPostViewModel.storePost(publicPost)
                }, {})
            }, {})
        }
    }

    private fun showPublicPostActionSheet(){
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = ActionSheetPublicPostBinding.inflate(layoutInflater, this.binding.root, false)
        val view = dialogBinding.root
        dialog.setContentView(view)

        lifecycleScope.launch(Dispatchers.Main){
            delay(300)
            val publicPostAdapter = PublicPostAdapter(parent, PublicPostAdapterShapes.RECTANCLE) {
                dialog.dismiss()
                displayPublicPostImage(it)
            }

            dialogBinding.allPostsRecyclerView.adapter = publicPostAdapter

            val currentUserPosts = parent.firebaseViewModel.currentUser.value?.public_posts?.values?.toList()
            if(currentUserPosts != null && currentUserPosts.isNotEmpty()){
                // TODO: Extract timestamps then reverse
                val reversed = PostUtils.sortPublicPosts_reversed(currentUserPosts)
                publicPostAdapter.publicPostStrings = reversed

                dialogBinding.postsShimmer.visibility = View.GONE
                dialogBinding.allPostsRecyclerView.visibility = View.VISIBLE
            } else {
                dialogBinding.postsShimmer.visibility = View.GONE
                dialogBinding.allPostsRecyclerView.visibility = View.GONE
                dialogBinding.noPublicPosts.visibility = View.VISIBLE

                // TODO: Display no public posts yet

            }
        }

        dialogBinding.openGalleryClickable.setOnClickListener {
            dialog.dismiss()
            ImageUtils.openImagePicker(this)
        }


        dialog.show()
    }

    private fun displayPublicPostImage(post: PublicPost){
        val currentUser = firebaseViewModel.currentUser.value
        if(currentUser != null){
            ImageUtils.displayPublicPostImage(post, currentUser, parent)
        }
    }

    private fun setUser(user: User){
        if(user.name != null){
            binding.name.text = user?.name
        }
        if(user.about != null){
            binding.aboutTextBody.text = UserUtils.truncate(user?.about!!, 150)
        }

        if(user.public_posts != null){
            binding.postCount.text = user?.public_posts?.count().toString()
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
            navigateToFriendList()
        }

        binding.editProfile.setOnClickListener {
            navigateToEditProfile()
        }
    }

    private fun navigateToFriendList() {
        try {
            view?.findNavController()?.navigate(R.id.action_profileFragment_to_friendListFragment)
        } catch (e: IllegalArgumentException){}
    }

    private fun navigateToEditProfile(){
        try {
            view?.findNavController()?.navigate(R.id.action_profileFragment_to_editProfilePage)
        } catch (e: IllegalArgumentException) { }
    }

    private fun observeProfileImage(){
        // TODO: Delete
//        parent.profileImageViewModel.profileImageEncodedCurrentUser.observe(viewLifecycleOwner, Observer {
//            if(it != null){
//                ImageUtils.setProfilePic(it, binding.profileImage, binding.profileImageParent)
//            }
//        })

        parent.profileImageViewModel.profileImage_currentUser.observe(viewLifecycleOwner, Observer {
            if(it?.image != null){
                ImageUtils.setProfilePic(it.image!!, binding.profileImage, binding.profileImageParent, parent)
                binding.profileImageParent.visibility = View.VISIBLE
                binding.profileImage.setOnClickListener { it2 ->
                    val currentUser = parent.firebaseViewModel.currentUser.value
                    if(currentUser != null){
                        ImageUtils.displayProfilePicture(it, currentUser, parent)
                    }
                }
            }
        })
    }

    private fun removeFromAdapter(adapter: FriendRequestsAdapter, viewHolder: RecyclerView.ViewHolder){
        val currentList = adapter.users.toMutableList()
        currentList.removeAt(viewHolder.absoluteAdapterPosition)
        adapter.users = currentList as ArrayList<User>

        adapter.notifyDataSetChanged()
    }

    private fun showFriendRequestsActionsheet(){
        val dialog = BottomSheetDialog(parent)
        dialog.setContentView(R.layout.action_sheet_friend_requests)

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.friend_requests_recycler_view)

        friendRequestsAdapter = FriendRequestsAdapter(parent, { id, user, base64 ->
            dialog.dismiss()

            navigateToOtherProfile(id, user, base64)

        }, { user ->
            firebaseViewModel.acceptFriendRequest(user, parent.mDbRef, parent.firebaseAuth)
            refreshRecyclerView()
        }, { image, user ->
            dialog.dismiss()
            ImageUtils.displayProfilePicture(image, user, parent)
        })
        recyclerView?.adapter = friendRequestsAdapter


        val friendRequestSwipeGesture = object : FriendRequestSwipeGesture(parent){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if(direction == ItemTouchHelper.LEFT){
                    firebaseViewModel.rejectFriendRequest(friendRequestsAdapter.users[viewHolder.adapterPosition], parent.mDbRef, parent.firebaseAuth)
                    removeFromAdapter(friendRequestsAdapter, viewHolder)

                } else if (direction == ItemTouchHelper.RIGHT){
                    firebaseViewModel.acceptFriendRequest(friendRequestsAdapter.users[viewHolder.adapterPosition], parent.mDbRef, parent.firebaseAuth)
                    removeFromAdapter(friendRequestsAdapter, viewHolder)

                }
            }
        }

        val touchHelper = ItemTouchHelper(friendRequestSwipeGesture)
        touchHelper.attachToRecyclerView(recyclerView)

        recyclerView?.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )

        firebaseViewModel.friendRequests.observe(viewLifecycleOwner, Observer {
            if(it != null){
                // TODO: Fix potential bug
                friendRequestsAdapter.run {
                    friendRequestsAdapter.users = it as ArrayList<User>
                    friendRequestsAdapter.notifyDataSetChanged()
                }
            }
        })

        dialog.show()
    }

    private fun navigateToOtherProfile(id: String, user: User, base64: String?) {
        try {
            val action = ProfileFragmentDirections.actionProfileFragmentToOtherProfileFragment(id)
            parent.firebaseViewModel.selectedUser.value = user
            parent.profileImageViewModel.selectedOtherUserProfilePic.value = base64
            binding.root.findNavController().navigate(action)
        } catch (e: IllegalArgumentException) {}
    }

    private fun refreshRecyclerView(){
        friendRequestsAdapter.notifyDataSetChanged()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.varsel.firechat.view.signedIn.fragments.bottomNav

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.databinding.ActionSheetPublicPostBinding
import com.varsel.firechat.databinding.FragmentProfileBinding
import com.varsel.firechat.model.PublicPost.PublicPost
import com.varsel.firechat.model.PublicPost.PublicPostType
import com.varsel.firechat.model.User.User
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.LifecycleUtils
import com.varsel.firechat.utils.MessageUtils
import com.varsel.firechat.utils.UserUtils
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedIn.adapters.FriendRequestsAdapter
import com.varsel.firechat.view.signedIn.adapters.PublicPostAdapter
import com.varsel.firechat.viewModel.FirebaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

            publicPostAdapter = PublicPostAdapter(parent) {
                showPublicPostActionSheet()
            }

            binding.publicPostRecyclerView.adapter = publicPostAdapter
            parent.publicPostViewModel.currentUserPublicPosts.observe(viewLifecycleOwner, Observer {
                val firstFour = it?.take(4)
                if(it != null){
                    binding.postsShimmer.visibility = View.GONE
                    binding.publicPostRecyclerView.visibility = View.VISIBLE

                    publicPostAdapter.submitList(firstFour)
                    publicPostAdapter.notifyDataSetChanged()
                }
            })

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



    // TODO: implement get first 5 public posts here
    private fun getPublicPosts_firstFive(postCallback: (posts: List<PublicPost>)-> Unit){
        val publicPosts = parent.firebaseViewModel.currentUser.value?.public_posts
        val first_5 = publicPosts?.values?.take(5)
        val postsToReturn = mutableListOf<PublicPost>()

        if(first_5 != null && first_5.isNotEmpty() == true){
            for(i in first_5){
                parent.determinePublicPostFetchMethod_fullObject(i) {
                    if(it != null){
                        postsToReturn.add(it)
                    }
                }
            }

            postCallback(postsToReturn)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ImageUtils.handleOnActivityResult(requireContext(), requestCode, resultCode, data, {
                // TODO: Show caption actionsheet before image upload
            uploadPublicPost_image(it, null)
        }, {})

    }

    private fun uploadPublicPost_image(uri: Uri, caption: String?){
        val encoded = ImageUtils.encodeUri(uri, parent)
        if(encoded != null){
            val uid = MessageUtils.generateUID(30)
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
            val publicPostAdapter = PublicPostAdapter(parent) {
                dialog.dismiss()
                displayPublicPostImage(it)
            }

            dialogBinding.allPostsRecyclerView.adapter = publicPostAdapter
            parent.publicPostViewModel.currentUserPublicPosts.observe(viewLifecycleOwner, Observer {
                Log.d("LLL", "${it?.count()}")
                if(it != null){
                    val sorted = MessageUtils.sortPublicPosts(it)
                    publicPostAdapter.submitList(sorted)
                    publicPostAdapter.notifyDataSetChanged()
                    dialogBinding.allPostsRecyclerView.visibility = View.VISIBLE
                    dialogBinding.postsShimmer.visibility = View.GONE
                }
            })
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

    private fun showFriendRequestsActionsheet(){
        val dialog = BottomSheetDialog(parent)
        dialog.setContentView(R.layout.action_sheet_friend_requests)

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.friend_requests_recycler_view)
        friendRequestsAdapter = FriendRequestsAdapter(parent, { id, user, base64 ->
            val action = ProfileFragmentDirections.actionProfileFragmentToOtherProfileFragment(id)
            parent.firebaseViewModel.selectedUser.value = user
            parent.profileImageViewModel.selectedOtherUserProfilePic.value = base64

            dialog.dismiss()
            binding.root.findNavController().navigate(action)

        }, { user ->
            firebaseViewModel.acceptFriendRequest(user, parent.mDbRef, parent.firebaseAuth)
            refreshRecyclerView()
        })
        recyclerView?.adapter = friendRequestsAdapter

        firebaseViewModel.friendRequests.observe(viewLifecycleOwner, Observer {
            if(it != null){
                friendRequestsAdapter.run {
                    friendRequestsAdapter.users = arrayListOf<User>()
                    friendRequestsAdapter.users = it as ArrayList<User>
                    friendRequestsAdapter.notifyDataSetChanged()
                }
            }
        })

        dialog.show()
    }

    private fun refreshRecyclerView(){
        friendRequestsAdapter.notifyDataSetChanged()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
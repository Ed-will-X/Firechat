package com.varsel.firechat.presentation.signedIn.fragments.screen_groups.bottomNav.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.databinding.ActionSheetPublicPostBinding
import com.varsel.firechat.databinding.FragmentProfileBinding
import com.varsel.firechat.data.local.PublicPost.PublicPost
import com.varsel.firechat.data.local.PublicPost.PublicPostType
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.use_case._util.InfobarColors
import com.varsel.firechat.domain.use_case.image.EncodeUri_UseCase
import com.varsel.firechat.domain.use_case.image.HandleOnActivityResult_UseCase
import com.varsel.firechat.domain.use_case.image.OpenImagePicker_UseCase
import com.varsel.firechat.domain.use_case.profile_image.DisplayProfileImage
import com.varsel.firechat.domain.use_case.public_post.DisplayPublicPostImage_UseCase
import com.varsel.firechat.domain.use_case.public_post.DoesPostExistUseCase
import com.varsel.firechat.domain.use_case.public_post.GetPublicPostUseCase
import com.varsel.firechat.domain.use_case.public_post.SortPublicPostReversedUseCase
import com.varsel.firechat.utils.gestures.FriendRequestSwipeGesture
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.adapters.FriendRequestsAdapter
import com.varsel.firechat.presentation.signedIn.adapters.PublicPostAdapter
import com.varsel.firechat.presentation.signedIn.adapters.PublicPostAdapterShapes
import com.varsel.firechat.presentation.viewModel.FirebaseViewModel
import com.varsel.firechat.common._utils.ExtensionFunctions.Companion.collectLatestLifecycleFlow
import com.varsel.firechat.common._utils.MessageUtils
import com.varsel.firechat.domain.use_case._util.string.Truncate_UseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment: Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private val firebaseViewModel: FirebaseViewModel by activityViewModels()
    private lateinit var friendRequestsAdapter: FriendRequestsAdapter
    private lateinit var publicPostAdapter: PublicPostAdapter
    private lateinit var viewModel: ProfileViewModel

    @Inject
    lateinit var displayProfileImage: DisplayProfileImage

    @Inject
    lateinit var doesPostExist: DoesPostExistUseCase

    @Inject
    lateinit var getPublicPostUseCase: GetPublicPostUseCase

    @Inject
    lateinit var sortPublicPosts: SortPublicPostReversedUseCase

    @Inject
    lateinit var openImagePicker: OpenImagePicker_UseCase

    @Inject
    lateinit var handleOnActivityResult: HandleOnActivityResult_UseCase

    @Inject
    lateinit var encodeUri: EncodeUri_UseCase

    @Inject
    lateinit var displayPublicPostImage: DisplayPublicPostImage_UseCase

    @Inject
    lateinit var truncate: Truncate_UseCase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        parent = activity as SignedinActivity
        parent.changeStatusBarColor(R.color.light_blue, false)

        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        viewModel.getProfileImage()
        collectState()

        lifecycleScope.launch(Dispatchers.Main){
            delay(300)

            publicPostAdapter = PublicPostAdapter(parent, PublicPostAdapterShapes.SQUARE, this@ProfileFragment, doesPostExist, getPublicPostUseCase) {
                showPublicPostActionSheet()
            }

            binding.publicPostRecyclerView.adapter = publicPostAdapter

            /*
            *   Adds the first 4 public post string IDs to the recycler view
            *
            * */
            val IDs = viewModel.getCurrentUserRecurrentUseCase().value.data?.public_posts?.keys?.toList()
            if(IDs != null && IDs.isNotEmpty()){
                val reversed = sortPublicPosts(IDs).take(4)

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

        return view
    }

    private fun collectState() {
        collectLatestLifecycleFlow(viewModel.state) {
            if(it.currentUser != null) {
                setUser(it.currentUser)
                setProfileImage(it.profileImage)
                handleInternetConnectivity(it.isConnectedToServer)
            }
        }
    }

    private fun handleInternetConnectivity(isConnected: Boolean) {
        binding.friendRequestsClickable.isEnabled = isConnected
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        handleOnActivityResult(requestCode, resultCode, data, {
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
        val encoded = encodeUri(uri, parent)
        if(encoded != null){
            val uid = ":${System.currentTimeMillis()}-${MessageUtils.generateUID()}:${PublicPostType.IMAGE}"
            val timestamp = System.currentTimeMillis()
            val currentUserId = parent.firebaseAuth.currentUser!!.uid
            val publicPost = PublicPost(currentUserId, uid, PublicPostType.IMAGE, caption, timestamp)

            // Shows bottom infobar
            parent.infobarController.showBottomInfobar(parent.getString(R.string.uploading_public_post), InfobarColors.UPLOADING)

            parent.firebaseViewModel.uploadPublicPost(publicPost, encoded, parent.firebaseStorage, parent.mDbRef, {
                parent.firebaseViewModel.appendPublicPostIdToUser(parent.firebaseAuth, parent.mDbRef, uid, {

                    parent.infobarController.showBottomInfobar(parent.getString(R.string.public_post_upload_successful), InfobarColors.SUCCESS)

                    parent.publicPostViewModel.storePost(it)
                }, {
                    parent.infobarController.showBottomInfobar(parent.getString(R.string.chat_image_upload_error), InfobarColors.FAILURE)
                })
            }, {
                parent.infobarController.showBottomInfobar(parent.getString(R.string.chat_image_upload_error), InfobarColors.FAILURE)
            })
        }
    }

    private fun showPublicPostActionSheet(){
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = ActionSheetPublicPostBinding.inflate(layoutInflater, this.binding.root, false)
        val view = dialogBinding.root
        dialog.setContentView(view)

        lifecycleScope.launch(Dispatchers.Main){
            delay(300)
            val publicPostAdapter = PublicPostAdapter(parent, PublicPostAdapterShapes.RECTANCLE, this@ProfileFragment, doesPostExist, getPublicPostUseCase) {
                dialog.dismiss()
                displayPublicPostImage(it)
            }

            dialogBinding.allPostsRecyclerView.adapter = publicPostAdapter

            val currentUserPosts = viewModel.getCurrentUserRecurrentUseCase().value.data?.public_posts?.keys?.toList()
            if(currentUserPosts != null && currentUserPosts.isNotEmpty()){
                // TODO: Extract timestamps then reverse
                val reversed = sortPublicPosts(currentUserPosts)
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
//            ImageUtils.openImagePicker(this)
            openImagePicker(this)
        }


        dialog.show()
    }

    private fun displayPublicPostImage(post: PublicPost){
        val currentUser = viewModel.getCurrentUserRecurrentUseCase().value.data
        if(currentUser != null){
//            ImageUtils.displayPublicPostImage(post, currentUser, parent)
            displayPublicPostImage(post, currentUser, parent)
        }
    }

    private fun setUser(user: User){
        binding.name.text = user?.name
        if(user.about != null){
            binding.aboutTextBody.text = truncate(user.about!!, 150)
        }

        if(user.public_posts != null){
            binding.postCount.text = user?.public_posts?.count().toString()
        } else {
            binding.postCount.text = getString(R.string.zero)
        }

        if(user.friends.isNotEmpty()){
            binding.friendCount.text = user.friends.count().toString()
        } else {
            binding.friendCount.text = getString(R.string.zero)
        }

        if(user.friendRequests.isNotEmpty()){
            binding.friendRequestsCount.text = getString(R.string.friend_request_count, user.friendRequests.count().toString())
        } else {
            binding.friendRequestsCount.text = getString(R.string.zero_in_brackets)
        }

        if(user.occupation != null){
            binding.occupation.text = user.occupation
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

    private fun setProfileImage(profileImage: ProfileImage?){
        if(profileImage?.image != null){
            viewModel.setProfilePicUseCase(profileImage.image!!, binding.profileImage, binding.profileImageParent, parent)
            binding.profileImage.setOnClickListener { it2 ->
                val currentUser = viewModel.getCurrentUserRecurrentUseCase().value.data
                if(currentUser != null){
                    displayProfileImage(profileImage, currentUser, parent)
                }
            }
        } else {
            binding.profileImageParent.visibility = View.GONE
        }
    }

    private fun removeFromAdapter(adapter: FriendRequestsAdapter, viewHolder: RecyclerView.ViewHolder){
        val currentList = adapter.users.toMutableList()
        currentList.removeAt(viewHolder.absoluteAdapterPosition)
        adapter.users = currentList

        adapter.notifyDataSetChanged()
    }

    private fun showFriendRequestsActionsheet(){
        val dialog = BottomSheetDialog(parent)
        dialog.setContentView(R.layout.action_sheet_friend_requests)

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.friend_requests_recycler_view)

        friendRequestsAdapter = FriendRequestsAdapter(parent, this, viewModel, { id, user, base64 ->
            dialog.dismiss()

            navigateToOtherProfile(id, user, base64)

        }, { user ->
            firebaseViewModel.acceptFriendRequest(user, parent.mDbRef, parent.firebaseAuth)
            refreshRecyclerView()
        }, { image, user ->
            dialog.dismiss()
            displayProfileImage(image, user, parent)
        })
        recyclerView?.adapter = friendRequestsAdapter


        val friendRequestSwipeGesture = object : FriendRequestSwipeGesture(parent){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewModel.checkServerConnection().onEach {
                    if(it) {
                        if(direction == ItemTouchHelper.LEFT){
                            firebaseViewModel.rejectFriendRequest(friendRequestsAdapter.users[viewHolder.adapterPosition], parent.mDbRef, parent.firebaseAuth)
                            removeFromAdapter(friendRequestsAdapter, viewHolder)

                        } else if (direction == ItemTouchHelper.RIGHT){
                            firebaseViewModel.acceptFriendRequest(friendRequestsAdapter.users[viewHolder.adapterPosition], parent.mDbRef, parent.firebaseAuth)
                            removeFromAdapter(friendRequestsAdapter, viewHolder)

                        }
                    }
                }.launchIn(lifecycleScope)
//                LifecycleUtils.observeInternetStatus(parent, this@ProfileFragment, {
//                    if(direction == ItemTouchHelper.LEFT){
//                        firebaseViewModel.rejectFriendRequest(friendRequestsAdapter.users[viewHolder.adapterPosition], parent.mDbRef, parent.firebaseAuth)
//                        removeFromAdapter(friendRequestsAdapter, viewHolder)
//
//                    } else if (direction == ItemTouchHelper.RIGHT){
//                        firebaseViewModel.acceptFriendRequest(friendRequestsAdapter.users[viewHolder.adapterPosition], parent.mDbRef, parent.firebaseAuth)
//                        removeFromAdapter(friendRequestsAdapter, viewHolder)
//
//                    }
//                }, {})
            }
        }

        val touchHelper = ItemTouchHelper(friendRequestSwipeGesture)

        // Disables swipe if no internet
        viewModel.checkServerConnection().onEach {
            if(it) {
                touchHelper.attachToRecyclerView(recyclerView)
            } else {
                touchHelper.attachToRecyclerView(null)
            }
        }.launchIn(lifecycleScope)

//        LifecycleUtils.observeInternetStatus(parent, this, {
//            touchHelper.attachToRecyclerView(recyclerView)
//        }, {
//            touchHelper.attachToRecyclerView(null)
//        })

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
                    friendRequestsAdapter.users = it.toMutableList()
                    friendRequestsAdapter.notifyDataSetChanged()
                }
            } else {
                friendRequestsAdapter.run {
                    friendRequestsAdapter.users = mutableListOf()
                    friendRequestsAdapter.notifyDataSetChanged()
                }
            }
        })

        dialog.show()
    }

    private fun navigateToOtherProfile(id: String, user: User, base64: String?) {
        try {
            val action = ProfileFragmentDirections.actionProfileFragmentToOtherProfileFragment(id)
//            parent.firebaseViewModel.selectedUser.value = user
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
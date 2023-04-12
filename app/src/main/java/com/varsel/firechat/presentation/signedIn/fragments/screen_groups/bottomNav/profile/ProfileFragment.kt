package com.varsel.firechat.presentation.signedIn.fragments.screen_groups.bottomNav.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.databinding.ActionSheetPublicPostBinding
import com.varsel.firechat.databinding.FragmentProfileBinding
import com.varsel.firechat.data.local.PublicPost.PublicPost
import com.varsel.firechat.data.local.PublicPost.PublicPostType
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.use_case.image.EncodeUri_UseCase
import com.varsel.firechat.domain.use_case.image.HandleOnActivityResult_image_UseCase
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
import com.varsel.firechat.common._utils.ExtensionFunctions.Companion.collectLatestLifecycleFlow
import com.varsel.firechat.common._utils.MessageUtils
import com.varsel.firechat.domain.use_case._util.status_bar.ChangeStatusBarColor_UseCase
import com.varsel.firechat.domain.use_case._util.string.Truncate_UseCase
import com.varsel.firechat.domain.use_case.other_user.AcceptFriendRequest_UseCase
import com.varsel.firechat.domain.use_case.other_user.RejectFriendRequest_UseCase
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
    lateinit var handleOnActivityResult: HandleOnActivityResult_image_UseCase

    @Inject
    lateinit var encodeUri: EncodeUri_UseCase

    @Inject
    lateinit var displayPublicPostImage: DisplayPublicPostImage_UseCase

    @Inject
    lateinit var truncate: Truncate_UseCase

    @Inject
    lateinit var acceptFriendRequest: AcceptFriendRequest_UseCase

    @Inject
    lateinit var rejectFriendRequest: RejectFriendRequest_UseCase

    @Inject
    lateinit var changeStatusBarColor: ChangeStatusBarColor_UseCase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        parent = activity as SignedinActivity
        changeStatusBarColor(R.color.light_blue, false, parent)

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

    var prevRequestCount = -1
    private fun collectState() {
        collectLatestLifecycleFlow(viewModel.state) {
            setProfileImage(it.profileImage)
            handleInternetConnectivity(it.isConnectedToServer)
        }

        collectLatestLifecycleFlow(viewModel.currentUser) {
            if(it != null) {
                setUser(it)
                if(prevRequestCount != it.friendRequests.count()) {
                    prevRequestCount = it.friendRequests.count()
                    viewModel.getFriendRequests(it.friendRequests)
                }
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

            viewModel.uploadPublicPost(publicPost, encoded, parent)

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
            acceptFriendRequest(user).onEach {
                when(it) {
                    is Response.Success -> { }
                    is Response.Fail -> { }
                    is Response.Loading -> { }
                }
            }.launchIn(lifecycleScope)
            refreshRecyclerView()
        }, { image, user ->
            dialog.dismiss()
            displayProfileImage(image, user, parent)
        })
        recyclerView?.adapter = friendRequestsAdapter

        collectLatestLifecycleFlow(viewModel.friendRequests) {
            setFriendRequests(it)
        }

        val friendRequestSwipeGesture = object : FriendRequestSwipeGesture(parent){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewModel.checkServerConnection().onEach {
                    if(it) {
                        if(direction == ItemTouchHelper.LEFT){
                            rejectFriendRequest(friendRequestsAdapter.users[viewHolder.adapterPosition]).onEach {
                                when(it) {
                                    is Response.Success -> {

                                    }
                                    is Response.Fail -> { }
                                    is Response.Loading -> {}
                                }
                            }.launchIn(lifecycleScope)
                            removeFromAdapter(friendRequestsAdapter, viewHolder)

                        } else if (direction == ItemTouchHelper.RIGHT){
                            acceptFriendRequest(friendRequestsAdapter.users[viewHolder.adapterPosition]).onEach {
                                when(it) {
                                    is Response.Success -> { }
                                    is Response.Fail -> { }
                                    is Response.Loading -> { }
                                }
                            }.launchIn(lifecycleScope)
                            removeFromAdapter(friendRequestsAdapter, viewHolder)

                        }
                    }
                }.launchIn(lifecycleScope)
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


        recyclerView?.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )

        dialog.show()
    }

    private fun setFriendRequests(it: List<User>) {
        friendRequestsAdapter.run {
            friendRequestsAdapter.users = it.toMutableList()
            friendRequestsAdapter.notifyDataSetChanged()
        }
    }

    private fun navigateToOtherProfile(id: String, user: User, base64: String?) {
        try {
            val action = ProfileFragmentDirections.actionProfileFragmentToOtherProfileFragment(id)
//            parent.firebaseViewModel.selectedUser.value = user
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
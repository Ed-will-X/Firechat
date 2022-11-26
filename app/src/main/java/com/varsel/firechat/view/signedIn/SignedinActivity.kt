package com.varsel.firechat.view.signedIn

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.createDataStore
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.varsel.firechat.FirechatApplication
import com.varsel.firechat.R
import com.varsel.firechat.databinding.ActivitySignedinBinding
import com.varsel.firechat.model.Chat.ChatRoom
import com.varsel.firechat.model.Chat.GroupRoom
import com.varsel.firechat.model.Image.Image
import com.varsel.firechat.model.Image.ImageViewModel
import com.varsel.firechat.model.Image.ImageViewModelFactory
import com.varsel.firechat.model.Message.Message
import com.varsel.firechat.model.ProfileImage.ProfileImage
import com.varsel.firechat.model.ProfileImage.ProfileImageViewModel
import com.varsel.firechat.model.ProfileImage.ProfileImageViewModelFactory
import com.varsel.firechat.model.PublicPost.PublicPost
import com.varsel.firechat.model.PublicPost.PublicPostViewModel
import com.varsel.firechat.model.PublicPost.PublicPostViewModelFactory
import com.varsel.firechat.model.ReadReceipt.ReadReceiptViewModel
import com.varsel.firechat.model.ReadReceipt.ReadReceiptViewModelFactory
import com.varsel.firechat.model.User.User
import com.varsel.firechat.utils.*
import com.varsel.firechat.utils.ExtensionFunctions.Companion.observeOnce
import com.varsel.firechat.view.signedOut.SignedoutActivity
import com.varsel.firechat.view.signedOut.fragments.AuthType
import com.varsel.firechat.viewModel.FirebaseViewModel
import com.varsel.firechat.viewModel.SignedinViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.fixedRateTimer

class SignedinActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignedinBinding
    lateinit var dataStore: DataStore<Preferences>
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var mDbRef: DatabaseReference
    lateinit var firebaseStorage: FirebaseStorage
    lateinit var firebaseViewModel: FirebaseViewModel
    lateinit var signedinViewModel: SignedinViewModel
    lateinit var imageViewModel: ImageViewModel
//    var timer: CountDownTimer? = null
//    lateinit var settingViewModel: SettingViewModel
    lateinit var profileImageViewModel: ProfileImageViewModel
    lateinit var publicPostViewModel: PublicPostViewModel
    lateinit var readReceiptViewModel: ReadReceiptViewModel
    lateinit var settingsDataStore: DataStore<Preferences>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseViewModel = ViewModelProvider(this).get(FirebaseViewModel::class.java)
        signedinViewModel = ViewModelProvider(this).get(SignedinViewModel::class.java)

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference
        firebaseStorage = FirebaseStorage.getInstance()

        initialiseProfileImageViewModel()
        initialiseImageViewModel()
        initialisePublicPostViewModel()
        initialiseReadReceiptsViewModel()
        initialiseSettingsDatastore()

        determineAuthType(intent)

        // get current user recurrent
        getCurrentUserRecurrent()

        // get current user single
        signedinViewModel.getCurrentUserSingle(this)

        binding = ActivitySignedinBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.signed_in_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        checkConnectivity()
//        setOverlayClickListeners()

        binding.logoutTextBtn.setOnClickListener {
            logout(this, this) {

            }
        }

        firebaseViewModel.chatRooms.observe(this, Observer {
            val sorted = MessageUtils.sortChats(it as MutableList<ChatRoom>)

            // TODO: Collect receipts that came after getUserSingle was re-run
            getNewMessage(it, navController)
        })

        firebaseViewModel.groupRooms.observe(this, Observer {
            getNewGroupMessage(it, navController)
        })

        firebaseViewModel.currentUser.observe(this, Observer {
            if (it != null) {
                compareUsers(it)
                determineShowRequesBottomInfobar(it)
                determineNewFriendBottomInfobar(it)
                determineShowGroupAddBottomInfobar(it)

                if(it.public_posts != null && it.public_posts!!.isNotEmpty()){
//                    getPublicPosts_first_5(it.public_posts?.values?.toList()!!)
                }
            }

            if(it?.friendRequests != null && it.friendRequests.isNotEmpty()){
                getFriendRequests(it.friendRequests)
            } else {
                firebaseViewModel.friendRequests.value = mutableListOf<User>()
            }

            if(it?.friends != null && it.friends.isNotEmpty()){
                getAllFriends(it.friends)
            } else {
                firebaseViewModel.setFriends(listOf())
            }
        })

        firebaseViewModel.currentUser.observeOnce(this, Observer {
            if(it != null){
                determineCurrentUserImgFetchMethod(it)
            }
        })

        binding.bottomNavView.setupWithNavController(navController)
        setBottomNavVisibility(navController)

        // Makes the chat fragment the default destination
        binding.bottomNavView.menu.findItem(R.id.chatsFragment).setChecked(true)

        setOverlayBindings()

//        profileImageViewModel.setClearBlacklistCountdown()
    }

    // TODO: Run in a coroutine
    private fun getNewMessage(chatRooms: MutableList<ChatRoom>, navController: NavController){
        val lastRunTimestamp = getUserSingleTimestamp
        val currentUserId = firebaseAuth.currentUser!!.uid

        for(i in chatRooms){
            val lastMessage = MessageUtils.getLastMessageObject(i)
            val selectedChatRoomUser = firebaseViewModel.selectedChatRoomUser.value

            /*
            *   Checks if the last message timestamp is higher than the last time get user single was fetched and,
            *   the last message sender is the current user.
            *   If both are true, it show the BottomInfobar.
            * */
            if((lastMessage?.time ?: 0L) > lastRunTimestamp && lastMessage?.sender != currentUserId){
                getOtherUser(i) {
                    /*
                    *   Shows the info bar if the selected chatRoom user is not the one who just sent the message
                    * */
                    if(it.userUID != selectedChatRoomUser?.userUID || navController.currentDestination?.id != R.id.chatPageFragment){
                        showBottomInfobar(this.getString(R.string.new_message_from, UserUtils.truncate(it.name, 15)), InfobarColors.NEW_MESSAGE)
                    }
                }
            }
        }
    }

    // TODO: Run in a coroutine
    private fun getNewGroupMessage(groupRooms: MutableList<GroupRoom>, navController: NavController){
        val lastRunTimestamp = getUserSingleTimestamp
        val currentUserId = firebaseAuth.currentUser!!.uid
        val selectedGroupRoom = firebaseViewModel.selectedGroupRoom

        for (i in groupRooms){
            val lastMessage = MessageUtils.getLastMessageObject(i)

            if((lastMessage?.time ?: 0L) > lastRunTimestamp && lastMessage?.sender != currentUserId){
                if(i.roomUID != selectedGroupRoom.value?.roomUID || navController.currentDestination?.id != R.id.groupChatPageFragment){
                    showBottomInfobar(this.getString(R.string.new_message_in, UserUtils.truncate(i.groupName, 15)), InfobarColors.NEW_MESSAGE)
                }
            }
        }
    }

    private fun getOtherUser(chatRoom: ChatRoom, userCallback: (user: User)-> Unit){
        val otherUserId = UserUtils.getOtherUserId(chatRoom.participants, this)
        UserUtils.getUser(otherUserId, this) {
            userCallback(it)
        }
    }

    var prevFriendRequests = -1
    fun determineShowRequesBottomInfobar(user: User){
        val sorted = UserUtils.sortByTimestamp(user.friendRequests.toSortedMap())

        if (prevFriendRequests < user.friendRequests.count() && prevFriendRequests != -1){
            UserUtils.getUser(sorted.keys.last(), this) {
                showBottomInfobar(this.getString(R.string.friend_request_From, UserUtils.truncate(it.name, 15)), InfobarColors.NEW_FRIEND_REQUEST)

            }
        }
        prevFriendRequests = user.friendRequests.count()
    }

    var prevFriends = -1
    fun determineNewFriendBottomInfobar(user: User){
        val sorted = UserUtils.sortByTimestamp(user.friends.toSortedMap())

        if(prevFriends < user.friends.count() && prevFriends != -1){
            UserUtils.getUser(sorted.keys.last(), this) {
                showBottomInfobar(this.getString(R.string.is_now_your_friend, UserUtils.truncate(it.name, 15)), InfobarColors.NEW_FRIEND)
            }
        }
        prevFriends = user.friends.count()
    }

    var prevGroups = -1
    fun determineShowGroupAddBottomInfobar(user: User){
        val sorted = UserUtils.sortByTimestamp(user.groupRooms.toSortedMap())

        if(prevGroups < user.groupRooms.count() && prevGroups != -1){
            firebaseViewModel.getGroupChatRoomSingle(sorted.keys.last(), mDbRef, {
                if(it?.admins?.contains(firebaseAuth.currentUser!!.uid) == false){
                    showBottomInfobar(this.getString(R.string.you_have_been_added_to, UserUtils.truncate(it.groupName, 10)), InfobarColors.GROUP_ADD)
                }
            }, {})
        }
        prevGroups = user.groupRooms.count()
    }

    // TODO: Replace strings with resources
    fun showBottomInfobar(customString: String?, customColor: Int?){
        lifecycleScope.launch(Dispatchers.Main) {
            setInfobarProps(customString, customColor)
            binding.bottomInfobar.visibility = View.VISIBLE

            delay(3000)

            binding.bottomInfobar.visibility = View.GONE
        }
    }

    private fun setInfobarProps(customString: String? = null, customColor: Int? = null){
        binding.bottomInfobarText.text = customString
        binding.bottomInfobar.setBackgroundColor(this.resources.getColor(customColor ?: R.color.black))
    }

    fun setOverlayBindings(){
        imageViewModel.showProfileImage.observe(this, Observer {
            if(it){
                binding.imgOverlayParent.visibility = View.VISIBLE
                binding.imgOverlayName.setText(imageViewModel.username.value.toString())
                binding.imgOverlayType.setText(imageViewModel.type.value.toString())
                binding.imgOverlayTimestamp.setText(MessageUtils.formatStampMessage(imageViewModel.timestamp.value.toString()))
                if(imageViewModel.image != null){
                    binding.imgOverlayImage.setImageBitmap(ImageUtils.base64ToBitmap(imageViewModel.image.value!!))
                }

                // TODO: Hide status bar
                hideStatusBar()
            } else {
                binding.imgOverlayParent.visibility = View.GONE
                imageViewModel.clearOverlayProps()
                // TODO: Unhide status bar
                showStatusBar()
            }
        })

        binding.imgOverlayBack.setOnClickListener {
            handleImgBackPress()
        }

        binding.imgOverlayParent.setOnClickListener {
            handleImgBackPress()
        }
    }

    private fun getPublicPosts(publicPosts: List<String>?){
        publicPostViewModel.currentUserPublicPosts.value = mutableListOf()

        if(publicPosts != null && publicPosts.isNotEmpty()){
            for(i in publicPosts){
                determinePublicPostFetchMethod_fullObject(i) {
                    if(it != null){
                        publicPostViewModel.currentUserPublicPosts.value?.add(it)
                    }
                }
            }

        }
    }

    private fun getPublicPosts_first_5(publicPosts: List<String>?){
        publicPostViewModel.currentUserPublicPosts.value = mutableListOf()
        val first_5 = publicPosts?.take(5)

        if(first_5 != null && first_5.isNotEmpty()){
            val reversed = PostUtils.sortPublicPosts_reversed(first_5)

            for(i in reversed){
                determinePublicPostFetchMethod_fullObject(i) {
                    if(it != null){
                        publicPostViewModel.currentUserPublicPosts.value?.add(it)
                    }
                }
            }

        }
    }

    private fun hideStatusBar(){
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    private fun showStatusBar(){
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    override fun onBackPressed() {
        if(imageViewModel.showProfileImage.value != true){
            super.onBackPressed()
        } else {
            handleImgBackPress()
        }
    }

    fun handleImgBackPress(){
        imageViewModel.showProfileImage.value = false
    }

    fun determineAuthType(intent: Intent?){
        val authType = intent?.extras?.getString("AUTH_TYPE").toString()

        if(authType == AuthType.SIGN_IN){
            removeImageFromDB()
        } else if(authType == AuthType.SIGN_UP){
            removeImageFromDB()
        } else if(authType == AuthType.NAVIGATE_TO_SIGNED_IN){
//            Log.d("LLL", "nav to signed in")
        } else if(authType == AuthType.NAVIGATE_TO_SIGN_UP){
            removeImageFromDB()
        }
    }

    fun removeImageFromDB(){
//        settingViewModel.deleteProfilePic()
    }

    fun setBottomNavVisibility(navController: NavController){
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavView.visibility = View.VISIBLE
            if(destination.id != R.id.chatsFragment && destination.id != R.id.settingsFragment && destination.id != R.id.profileFragment){
                binding.bottomNavView.visibility = View.GONE
            }
        }
    }

    fun checkIfChatPageFragment(navController: NavController, isChatFragment: (bool: Boolean)-> Unit){
        navController.addOnDestinationChangedListener { _, destination, arguments ->
            if(destination.id == R.id.chatPageFragment){

            }
        }
    }


//    private fun initialiseSettingViewModel(){
//        val vmFactory = SettingViewModelFactory((this.application as FirechatApplication).database.settingDao)
//        settingViewModel = ViewModelProvider(this, vmFactory).get(SettingViewModel::class.java)
//    }

    private fun initialiseProfileImageViewModel(){
        val vmFactory = ProfileImageViewModelFactory((this.application as FirechatApplication).profileImageDatabase.profileImageDao)
        profileImageViewModel = ViewModelProvider(this, vmFactory).get(ProfileImageViewModel::class.java)
    }

    private fun initialiseImageViewModel(){
        val vmFactory = ImageViewModelFactory((this.application as FirechatApplication).imageDatabase.imageDao)
        imageViewModel = ViewModelProvider(this, vmFactory).get(ImageViewModel::class.java)
    }

    private fun initialisePublicPostViewModel(){
        val vmFactory = PublicPostViewModelFactory((this.application as FirechatApplication).publicPostDatabase.publicPostDao)
        publicPostViewModel = ViewModelProvider(this, vmFactory).get(PublicPostViewModel::class.java)
    }

    private fun initialiseReadReceiptsViewModel(){
        val vmFactory = ReadReceiptViewModelFactory((this.application as FirechatApplication).readReceiptsDatabase.readReceiptDao)
        readReceiptViewModel = ViewModelProvider(this, vmFactory).get(ReadReceiptViewModel::class.java)
    }

    private fun initialiseSettingsDatastore(){
        dataStore = createDataStore("settings")
    }

    private fun fetchCurrentUserProfileImage(){
        Log.d("IMAGE_FETCH", "Get image called for CURRENT USER")
        firebaseViewModel.getProfileImage(firebaseAuth.currentUser!!.uid, firebaseStorage, mDbRef, {
            if(it != null){
                profileImageViewModel.storeImage(it)
                profileImageViewModel.profileImage_currentUser.value = it
            }
        }, {

        }, { exists ->
            if(!exists){
                val currentUser = firebaseAuth.currentUser!!.uid
                profileImageViewModel.nullifyImageInRoom(currentUser)
            }
        })
    }

    private fun determineCurrentUserImgFetchMethod(user: User){

        val imageLiveData = profileImageViewModel.checkForProfileImageInRoom(user.userUID)

        imageLiveData?.observeOnce(this, Observer {
            if(it != null && user.imgChangeTimestamp == it.imgChangeTimestamp){
                Log.d("IMAGE_CHECK", "current user display image gotten from database")
                if(it.image != null){
                    profileImageViewModel.profileImage_currentUser.value = it
                }
            } else if(it != null && it.imgChangeTimestamp == 0L){
                // Runs if the img change timestamp was empty the first time
                Log.d("IMAGE_CHECK", "current user display image NULL from database")
            } else {
                Log.d("IMAGE_CHECK", "CURRENT USER DISPLAY IMAGE GOTTEN FROM FIREBASE")
                fetchCurrentUserProfileImage()
            }
        })
    }

    private fun fetchProfileImage(userId: String, afterCallback: (image: String?)-> Unit){
        Log.d("IMAGE_FETCH", "Get image called for ${userId}")

        firebaseViewModel.getProfileImage(userId, firebaseStorage, mDbRef, {
            if(it != null){
                profileImageViewModel.storeImage(it)
                afterCallback(it.image)
            } else {
                afterCallback(null)
            }
            // TODO: Remove image from DB if it is null
        }, {
            afterCallback(null)
        }, { exists ->
            if(!exists){
                profileImageViewModel.nullifyImageInRoom(userId)
            }
        })
    }

    private fun fetchProfileImage_fullObject(userId: String, afterCallback: (image: ProfileImage?)-> Unit){
        Log.d("IMAGE_FETCH", "Get image called for ${userId}")

        firebaseViewModel.getProfileImage(userId, firebaseStorage, mDbRef, {
            if(it != null){
                profileImageViewModel.storeImage(it)
                afterCallback(it)
            } else {
                afterCallback(null)
            }
            // TODO: Remove image from DB if it is null
        }, {
            afterCallback(null)
        }, { exists ->
            if(!exists){
                profileImageViewModel.nullifyImageInRoom(userId)
            }
        })
    }

    fun determineOtherImgFetchMethod(user: User, fetchCallback: (image: String?)-> Unit, dbCallback: (image: String?)-> Unit){
        val imageLiveData = profileImageViewModel.checkForProfileImageInRoom(user.userUID)

        imageLiveData?.observeOnce(this, Observer {
            if(it != null && user.imgChangeTimestamp == it.imgChangeTimestamp){
                Log.d("IMAGE_CHECK", "other user display image gotten from database")
                if(it.image != null){
                    dbCallback(it.image)
                }
            } else if(it != null && it.imgChangeTimestamp == 0L){
                Log.d("IMAGE_CHECK", "other user display NULL from database")
            } else {
                profileImageViewModel.isNotUserInBlacklist(user,{
                    profileImageViewModel.addUserToBlacklist(user)
                    Log.d("IMAGE_CHECK", "OTHER USER DISPLAY IMAGE GOTTEN FROM FIREBASE")
                    fetchProfileImage(user.userUID) {
                        fetchCallback(it)
                    }
                }, {
                    fetchCallback(null)
                })
            }
        })
    }

    fun determineOtherImgFetchMethod_fullObject(user: User, fetchCallback: (image: ProfileImage?)-> Unit, dbCallback: (image: ProfileImage?)-> Unit){
        val imageLiveData = profileImageViewModel.checkForProfileImageInRoom(user.userUID)

        imageLiveData?.observeOnce(this, Observer {
            if(it != null && user.imgChangeTimestamp == it.imgChangeTimestamp){
                Log.d("IMAGE_CHECK", "other user display image gotten from database")
                if(it.image != null){
                    dbCallback(it)
                } else {
                    dbCallback(null)
                }
            } else if(it != null && it.imgChangeTimestamp == 0L){
                Log.d("IMAGE_CHECK", "other user display NULL from database")
            } else {
                profileImageViewModel.isNotUserInBlacklist(user,{
                    profileImageViewModel.addUserToBlacklist(user)
                    Log.d("IMAGE_CHECK", "OTHER USER DISPLAY IMAGE GOTTEN FROM FIREBASE")
                    fetchProfileImage_fullObject(user.userUID) {
                        fetchCallback(it)
                    }
                }, {
                    fetchCallback(null)
                })
            }
        })
    }

    fun determineGroupFetchMethod(group: GroupRoom, fetchCallback: (image: String?)-> Unit, dbCallback: (image: String?)-> Unit){
        val imageLiveData = profileImageViewModel.checkForProfileImageInRoom(group.roomUID)

        imageLiveData?.observeOnce(this, Observer {
            if(it != null && group.imgChangeTimestamp == it.imgChangeTimestamp){
                Log.d("IMAGE_CHECK", "Group display image gotten from database")
                if(it.image != null){
                    dbCallback(it.image)
                }
            } else if(it != null && it.imgChangeTimestamp == 0L){
                Log.d("IMAGE_CHECK", "Group display NULL from database")
            }else {
                profileImageViewModel.isNotGroupInBlacklist(group,{
                    profileImageViewModel.addGroupToBlacklist(group)
                    Log.d("IMAGE_CHECK", "GROUP DISPLAY IMAGE GOTTEN FROM FIREBASE")
                    fetchProfileImage(group.roomUID) {
                        fetchCallback(it)
                    }
                }, {
                    fetchCallback(null)
                })
            }
        })
    }

    fun determineGroupFetchMethod_fullObject(group: GroupRoom, fetchCallback: (image: ProfileImage?)-> Unit, dbCallback: (image: ProfileImage?)-> Unit){
        val imageLiveData = profileImageViewModel.checkForProfileImageInRoom(group.roomUID)

        imageLiveData?.observeOnce(this, Observer {
            if(it != null && group.imgChangeTimestamp == it.imgChangeTimestamp){
                Log.d("IMAGE_CHECK", "Group display image gotten from database")
                dbCallback(it)
            } else if(it != null && it.imgChangeTimestamp == 0L){
                Log.d("IMAGE_CHECK", "Group display NULL from database")
            }else {
                profileImageViewModel.isNotGroupInBlacklist(group,{
                    profileImageViewModel.addGroupToBlacklist(group)
                    Log.d("IMAGE_CHECK", "GROUP DISPLAY IMAGE GOTTEN FROM FIREBASE")
                    fetchProfileImage_fullObject(group.roomUID) {
                        fetchCallback(it)
                    }
                }, {
                    fetchCallback(null)
                })
            }
        })
    }


    fun fetchChatImage(imageId: String, chatRoomId: String, afterCallback: (image: String?)-> Unit){
        Log.d("IMAGE_FETCH", "Get image called for ${imageId}")

        firebaseViewModel.getChatImage(imageId, chatRoomId, mDbRef, firebaseStorage, {
            if(it != null){
                imageViewModel.storeImage(it)
                afterCallback(it.image)
            } else {
                afterCallback(null)
            }
        }, {
            afterCallback(null)
        })
    }

    fun determineMessageImgFetchMethod(message: Message, chatRoomId: String, fetchCallback: (image: String?)-> Unit, dbCallback: (image: String?)-> Unit){
        val imageLiveData = imageViewModel.checkForImgInRoom(message.message)

        imageLiveData.observeOnce(this, Observer {
            if(it != null){
                Log.d("IMAGE_CHECK", "Chat image gotten from database")
                dbCallback(it.image)
            } else {
                Log.d("IMAGE_CHECK", "CHAT IMAGE GOTTEN FROM FIREBASE")
                fetchChatImage(message.message, chatRoomId) {
                    fetchCallback(it)
                }
            }
        })
    }

    /*
        Fetches chat image from firebase,
        If it exists, it stores the image in room and provides it in a callback,
        else, it returns null in that callback
    */
    fun fetchChatImage_fullObject(imageId: String, chatRoomId: String, afterCallback: (image: Image?)-> Unit){
        Log.d("IMAGE_FETCH", "Get image from firebase called for ${imageId}")

        firebaseViewModel.getChatImage(imageId, chatRoomId, mDbRef, firebaseStorage, {
            if(it != null){
                // TODO: store image
                imageViewModel.storeImage(it)
                afterCallback(it)
            } else {
                afterCallback(null)
            }
        }, {
            afterCallback(null)
        })
    }

    fun determineMessageImgFetchMethod_fullObject(message: Message, chatRoomId: String, imgCallback: (image: Image?)-> Unit){
        val imageLiveData = imageViewModel.checkForImgInRoom(message.message)

        imageLiveData.observeOnce(this, Observer {
            if(it != null){
                Log.d("IMAGE_CHECK", "Chat image gotten from database")
                imgCallback(it)
            } else {
                Log.d("IMAGE_CHECK", "CHAT IMAGE GOTTEN FROM FIREBASE")
                fetchChatImage_fullObject(message.message, chatRoomId) {
                    imgCallback(it)
                }
            }
        })
    }

    /*
        Fetches PUBLIC POST from firebase,
        If it exists, it stores the PUBLIC POST in room and provides it in a callback,
        else, it returns null in that callback
    */
    fun fetchPublicPost_fullObject(postId: String, afterCallback: (publicPost: PublicPost?)-> Unit){
        Log.d("POST_FETCH", "Get public post from firebase called for ${postId}")

        firebaseViewModel.getPublicPost(postId, firebaseStorage,mDbRef, {
            if(it != null){
                // TODO: store image
                publicPostViewModel.storePost(it)
                afterCallback(it)
            } else {
                afterCallback(null)
            }
        }, {
            afterCallback(null)
        })
    }

    /*
        Checks if the public post is in room,
        if: returns the post from room in a callback
        else: fetches from firebase and returns it in a callback
    */
    fun determinePublicPostFetchMethod_fullObject(postId: String, postCallback: (publicPost: PublicPost?)-> Unit){
        val postLiveData = publicPostViewModel.checkIfPostInRoom(postId)

        postLiveData.observeOnce(this, Observer {
            if(it != null){
                Log.d("POST_CHECK", "Public post gotten from database")
                postCallback(it)
            } else {
                Log.d("POST_CHECK", "PUBLIC POST GOTTEN FROM FIREBASE")
                fetchPublicPost_fullObject(postId) {
                    postCallback(it)
                }
            }
        })
    }


    /*
        Just checks if the image is in the database,
        It does not fetch
    */
    fun checkIfImgMessageInDb(message: Message, image: (image: Image?)-> Unit){
        val imageLiveData = imageViewModel.checkForImgInRoom(message.message)

        imageLiveData.observeOnce(this, Observer {
            if(it != null){
                Log.d("IMAGE_CHECK", "Chat exists database")
                image(it)
            } else {
                Log.d("IMAGE_CHECK", "CHAT IMAGE DOES NOT EXIST IN DATABASE")
                image(null)
            }
        })
    }

    fun checkIfPostInDb(ID: String, post: (post: PublicPost?)-> Unit){
        val postLiveData = publicPostViewModel.checkIfPostInRoom(ID)

        postLiveData.observeOnce(this, Observer {
            if(it != null){
                Log.d("POST_CHECK", "Post exists database")
                post(it)
            } else {
                Log.d("POST_CHECK", "POST DOES NOT EXIST IN DATABASE")
                post(null)
            }
        })
    }

    var offlineInfobarTimer: Timer? = null
    private fun checkConnectivity(){
        firebaseViewModel.checkFirebaseConnection {
            if(it){
                firebaseViewModel.isConnectedToDatabase.value = true
                showBottomInfobar(this.getString(R.string.back_online), InfobarColors.ONLINE)

                if(offlineInfobarTimer != null){
                    offlineInfobarTimer?.cancel()
                }

//                if(timer != null){
//                    timer?.cancel()
//                }
                binding.networkErrorOverlay.visibility = View.GONE
            } else {
                firebaseViewModel.isConnectedToDatabase.value = false
                offlineInfobarTimer = fixedRateTimer("no_connection_timer", false, 0L, 5 * 1000 + 3000) {
                    showBottomInfobar(this@SignedinActivity.getString(R.string.no_connection), InfobarColors.OFFLINE)
                }

//                timer = signedinViewModel.setNetworkOverlayTimer {
//                    binding.networkErrorOverlay.visibility = View.VISIBLE
//                }
            }
        }
    }

//    private fun setOverlayClickListeners(){
//        binding.networkErrorOverlay.setOnClickListener {
//            dismissNetworkErrorOverlay()
//            timer = signedinViewModel.setNetworkOverlayTimer {
//                binding.networkErrorOverlay.visibility = View.VISIBLE
//            }
//        }
//
//        binding.cancelNetworkErrorOverlay.setOnClickListener {
//            dismissNetworkErrorOverlay()
//
//            timer = signedinViewModel.setNetworkOverlayTimer {
//                binding.networkErrorOverlay.visibility = View.VISIBLE
//            }
//        }
//    }

    private fun dismissNetworkErrorOverlay(){
        binding.networkErrorOverlay.visibility = View.GONE
    }

    private fun getCurrentUserRecurrent(){
        firebaseViewModel.getCurrentUserRecurrent(firebaseAuth, mDbRef, {  }, {

        })
    }

    private fun getFriendRequests(requests: HashMap<String, Long>){
        val users = mutableListOf<User>()
        val sortedMap = UserUtils.sortByTimestamp(requests.toSortedMap())

        for(i in sortedMap.keys){
            firebaseViewModel.getUserSingle(i, mDbRef, {
                if(it != null){
                    users.add(it)
                }
            }, {
                firebaseViewModel.friendRequests.value = users.reversed()
            })
        }
    }

    private fun getAllFriends(friends: HashMap<String, Long>){
        val sortedMap = UserUtils.sortByTimestamp(friends.toSortedMap())

        val users = mutableListOf<User>()
        for(i in sortedMap.keys){
            firebaseViewModel.getUserSingle(i, mDbRef, {
                if(it != null){
                    users.add(it)
                }
            }, {
                firebaseViewModel.setFriends(users)
            })
        }
    }

    private fun logout(activity: FragmentActivity, context: Context?, callback: ()-> Unit){
        val intent = Intent(context, SignedoutActivity::class.java)
        callback()

        activity.startActivity(intent)
        activity.finish()

        // run firebase logout
    }

    var getUserSingleTimestamp: Long = -1L
    private fun compareUsers(user: User){
        val chatRoomSize: Int = firebaseViewModel.currentUserSingle.value?.chatRooms?.size ?: 0
        val groupRoomSize: Int = firebaseViewModel.currentUserSingle.value?.groupRooms?.size ?: 0

        // collect timestamp whenever this function runs
        getUserSingleTimestamp = System.currentTimeMillis()
        // if a new chat room or group room is added to the array re-run get user single
        if(user.chatRooms?.size != chatRoomSize){
            signedinViewModel.getCurrentUserSingle(this)
        } else if(user.groupRooms?.size != groupRoomSize){
            signedinViewModel.getCurrentUserSingle(this)
        }
    }

    fun hideKeyboard() {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
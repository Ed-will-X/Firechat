package com.varsel.firechat.presentation.signedIn

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.varsel.firechat.R
import com.varsel.firechat.databinding.ActivitySignedinBinding
import com.varsel.firechat.data.local.Setting.Setting
import com.varsel.firechat.data.local.Setting.SettingViewModel
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.use_case._util.InfobarColors
import com.varsel.firechat.domain.use_case._util.InfobarControllerUseCase
import com.varsel.firechat.presentation.signedOut.SignedoutActivity
import com.varsel.firechat.presentation.signedOut.fragments.AuthType
import com.varsel.firechat.presentation.viewModel.FirebaseViewModel
import com.varsel.firechat.common._utils.ExtensionFunctions.Companion.collectLatestLifecycleFlow
import com.varsel.firechat.domain.repository.FirebaseRepository
import com.varsel.firechat.domain.use_case._util.message.FormatStampMessage_UseCase
import com.varsel.firechat.domain.use_case._util.message.GetLastMessage_UseCase
import com.varsel.firechat.domain.use_case._util.message.SortChats_UseCase
import com.varsel.firechat.domain.use_case._util.string.Truncate_UseCase
import com.varsel.firechat.domain.use_case._util.user.GetOtherUserId_UseCase
import com.varsel.firechat.domain.use_case._util.user.SortByTimestamp_UseCase
import com.varsel.firechat.domain.use_case.current_user.CheckServerConnectionUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

@AndroidEntryPoint
class SignedinActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignedinBinding
    lateinit var dataStore: DataStore<Preferences>
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var mDbRef: DatabaseReference
    lateinit var firebaseStorage: FirebaseStorage
    lateinit var firebaseViewModel: FirebaseViewModel
    lateinit var signedinViewModel: SignedinViewModel
//    var timer: CountDownTimer? = null
    val settingViewModel: SettingViewModel by viewModels()
    lateinit var infobarController: InfobarControllerUseCase

    @Inject
    lateinit var sortByTimestamp: SortByTimestamp_UseCase

    @Inject
    lateinit var truncate: Truncate_UseCase

    @Inject
    lateinit var getOtherUserId: GetOtherUserId_UseCase

    @Inject
    lateinit var formatStampMessage: FormatStampMessage_UseCase

    @Inject
    lateinit var sortChats: SortChats_UseCase

    @Inject
    lateinit var getLastMessage: GetLastMessage_UseCase

    @Inject
    lateinit var checkServerConnection: CheckServerConnectionUseCase

    @Inject
    lateinit var firebase: FirebaseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignedinBinding.inflate(layoutInflater)
        val view = binding.root

        firebaseViewModel = ViewModelProvider(this).get(FirebaseViewModel::class.java)

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference
        firebaseStorage = FirebaseStorage.getInstance()
        infobarController = InfobarControllerUseCase(this, this, binding.bottomInfobar, binding.bottomInfobarText)
        signedinViewModel = ViewModelProvider(this).get(SignedinViewModel::class.java)

        collectState()

        initialiseSettingConfiguration()

        determineAuthType(intent)

        // get current user recurrent

        setContentView(view)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.signed_in_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        checkConnectivity()
//        setOverlayClickListeners()

        binding.logoutTextBtn.setOnClickListener {
            logout(this, this) {

            }
        }

//        firebaseViewModel.chatRooms.observe(this, Observer {
//            val sorted = sortChats(it as MutableList<ChatRoom>)
//
//            // TODO: Collect receipts that came after getUserSingle was re-run
////            getNewMessage(it, navController)
//        })
//
//        firebaseViewModel.groupRooms.observe(this, Observer {
////            getNewGroupMessage(it, navController)
//        })


        binding.bottomNavView.setupWithNavController(navController)
        setBottomNavVisibility(navController)

        // Makes the chat fragment the default destination
        binding.bottomNavView.menu.findItem(R.id.chatsFragment).setChecked(true)


//        profileImageViewModel.setClearBlacklistCountdown()
    }

    private fun collectState() {
        collectLatestLifecycleFlow(signedinViewModel.signedInState) {
            if (it.currentUser != null) {
                determineShowRequesBottomInfobar(it.currentUser)
                determineNewFriendBottomInfobar(it.currentUser)
//                determineShowGroupAddBottomInfobar(it.currentUser)

                if(it.currentUser.public_posts != null && it.currentUser.public_posts!!.isNotEmpty()){
//                    getPublicPosts_first_5(it.public_posts?.values?.toList()!!)
                }
            }

//            if(it.currentUser?.friendRequests != null && it.currentUser.friendRequests.isNotEmpty()){
//                getFriendRequests(it.currentUser.friendRequests)
//            } else {
//                firebaseViewModel.friendRequests.value = mutableListOf<User>()
//            }

//            if(it.currentUser?.friends != null && it.currentUser.friends.isNotEmpty()){
//                getAllFriends(it.currentUser.friends)
//            } else {
//                firebaseViewModel.setFriends(listOf())
//            }
        }
    }

    // TODO: Run in a coroutine
    // DONT DELETE
//    private fun getNewMessage(chatRooms: MutableList<ChatRoom>, navController: NavController){
//        val lastRunTimestamp = getUserSingleTimestamp
//        val currentUserId = firebaseAuth.currentUser!!.uid
//
//        for(i in chatRooms){
//            val lastMessage = getLastMessage(i)
//            val selectedChatRoomUser = firebaseViewModel.selectedChatRoomUser.value
//
//            /*
//            *   Checks if the last message timestamp is higher than the last time get user single was fetched and,
//            *   the last message sender is the current user.
//            *   If both are true, it show the BottomInfobar.
//            * */
//            if((lastMessage?.time ?: 0L) > lastRunTimestamp && lastMessage?.sender != currentUserId){
//                getOtherUser(i) {
//                    /*
//                    *   Shows the info bar if the selected chatRoom user is not the one who just sent the message
//                    * */
//                    if(it.userUID != selectedChatRoomUser?.userUID || navController.currentDestination?.id != R.id.chatPageFragment){
//                        infobarController.showBottomInfobar(this.getString(R.string.new_message_from, truncate(it.name, 15)), InfobarColors.NEW_MESSAGE)
//                    }
//                }
//            }
//        }
//    }

    // TODO: Run in a coroutine
    // DONT DELETE
//    private fun getNewGroupMessage(groupRooms: MutableList<GroupRoom>, navController: NavController){
//        val lastRunTimestamp = getUserSingleTimestamp
//        val currentUserId = firebaseAuth.currentUser!!.uid
//        val selectedGroupRoom = firebaseViewModel.selectedGroupRoom
//
//        for (i in groupRooms){
//            val lastMessage = getLastMessage(i)
//
//            if((lastMessage?.time ?: 0L) > lastRunTimestamp && lastMessage?.sender != currentUserId){
//                if(i.roomUID != selectedGroupRoom.value?.roomUID || navController.currentDestination?.id != R.id.groupChatPageFragment){
//                    infobarController.showBottomInfobar(this.getString(R.string.new_message_in, truncate(i.groupName, 15)), InfobarColors.NEW_MESSAGE)
//                }
//            }
//        }
//    }

    var prevFriendRequests = -1
    fun determineShowRequesBottomInfobar(user: User){
        val sorted = sortByTimestamp(user.friendRequests.toSortedMap())

        if (prevFriendRequests < user.friendRequests.count() && prevFriendRequests != -1){
            firebase.getFirebaseInstance().getUserSingle(sorted.keys.last(), {
                infobarController.showBottomInfobar(this.getString(R.string.friend_request_From, truncate(it.name, 15)), InfobarColors.NEW_FRIEND_REQUEST)

            })
        }
        prevFriendRequests = user.friendRequests.count()
    }

    var prevFriends = -1
    fun determineNewFriendBottomInfobar(user: User){
        val sorted = sortByTimestamp(user.friends.toSortedMap())

        if(prevFriends < user.friends.count() && prevFriends != -1){
            firebase.getFirebaseInstance().getUserSingle(sorted.keys.last(), {
                infobarController.showBottomInfobar(this.getString(R.string.is_now_your_friend, truncate(it.name, 15)), InfobarColors.NEW_FRIEND)
            })
        }
        prevFriends = user.friends.count()
    }

//    var prevGroups = -1
//    fun determineShowGroupAddBottomInfobar(user: User){
//        val sorted = sortByTimestamp(user.groupRooms.toSortedMap())
//
//        if(prevGroups < user.groupRooms.count() && prevGroups != -1){
//            firebaseViewModel.getGroupChatRoomSingle(sorted.keys.last(), mDbRef, {
//                if(it?.admins?.contains(firebaseAuth.currentUser!!.uid) == false){
//                    infobarController.showBottomInfobar(this.getString(R.string.you_have_been_added_to, truncate(it.groupName, 10)), InfobarColors.GROUP_ADD)
//                }
//            }, {})
//        }
//        prevGroups = user.groupRooms.count()
//    }

    fun hideStatusBar(){
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    fun showStatusBar(){
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    fun changeStatusBarColor(color: Int, light: Boolean){
        if (Build.VERSION.SDK_INT >= 21) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.setStatusBarColor(getResources().getColor(color))

            // new api
            ViewCompat.getWindowInsetsController(window.decorView)?.apply {
                // Light text == dark status bar
                isAppearanceLightStatusBars = light
            }
            // old api (Uncomment the cole below if the status bar is buggy on older devices)
//            val decorView = window.decorView
//            decorView.systemUiVisibility =
//                if (light) {
//                    decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
//                } else {
//                    decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//                }
        }
    }

    fun isNightMode(): Boolean{
        when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                return true
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                return false
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                return false
            }
            else -> {
                return false
            }
        }
    }

    override fun onBackPressed() {
        if(binding.imgOverlayParent.visibility == View.VISIBLE) {
            binding.imgOverlayParent.visibility = View.GONE
            binding.imgOverlayName.setText("")
            binding.imgOverlayType.setText("")
            binding.imgOverlayTimestamp.setText("")
            showStatusBar()

            binding.imgOverlayImage.setImageBitmap(null)
        } else {
            super.onBackPressed()
        }
    }

//    fun handleImgBackPress(){
//        imageViewModel.showProfileImage.value = false
//    }

    fun determineAuthType(intent: Intent?){
        val authType = intent?.extras?.getString("AUTH_TYPE").toString()

        if(authType == AuthType.SIGN_IN){

        } else if(authType == AuthType.SIGN_UP){

        } else if(authType == AuthType.NAVIGATE_TO_SIGNED_IN){
//            Log.d("LLL", "nav to signed in")
        } else if(authType == AuthType.NAVIGATE_TO_SIGN_UP){

        }
    }

    fun setBottomNavVisibility(navController: NavController){
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavView.visibility = View.VISIBLE
            if(destination.id != R.id.chatsFragment && destination.id != R.id.settingsFragment && destination.id != R.id.profileFragment){
                binding.bottomNavView.visibility = View.GONE
            }
        }
    }


    private fun initialiseSettingConfiguration(){
        val currentUserID = firebaseAuth.currentUser!!.uid
        val settingLiveData = settingViewModel.getSetting(currentUserID)

        settingLiveData.observe(this, Observer {
            if(it != null){
                settingViewModel.settingConfig.value = it
            } else {
                val setting = Setting(currentUserID)
                settingViewModel.storeSetting(setting)
            }
        })
    }

    var offlineInfobarTimer: Timer? = null
    private fun checkConnectivity(){
        checkServerConnection().onEach {
            if(it){
                infobarController.showBottomInfobar(this.getString(R.string.back_online), InfobarColors.ONLINE)

                if(offlineInfobarTimer != null){
                    offlineInfobarTimer?.cancel()
                }

                binding.networkErrorOverlay.visibility = View.GONE
            } else {
                offlineInfobarTimer = fixedRateTimer("no_connection_timer", false, 0L, 5 * 1000 + 3000) {
                    infobarController.showBottomInfobar(this@SignedinActivity.getString(R.string.no_connection), InfobarColors.OFFLINE)
                }
            }
        }.launchIn(lifecycleScope)
//        firebaseViewModel.checkFirebaseConnection {
//            if(it){
//                firebaseViewModel.isConnectedToDatabase.value = true
//                infobarController.showBottomInfobar(this.getString(R.string.back_online), InfobarColors.ONLINE)
//
//                if(offlineInfobarTimer != null){
//                    offlineInfobarTimer?.cancel()
//                }
//
//                binding.networkErrorOverlay.visibility = View.GONE
//            } else {
//                firebaseViewModel.isConnectedToDatabase.value = false
//                offlineInfobarTimer = fixedRateTimer("no_connection_timer", false, 0L, 5 * 1000 + 3000) {
//                    infobarController.showBottomInfobar(this@SignedinActivity.getString(R.string.no_connection), InfobarColors.OFFLINE)
//                }
//            }
//        }
    }

//    private fun getFriendRequests(requests: HashMap<String, Long>){
//        val users = mutableListOf<User>()
//        val sortedMap = sortByTimestamp(requests.toSortedMap())
//
//        for(i in sortedMap.keys){
//            firebaseViewModel.getUserSingle(i, mDbRef, {
//                if(it != null){
//                    users.add(it)
//                }
//            }, {
//                firebaseViewModel.friendRequests.value = users.reversed()
//            })
//        }
//    }

//    private fun getAllFriends(friends: HashMap<String, Long>){
//        val sortedMap = sortByTimestamp(friends.toSortedMap())
//
//        val users = mutableListOf<User>()
//        for(i in sortedMap.keys){
//            firebaseViewModel.getUserSingle(i, mDbRef, {
//                if(it != null){
//                    users.add(it)
//                }
//            }, {
//                firebaseViewModel.setFriends(users)
//            })
//        }
//    }

    private fun logout(activity: FragmentActivity, context: Context?, callback: ()-> Unit){
        val intent = Intent(context, SignedoutActivity::class.java)
        callback()

        activity.startActivity(intent)
        activity.finish()

        // run firebase logout
    }

    // TODO: Delete
//    var getUserSingleTimestamp: Long = -1L
//    private fun compareUsers(user: User){
//        val chatRoomSize: Int = firebaseViewModel.currentUserSingle.value?.chatRooms?.size ?: 0
//        val groupRoomSize: Int = firebaseViewModel.currentUserSingle.value?.groupRooms?.size ?: 0
//
//        // collect timestamp whenever this function runs
//        getUserSingleTimestamp = System.currentTimeMillis()
//        // if a new chat room or group room is added to the array re-run get user single
//        if(user.chatRooms?.size != chatRoomSize){
//            signedinViewModel.getCurrentUserSingle(this)
//        } else if(user.groupRooms?.size != groupRoomSize){
//            signedinViewModel.getCurrentUserSingle(this)
//        }
//    }

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
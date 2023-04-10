package com.varsel.firechat.presentation.signedIn

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.createDataStore
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.varsel.firechat.R
import com.varsel.firechat.databinding.ActivitySignedinBinding
import com.varsel.firechat.data.local.Setting.SettingViewModel
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.use_case._util.InfobarColors
import com.varsel.firechat.domain.use_case._util.InfobarControllerUseCase
import com.varsel.firechat.presentation.signedOut.SignedoutActivity
import com.varsel.firechat.presentation.signedOut.fragments.AuthType
import com.varsel.firechat.common._utils.ExtensionFunctions.Companion.collectLatestLifecycleFlow
import com.varsel.firechat.domain.repository.FirebaseRepository
import com.varsel.firechat.domain.use_case._util.message.FormatStampMessage_UseCase
import com.varsel.firechat.domain.use_case._util.message.GetLastMessage_UseCase
import com.varsel.firechat.domain.use_case._util.message.SortChats_UseCase
import com.varsel.firechat.domain.use_case._util.notification.CreateNotificationMessageChannel_UseCase
import com.varsel.firechat.domain.use_case._util.status_bar.SetStatusBarVisibility_UseCase
import com.varsel.firechat.domain.use_case._util.status_bar.StatusBarVisibility
import com.varsel.firechat.domain.use_case._util.string.Truncate_UseCase
import com.varsel.firechat.domain.use_case._util.theme.SetThemeConfiguration_UseCase
import com.varsel.firechat.domain.use_case._util.user.GetOtherUserId_UseCase
import com.varsel.firechat.domain.use_case._util.user.SortByTimestamp_UseCase
import com.varsel.firechat.domain.use_case.current_user.CheckServerConnectionUseCase
import com.varsel.firechat.domain.use_case.last_online.UpdateLastOnline_UseCase
import com.varsel.firechat.domain.use_case.settings.GetSetting_Boolean_UseCase
import com.varsel.firechat.domain.use_case.settings.StoreSetting_boolean_UseCase
import com.varsel.firechat.presentation.signedIn.fragments.screen_groups.bottomNav.settings.SettingKeys_Boolean
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

@AndroidEntryPoint
class SignedinActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignedinBinding
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var mDbRef: DatabaseReference
    lateinit var firebaseStorage: FirebaseStorage
    lateinit var signedinViewModel: SignedinViewModel
    val settingViewModel: SettingViewModel by viewModels()
    lateinit var infobarController: InfobarControllerUseCase
    lateinit var datastore: DataStore<Preferences>

    var timer: Timer? = null

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

    @Inject
    lateinit var setStatusBarVisibility: SetStatusBarVisibility_UseCase

    @Inject
    lateinit var getBoolean: GetSetting_Boolean_UseCase

    @Inject
    lateinit var storeBoolean: StoreSetting_boolean_UseCase

    @Inject
    lateinit var createNotificationChannel_message: CreateNotificationMessageChannel_UseCase

    @Inject
    lateinit var setThemeConfiguration: SetThemeConfiguration_UseCase

    @Inject
    lateinit var updateLastOnline: UpdateLastOnline_UseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignedinBinding.inflate(layoutInflater)
        val view = binding.root


        datastore = createDataStore(getString(R.string.settings).toLowerCase())
//        setThemeConfiguration(datastore, lifecycleScope)

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference
        firebaseStorage = FirebaseStorage.getInstance()
        infobarController = InfobarControllerUseCase(this, this, binding.bottomInfobar, binding.bottomInfobarText)
        signedinViewModel = ViewModelProvider(this).get(SignedinViewModel::class.java)
        createNotificationChannel_message(this)
        collectState()


        determineAuthType(intent)

        // get current user recurrent

        setContentView(view)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.signed_in_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

//        setOverlayClickListeners()

        binding.logoutTextBtn.setOnClickListener {
            logout(this, this) {

            }
        }

        binding.bottomNavView.setupWithNavController(navController)
        setBottomNavVisibility(navController)

        // Makes the chat fragment the default destination
        binding.bottomNavView.menu.findItem(R.id.chatsFragment).setChecked(true)

        KeyboardVisibilityEvent.setEventListener(
            this,
            object : KeyboardVisibilityEventListener {
                override fun onVisibilityChanged(isOpen: Boolean) {
                    val destination = signedinViewModel.destinationId.value
                    if(destination == null) {
                        return
                    }
                    if(destination != R.id.chatsFragment && destination != R.id.settingsFragment && destination != R.id.profileFragment){
                        binding.bottomNavView.isVisible = false
                    } else {
                        binding.bottomNavView.isVisible = !isOpen
                    }
                }
            }
        )
//        profileImageViewModel.setClearBlacklistCountdown()
    }

    override fun onPause() {
        super.onPause()
        Log.d("LLL", "On Pause Ran")

        if(timer != null) {
            timer?.cancel()
            timer = null
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("LLL", "On Resume Ran")
        lifecycleScope.launch {
            val show_last_seen = getBoolean(SettingKeys_Boolean.SHOW_LAST_SEEN, datastore)

            if(show_last_seen == true || show_last_seen == null) {
                checkConnectivity({
                    if(timer == null) {
                        timer = updateLastOnline(this@SignedinActivity)
                    }
                }, {
                    if(timer != null) {
                        timer?.cancel()
                        timer = null
                    }
                })
            }
        }

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
        }
    }

    fun hideBottomNav() {

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

    // DONT DELETE
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


    override fun onBackPressed() {
        if(binding.imgOverlayParent.visibility == View.VISIBLE) {
            binding.imgOverlayParent.visibility = View.GONE
            binding.imgOverlayName.setText("")
            binding.imgOverlayType.setText("")
            binding.imgOverlayTimestamp.setText("")
            setStatusBarVisibility(StatusBarVisibility.Show(), this)

            binding.imgOverlayImage.setImageBitmap(null)
        } else {
            super.onBackPressed()
        }
    }

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
            signedinViewModel.destinationId.value = destination.id

            if(destination.id != R.id.chatsFragment && destination.id != R.id.settingsFragment && destination.id != R.id.profileFragment){
                binding.bottomNavView.visibility = View.GONE
            }
        }
    }



    var offlineInfobarTimer: Timer? = null
    private fun checkConnectivity(onlineCallback: ()-> Unit = {}, offlineCallback: ()-> Unit = {}){
        checkServerConnection().onEach {
            if(it){
                onlineCallback()
                infobarController.showBottomInfobar(this.getString(R.string.back_online), InfobarColors.ONLINE)

                if(offlineInfobarTimer != null){
                    offlineInfobarTimer?.cancel()
                }

                binding.networkErrorOverlay.visibility = View.GONE
            } else {
                offlineCallback()

                offlineInfobarTimer = fixedRateTimer("no_connection_timer", false, 0L, 5 * 1000 + 3000) {
                    infobarController.showBottomInfobar(this@SignedinActivity.getString(R.string.no_connection), InfobarColors.OFFLINE)
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun logout(activity: FragmentActivity, context: Context?, callback: ()-> Unit){
        val intent = Intent(context, SignedoutActivity::class.java)
        callback()

        activity.startActivity(intent)
        activity.finish()

        // run firebase logout
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
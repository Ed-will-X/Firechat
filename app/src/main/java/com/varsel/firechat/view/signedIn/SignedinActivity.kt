package com.varsel.firechat.view.signedIn

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.varsel.firechat.FirechatApplication
import com.varsel.firechat.R
import com.varsel.firechat.databinding.ActivitySignedinBinding
import com.varsel.firechat.model.Chat.GroupRoom
import com.varsel.firechat.model.User.User
import com.varsel.firechat.model.ProfileImage.ProfileImageViewModel
import com.varsel.firechat.model.ProfileImage.ProfileImageViewModelFactory
import com.varsel.firechat.utils.ExtensionFunctions.Companion.observeOnce
import com.varsel.firechat.view.signedOut.SignedoutActivity
import com.varsel.firechat.view.signedOut.fragments.AuthType
import com.varsel.firechat.viewModel.FirebaseViewModel
import com.varsel.firechat.viewModel.SignedinViewModel

class SignedinActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignedinBinding
    lateinit var dataStore: DataStore<Preferences>
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var mDbRef: DatabaseReference
    lateinit var firebaseViewModel: FirebaseViewModel
    lateinit var signedinViewModel: SignedinViewModel
    var timer: CountDownTimer? = null
//    lateinit var settingViewModel: SettingViewModel
    lateinit var profileImageViewModel: ProfileImageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseViewModel = ViewModelProvider(this).get(FirebaseViewModel::class.java)
        signedinViewModel = ViewModelProvider(this).get(SignedinViewModel::class.java)

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference

//        initialiseSettingViewModel()
        initialiseImageViewModel()

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
        setOverlayClickListeners()

        binding.logoutTextBtn.setOnClickListener {
            logout(this, this) {

            }
        }

        firebaseViewModel.currentUser.observe(this, Observer {
            if (it != null) {
                compareUsers(it)
                determineCurrentUserImgFetchMethod(it)
            }

            if(it?.friendRequests != null){
                getFriendRequests(it?.friendRequests?.values!!.toList())
            } else {
                firebaseViewModel.friendRequests.value = mutableListOf<User>()
            }

            if(it?.friends != null){
                getAllFriends(it?.friends?.values!!.toList())
            } else {
                firebaseViewModel.friends.value = mutableListOf<User>()
            }
        })

        binding.bottomNavView.setupWithNavController(navController)
        setBottomNavVisibility(navController)

        // Makes the chat fragment the default destination
        binding.bottomNavView.menu.findItem(R.id.chatsFragment).setChecked(true)

//        profileImageViewModel.setClearBlacklistCountdown()
    }

    private fun enablePersistence(){
        Firebase.database.setPersistenceEnabled(true)
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
            if(destination.id == R.id.addFriends){
                binding.bottomNavView.visibility = View.GONE
            }
            if(destination.id == R.id.otherProfileFragment){
                binding.bottomNavView.visibility = View.GONE
            }
            if(destination.id == R.id.chatPageFragment){
                binding.bottomNavView.visibility = View.GONE
            }
            if(destination.id == R.id.editProfilePage){
                binding.bottomNavView.visibility = View.GONE
            }
            if(destination.id == R.id.friendListFragment){
                binding.bottomNavView.visibility = View.GONE
            }
            if(destination.id == R.id.createGroupFragment){
                binding.bottomNavView.visibility = View.GONE
            }
            if(destination.id == R.id.groupChatPageFragment){
                binding.bottomNavView.visibility = View.GONE
            }
            if(destination.id == R.id.aboutUserFragment){
                binding.bottomNavView.visibility = View.GONE
            }
            if (destination.id == R.id.groupChatDetailFragment){
                binding.bottomNavView.visibility = View.GONE
            }
            if (destination.id == R.id.addGroupMembersFragment){
                binding.bottomNavView.visibility = View.GONE
            }
        }
    }


//    private fun initialiseSettingViewModel(){
//        val vmFactory = SettingViewModelFactory((this.application as FirechatApplication).database.settingDao)
//        settingViewModel = ViewModelProvider(this, vmFactory).get(SettingViewModel::class.java)
//    }

    private fun initialiseImageViewModel(){
        val vmFactory = ProfileImageViewModelFactory((this.application as FirechatApplication).profileImageDatabase.profileImageDao)
        profileImageViewModel = ViewModelProvider(this, vmFactory).get(ProfileImageViewModel::class.java)
    }

    private fun fetchCurrentUserProfileImage(){
        firebaseViewModel.getProfileImage(firebaseAuth.currentUser!!.uid, mDbRef, {
            if(it != null){
                profileImageViewModel.storeImage(it)
                profileImageViewModel.profileImageEncodedCurrentUser.value = it.image
            }
        }, {

        })
    }

    private fun determineCurrentUserImgFetchMethod(user: User){

        val imageLiveData = profileImageViewModel.checkForProfileImageInRoom(user.userUID)

        imageLiveData?.observeOnce(this, Observer {
            if(it != null && user.imgChangeTimestamp == it.imgChangeTimestamp){
                profileImageViewModel.profileImageEncodedCurrentUser.value = it.image
            } else {
                fetchCurrentUserProfileImage()
            }
        })
    }

    private fun fetchProfileImage(userId: String, afterCallback: (image: String?)-> Unit){
        firebaseViewModel.getProfileImage(userId, mDbRef, {
            if(it != null){
                profileImageViewModel.storeImage(it)
                afterCallback(it.image)
            } else {
                afterCallback(null)
            }
            // TODO: Remove image from DB if it is null
        }, {
            afterCallback(null)
            Log.d("LLL", "ddddddddddddddddddddddddddddddddddddddddd")
        })
    }

    fun determineOtherImgFetchMethod(user: User, fetchCallback: (image: String?)-> Unit, dbCallback: (image: String?)-> Unit){
        val imageLiveData = profileImageViewModel.checkForProfileImageInRoom(user.userUID)

        imageLiveData?.observeOnce(this, Observer {
            if(it != null && user.imgChangeTimestamp == it.imgChangeTimestamp){
                dbCallback(it.image)
            } else {
                profileImageViewModel.isNotUserInBlacklist(user,{
                    profileImageViewModel.addUserToBlacklist(user)
                    fetchProfileImage(user.userUID) {
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
                dbCallback(it.image)
            } else {
                profileImageViewModel.isNotGroupInBlacklist(group,{
                    profileImageViewModel.addGroupToBlacklist(group)
                    fetchProfileImage(group.roomUID) {
                        fetchCallback(it)
                    }
                }, {
                    fetchCallback(null)
                })
            }
        })
    }

    private fun checkConnectivity(){
        firebaseViewModel.checkFirebaseConnection {
            if(it){
                firebaseViewModel.isConnectedToDatabase.value = true

                if(timer != null){
                    timer?.cancel()
                }
                binding.networkErrorOverlay.visibility = View.GONE
            } else {
                firebaseViewModel.isConnectedToDatabase.value = false

                timer = signedinViewModel.setNetworkOverlayTimer {
                    binding.networkErrorOverlay.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setOverlayClickListeners(){
        binding.networkErrorOverlay.setOnClickListener {
            dismissNetworkErrorOverlay()
            timer = signedinViewModel.setNetworkOverlayTimer {
                binding.networkErrorOverlay.visibility = View.VISIBLE
            }
        }

        binding.cancelNetworkErrorOverlay.setOnClickListener {
            dismissNetworkErrorOverlay()

            timer = signedinViewModel.setNetworkOverlayTimer {
                binding.networkErrorOverlay.visibility = View.VISIBLE
            }
        }
    }

    private fun dismissNetworkErrorOverlay(){
        binding.networkErrorOverlay.visibility = View.GONE
    }

    private fun getCurrentUserRecurrent(){
        firebaseViewModel.getCurrentUserRecurrent(firebaseAuth, mDbRef, {  }, {

        })
    }

    private fun getFriendRequests(requests: List<String>){
        val users = mutableListOf<User?>()
        for(i in requests){
            firebaseViewModel.getUserSingle(i, mDbRef, {
                users.add(it)
            }, {
                firebaseViewModel.friendRequests.value = users
            })
        }
    }

    private fun getAllFriends(friends: List<String>){
        val users = mutableListOf<User?>()
        for(i in friends){
            firebaseViewModel.getUserSingle(i, mDbRef, {
                users.add(it)
            }, {
                firebaseViewModel.friends.value = users
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

    private fun compareUsers(user: User){
        val chatRoomSize: Int = firebaseViewModel.currentUserSingle.value?.chatRooms?.size ?: 0
        val groupRoomSize: Int = firebaseViewModel.currentUserSingle.value?.groupRooms?.size ?: 0
        // if a new chat room or group room is added to the array re-run get user single
        if(user.chatRooms?.size != chatRoomSize){
            signedinViewModel.getCurrentUserSingle(this)
        } else if(user.groupRooms?.size != groupRoomSize){
            signedinViewModel.getCurrentUserSingle(this)
        }
    }
}
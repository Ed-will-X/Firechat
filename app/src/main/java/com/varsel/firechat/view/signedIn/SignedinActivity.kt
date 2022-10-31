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
import com.varsel.firechat.model.Image.Image
import com.varsel.firechat.model.Image.ImageViewModel
import com.varsel.firechat.model.Image.ImageViewModelFactory
import com.varsel.firechat.model.Message.Message
import com.varsel.firechat.model.ProfileImage.ProfileImage
import com.varsel.firechat.model.ProfileImage.ProfileImageViewModel
import com.varsel.firechat.model.ProfileImage.ProfileImageViewModelFactory
import com.varsel.firechat.model.User.User
import com.varsel.firechat.utils.ExtensionFunctions.Companion.observeOnce
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.MessageUtils
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
    lateinit var imageViewModel: ImageViewModel
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

        initialiseProfileImageViewModel()
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

        setOverlayBindings()

//        profileImageViewModel.setClearBlacklistCountdown()
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
            } else {
                binding.imgOverlayParent.visibility = View.GONE
                imageViewModel.clearOverlayProps()
            }
        })

        binding.imgOverlayBack.setOnClickListener {
            imageViewModel.showProfileImage.value = false
        }

        binding.imgOverlayParent.setOnClickListener {
            imageViewModel.showProfileImage.value = false
        }
    }

    override fun onBackPressed() {
        if(imageViewModel.showProfileImage.value != true){
            super.onBackPressed()
        } else {
            imageViewModel.showProfileImage.value = false
        }
        Log.d("LLL", "Back pressed")
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
            } else if(destination.id == R.id.otherProfileFragment){
                binding.bottomNavView.visibility = View.GONE
            } else if(destination.id == R.id.chatPageFragment){
                binding.bottomNavView.visibility = View.GONE
            }else if(destination.id == R.id.editProfilePage){
                binding.bottomNavView.visibility = View.GONE
            } else if(destination.id == R.id.friendListFragment){
                binding.bottomNavView.visibility = View.GONE
            } else if(destination.id == R.id.createGroupFragment){
                binding.bottomNavView.visibility = View.GONE
            }else if(destination.id == R.id.groupChatPageFragment){
                binding.bottomNavView.visibility = View.GONE
            }else if(destination.id == R.id.aboutUserFragment){
                binding.bottomNavView.visibility = View.GONE
            } else if (destination.id == R.id.groupChatDetailFragment){
                binding.bottomNavView.visibility = View.GONE
            } else if (destination.id == R.id.addGroupMembersFragment){
                binding.bottomNavView.visibility = View.GONE
            } else if(destination.id == R.id.profileImageFragment){
                binding.bottomNavView.visibility = View.GONE
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

    private fun fetchCurrentUserProfileImage(){
        Log.d("IMAGE_FETCH", "Get image called for CURRENT USER")
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
                Log.d("IMAGE_CHECK", "current user display image gotten from database")
                profileImageViewModel.profileImageEncodedCurrentUser.value = it.image
            } else {
                Log.d("IMAGE_CHECK", "CURRENT USER DISPLAY IMAGE GOTTEN FROM FIREBASE")
                fetchCurrentUserProfileImage()
            }
        })
    }

    private fun fetchProfileImage(userId: String, afterCallback: (image: String?)-> Unit){
        Log.d("IMAGE_FETCH", "Get image called for ${userId}")

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
        })
    }

    private fun fetchProfileImage_fullObject(userId: String, afterCallback: (image: ProfileImage?)-> Unit){
        Log.d("IMAGE_FETCH", "Get image called for ${userId}")

        firebaseViewModel.getProfileImage(userId, mDbRef, {
            if(it != null){
                profileImageViewModel.storeImage(it)
                afterCallback(it)
            } else {
                afterCallback(null)
            }
            // TODO: Remove image from DB if it is null
        }, {
            afterCallback(null)
        })
    }

    fun determineOtherImgFetchMethod(user: User, fetchCallback: (image: String?)-> Unit, dbCallback: (image: String?)-> Unit){
        val imageLiveData = profileImageViewModel.checkForProfileImageInRoom(user.userUID)

        imageLiveData?.observeOnce(this, Observer {
            if(it != null && user.imgChangeTimestamp == it.imgChangeTimestamp){
                Log.d("IMAGE_CHECK", "other user display image gotten from database")
                dbCallback(it.image)
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
                dbCallback(it)
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
                dbCallback(it.image)
            } else {
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

    fun fetchChatImage(imageId: String, afterCallback: (image: String?)-> Unit){
        Log.d("IMAGE_FETCH", "Get image called for ${imageId}")

        firebaseViewModel.getChatImage(imageId, mDbRef, {
            if(it != null){
                // TODO: store image
                imageViewModel.storeImage(it)
                afterCallback(it.image)
            } else {
                afterCallback(null)
            }
            // TODO: Remove image from DB if it is null
        }, {
            afterCallback(null)
        })
    }



    // TODO: Implement determine chat image fetch
    fun determineMessageImgFetchMethod(message: Message, fetchCallback: (image: String?)-> Unit, dbCallback: (image: String?)-> Unit){
        val imageLiveData = imageViewModel.checkForImgInRoom(message.message)

        imageLiveData.observeOnce(this, Observer {
            if(it != null){
                Log.d("IMAGE_CHECK", "Chat image gotten from database")
                dbCallback(it.image)
            } else {
                Log.d("IMAGE_CHECK", "CHAT IMAGE GOTTEN FROM FIREBASE")
                fetchChatImage(message.message) {
                    fetchCallback(it)
                }
            }
        })
    }

    fun fetchChatImage_fullObject(imageId: String, afterCallback: (image: Image?)-> Unit){
        Log.d("IMAGE_FETCH", "Get image from firebase called for ${imageId}")

        firebaseViewModel.getChatImage(imageId, mDbRef, {
            if(it != null){
                // TODO: store image
                imageViewModel.storeImage(it)
                afterCallback(it)
            } else {
                afterCallback(null)
            }
            // TODO: Remove image from DB if it is null
        }, {
            afterCallback(null)
        })
    }

    fun determineMessageImgFetchMethod_fullObject(message: Message, imgCallback: (image: Image?)-> Unit){
        val imageLiveData = imageViewModel.checkForImgInRoom(message.message)

        imageLiveData.observeOnce(this, Observer {
            if(it != null){
                Log.d("IMAGE_CHECK", "Chat image gotten from database")
                imgCallback(it)
            } else {
                Log.d("IMAGE_CHECK", "CHAT IMAGE GOTTEN FROM FIREBASE")
                fetchChatImage_fullObject(message.message) {
                    imgCallback(it)
                }
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
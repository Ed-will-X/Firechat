package com.varsel.firechat.view.signedIn

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.varsel.firechat.R
import com.varsel.firechat.databinding.ActivitySignedinBinding
import com.varsel.firechat.model.Chat.ChatRoom
import com.varsel.firechat.model.Chat.GroupRoom
import com.varsel.firechat.model.User.User
import com.varsel.firechat.viewModel.FirebaseViewModel
import com.varsel.firechat.viewModel.SignedinViewModel


class SignedinActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignedinBinding
    lateinit var dataStore: DataStore<Preferences>
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var mDbRef: DatabaseReference
    lateinit var firebaseViewModel: FirebaseViewModel
    lateinit var signedinViewModel: SignedinViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseViewModel = ViewModelProvider(this).get(FirebaseViewModel::class.java)
        signedinViewModel = ViewModelProvider(this).get(SignedinViewModel::class.java)

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference

        // get current user recurrent
        getCurrentUserRecurrent()

        // get current user single
        signedinViewModel.getCurrentUserSingle(this)


        binding = ActivitySignedinBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.signed_in_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController



        firebaseViewModel.currentUser.observe(this, Observer {
            if (it != null) {
                compareUsers(it)
                Log.d("LLL", "R ${it?.chatRooms?.size}")
                Log.d("LLL", "C ${firebaseViewModel.currentUserSingle.value?.chatRooms?.size}")
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
            if(destination.id == R.id.chatDetailFragment){
                binding.bottomNavView.visibility = View.GONE
            }
            if (destination.id == R.id.groupChatDetailFragment){
                binding.bottomNavView.visibility = View.GONE
            }
        }
    }

    private fun getCurrentUserRecurrent(){
        firebaseViewModel.getCurrentUserRecurrent(firebaseAuth, mDbRef, { }, {

        })
    }

//    private fun getCurrentUserSingle(){
//        firebaseViewModel.getCurrentUserSingle(firebaseAuth, mDbRef, { }, {
//            // sets the chat rooms in the viewModel
//            if(firebaseViewModel.currentUser.value?.chatRooms != null){
//                getAllChats(firebaseViewModel.currentUser.value!!.chatRooms!!.values.toList())
//            } else {
//                firebaseViewModel.chatRooms.value = mutableListOf()
//            }
//
//            // Sets the groups in the viewModel
//            if(firebaseViewModel.currentUser.value?.groupRooms != null){
//                getAllGroupChats(firebaseViewModel.currentUser.value!!.groupRooms!!.values.toList())
//            } else {
//                firebaseViewModel.groupRooms.value = mutableListOf()
//            }
//
//        })
//    }

//    private fun getAllChats(chatRoomsUID: List<String>){
//        val chatRooms = mutableListOf<ChatRoom?>()
//        for(i in chatRoomsUID){
//            firebaseViewModel.getChatRoomRecurrent(i, mDbRef, { chatRoom ->
//                if(firebaseViewModel.chatRooms.value != null){
//                    val groupIterator = firebaseViewModel.chatRooms.value!!.iterator()
//                    while (groupIterator.hasNext()){
//                        val g = groupIterator.next()
//                        if(g?.roomUID == chatRoom?.roomUID){
//                            groupIterator.remove()
//                        }
//                    }
//                }
//                chatRooms.add(chatRoom)
//            }, {
//                firebaseViewModel.chatRooms.value = chatRooms
//            })
//        }
//    }
//
//    private fun getAllGroupChats(groupRoomIDs: List<String>){
//        val groupRooms = mutableListOf<GroupRoom?>()
//
//        for (i in groupRoomIDs){
//            // TODO: Fix bug here:
//            firebaseViewModel.getGroupChatRoomRecurrent(i, mDbRef, {
//                if(firebaseViewModel.groupRooms.value != null){
//                    val groupIterator = firebaseViewModel.groupRooms.value!!.iterator()
//                    while (groupIterator.hasNext()){
//                        val g = groupIterator.next()
//                        if(g?.roomUID == it?.roomUID){
//                            groupIterator.remove()
//                        }
//                    }
//                }
//                groupRooms.add(it)
//
//            }, {
//                firebaseViewModel.groupRooms.value = groupRooms
//            })
//        }
//    }

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

    private fun compareUsers(user: User){
        val chatRoomSize: Int = firebaseViewModel.currentUserSingle.value?.chatRooms?.size ?: 0
        val groupRoomSize: Int = firebaseViewModel.currentUserSingle.value?.groupRooms?.size ?: 0
        // if a new chat room or group room is added to the array re-run get user single
        if(user.chatRooms?.size != chatRoomSize){
            Log.d("LLL", "Ran")
            signedinViewModel.getCurrentUserSingle(this)
        } else if(user.groupRooms?.size != groupRoomSize){
            signedinViewModel.getCurrentUserSingle(this)
        }
    }
}
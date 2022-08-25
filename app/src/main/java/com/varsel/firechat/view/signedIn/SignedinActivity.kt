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
import com.varsel.firechat.model.User.User
import com.varsel.firechat.viewModel.FirebaseViewModel
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class SignedinActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignedinBinding
    lateinit var dataStore: DataStore<Preferences>
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var mDbRef: DatabaseReference
    lateinit var firebaseViewModel: FirebaseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseViewModel = ViewModelProvider(this).get(FirebaseViewModel::class.java)

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference

        // get user from DB
        getCurrentUser()

        binding = ActivitySignedinBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.signed_in_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        firebaseViewModel.currentUser.observe(this, Observer {
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
        }
    }

    private fun getCurrentUser(){
        firebaseViewModel.getCurrentUser(firebaseAuth, mDbRef, {

        }, {
            if(firebaseViewModel.currentUser.value?.chatRooms != null){
                getAllChats(firebaseViewModel.currentUser.value!!.chatRooms!!.values.toList())
            } else {
                firebaseViewModel.chatRooms.value = mutableListOf()
            }
        })
    }

    private fun getAllChats(chatRoomsUID: List<String>){
        val chatRooms = mutableListOf<ChatRoom?>()
        for(i in chatRoomsUID){
            firebaseViewModel.getChatRoom(i, mDbRef, {
                chatRooms.add(it)
            }, {
                firebaseViewModel.chatRooms.value = chatRooms
            })
        }
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
}
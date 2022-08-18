package com.varsel.firechat.viewModel

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.varsel.firechat.model.Chat.ChatRoom
import com.varsel.firechat.model.User.User

class FirebaseViewModel: ViewModel() {
    val usersLiveData = MutableLiveData<List<User>>()
    val selectedUser = MutableLiveData<User?>()
    val currentUser = MutableLiveData<User?>()
    val friendRequests = MutableLiveData<List<User?>>()
    var friends = MutableLiveData<List<User?>>()

    fun signUp(
        email: String,
        password: String,
        mAuth: FirebaseAuth,
        callbackSuccess: ()-> Unit,
        callbackFail: ()-> Unit
        ){
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if(task.isSuccessful){
                callbackSuccess()
            } else {
                callbackFail()
            }
        }
    }

    fun signin(
        email: String,
        password: String,
        mAuth: FirebaseAuth,
        callbackSuccess: ()-> Unit,
        callbackFail: () -> Unit
    ){
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if(it.isSuccessful){
                callbackSuccess()
            } else {
                callbackFail()
            }
        }
    }

    fun saveUser(
        name: String,
        email: String,
        UID: String,
        mDbRef: DatabaseReference,
        onSuccessCallback: ()-> Unit,
        onFailureCallback: ()-> Unit
    ){
        mDbRef.child("Users").child(UID).setValue(User(name, email, UID, null, null, null, false, null, null, null, null))
            .addOnCompleteListener {
                if(it.isSuccessful){
                    onSuccessCallback()
                } else {
                    onFailureCallback()
                }
            }
    }

    fun getCurrentUser(mAuth: FirebaseAuth?, mDbRef: DatabaseReference, beforeCallback: () -> Unit, afterCallback: () -> Unit){
        mDbRef.child("Users").orderByChild("userUID").equalTo(mAuth?.currentUser?.uid.toString()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                beforeCallback()

                for (item in snapshot.children){
                    val user = item.getValue(User::class.java)
                    currentUser.value = user
                }
                afterCallback()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    fun getUserById(uid: String, mDbRef: DatabaseReference, beforeCallback: () -> Unit, afterCallback: ()-> Unit = {}) {
        mDbRef.child("Users").orderByChild("userUID").equalTo(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                beforeCallback()

                for(item in snapshot.children){
                    val user = item.getValue(User::class.java)
                    selectedUser.value = user
                }

                afterCallback()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun getUserSingle(UID: String, mDbRef: DatabaseReference, loopCallback: (user: User?) -> Unit, afterCallback: () -> Unit){
        mDbRef.child("Users").orderByChild("userUID").equalTo(UID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children){
                    var user = i.getValue(User::class.java)
                    loopCallback(user)
                }
                afterCallback()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun getUserRecurrent(UID: String, mDbRef: DatabaseReference, loopCallback: (user: User?) -> Unit, afterCallback: () -> Unit){
        mDbRef.child("Users").orderByChild("userUID").equalTo(UID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children){
                    var user = i.getValue(User::class.java)
                    loopCallback(user)
                }
                afterCallback()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun clearSelectedUser(){
        selectedUser.value = null
    }

    fun getAllUsers(mDbRef: DatabaseReference, mAuth: FirebaseAuth, beforeCallback: ()-> Unit, afterCallback: () -> Unit = {}){
        mDbRef.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                beforeCallback()
                val users = arrayListOf<User>()
                for(item in snapshot.children){
                    val user = item.getValue(User::class.java)
                    if(mAuth.currentUser?.uid != user?.userUID){
                        users.add(user!!)
                    }
                }
                usersLiveData.value = users

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun sendFriendRequest(currentUserUid: String, user: User, mDbRef: DatabaseReference, successCallback: () -> Unit, failureCallback: ()-> Unit){
        if(currentUserUid == user.userUID.toString()){
            return
        }

        mDbRef
            .child("Users")
            .child(user.userUID.toString())
            .child("friendRequests")
            .child(currentUserUid)
            .setValue(currentUserUid)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                }else{
                    failureCallback()
                }
            }
    }

    fun revokeFriendRequest(currentUserId: String, user: User, mDbRef: DatabaseReference, successCallback: () -> Unit, failureCallback: () -> Unit){
        mDbRef
            .child("Users")
            .child(user.userUID.toString())
            .child("friendRequests")
            .child(currentUserId)
            .removeValue()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                } else {
                    failureCallback()
                }
            }
    }

    // TODO: Implement Accept Friend Request
    fun acceptFriendRequest(user: User, mDbRef: DatabaseReference, mAuth: FirebaseAuth, successCallback: () -> Unit = {}, failureCallback: () -> Unit = {}) {
        val otherUserRef = mDbRef.child("Users").child(user.userUID.toString())
        val currentUserRef = mDbRef.child("Users").child(mAuth.currentUser?.uid.toString())

        otherUserRef
            .child("friendRequests")
            .child(mAuth.currentUser?.uid.toString())
            .removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    currentUserRef
                        .child("friendRequests")
                        .child(user.userUID.toString())
                        .removeValue()
                        .addOnCompleteListener {
                            if(it.isSuccessful){
                                otherUserRef
                                    .child("friends")
                                    .child(mAuth.currentUser?.uid.toString())
                                    .setValue(mAuth.currentUser?.uid.toString())
                                    .addOnCompleteListener {
                                        if(it.isSuccessful){
                                            currentUserRef
                                                .child("friends")
                                                .child(user.userUID!!)
                                                .setValue(user.userUID!!)
                                                .addOnCompleteListener {
                                                    if (it.isSuccessful){
                                                        successCallback()
                                                    } else {
                                                        failureCallback()
                                                    }
                                                }
                                        } else {
                                            failureCallback()
                                        }
                                    }
                            } else {
                                failureCallback()
                            }
                        }

                } else {
                    failureCallback()
                }
            }

    }

    // TODO: Implement Reject Friend Request
    fun rejectFriendRequest(user: User){
        // remove from request list
    }

    fun unfriendUser(user: User){

    }

    // TODO: Implement get all friends
    fun getAllFriends(): List<User>{
        return listOf<User>()
    }

    // TODO: Implement Query database
    fun queryUsers(term: String): List<User>{
        return listOf<User>()
    }

    // TODO: Implement get user chatroom
    fun getUserChatRooms(): List<ChatRoom>{
        return listOf<ChatRoom>()
    }

    // TODO: Implement get single chat room
    fun getChatRoom(): ChatRoom{
        return ChatRoom()
    }

    // TODO: Implement delete chatroom
    fun deleteChatRoom(){
        // push to deleted
    }

    fun signOut(mAuth: FirebaseAuth){
        mAuth.signOut()
    }
}
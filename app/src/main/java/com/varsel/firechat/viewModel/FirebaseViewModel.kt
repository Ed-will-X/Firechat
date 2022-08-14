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
    val users = arrayListOf<User>()
    val selectedUser = MutableLiveData<User?>()

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

    // TODO: Create save User to Realtime database
    fun saveUser(
        name: String,
        email: String,
        UID: String,
        mDbRef: DatabaseReference,
        onSuccessCallback: ()-> Unit,
        onFailureCallback: ()-> Unit
    ){
        mDbRef.child("Users").child(UID).setValue(User(name, email, UID, null, null, null, false, null, null, null))
            .addOnCompleteListener {
                if(it.isSuccessful){
                    onSuccessCallback()
                } else {
                    onFailureCallback()
                }
            }
    }

    // TODO: Create single retrieve user from Realtime database
    fun getUserById(uid: String, mDbRef: DatabaseReference, beforeCallback: () -> Unit, afterCallback: ()-> Unit = {}): User {
        mDbRef.child("Users").orderByChild("userUID").equalTo(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                beforeCallback()

                for(item in snapshot.children){
                    Log.d("LLL", "$item")
                    val user = item.getValue(User::class.java)
                    selectedUser.value = user
                }

                afterCallback()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        return User()
    }

    fun clearSelectedUser(){
        selectedUser.value = null
    }


    // TODO: Implement get all unlocked users
    fun getAllUsers(mDbRef: DatabaseReference, mAuth: FirebaseAuth, beforeCallback: ()-> Unit){
        mDbRef.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                beforeCallback()
                for(item in snapshot.children){
                    val user = item.getValue(User::class.java)
                    // TODO: Add check to remove the current user
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


    // TODO: Implement Accept Friend Request
    fun acceptFriendRequest(user: User) {

    }

    // TODO: Implement send friend Request
    fun sendFriendRequest(){

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
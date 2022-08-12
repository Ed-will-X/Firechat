package com.varsel.firechat.viewModel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.varsel.firechat.model.Chat.ChatRoom
import com.varsel.firechat.model.User.User

class FirebaseViewModel: ViewModel() {
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
    fun getUserById(uid: String): User {
        return User()
    }

    // TODO: Implement get all unlocked users
    fun getAllUsers(): List<User> {
        return listOf<User>()
    }

    // TODO: Implement get all friends
    fun getAllFriends(user: User): List<User>{
        return listOf<User>()
    }

    // TODO: Implement get user chatroom
    fun getUserChatRooms(user: User): List<ChatRoom>{
        return listOf<ChatRoom>()
    }

    // TODO: Implement get single chat room
    fun getChatRoom(user: User): ChatRoom{
        return ChatRoom()
    }

    // TODO: Implement delete chatroom
    fun deleteChatRoom(){

    }

    fun signOut(mAuth: FirebaseAuth){
        mAuth.signOut()
    }
}
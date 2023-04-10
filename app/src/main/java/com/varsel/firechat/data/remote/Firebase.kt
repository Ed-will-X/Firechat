package com.varsel.firechat.data.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.varsel.firechat.common._utils.ImageUtils
import com.varsel.firechat.data.local.BugReport.BugReport
import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.Image.Image
import com.varsel.firechat.data.local.Message.Message
import com.varsel.firechat.data.local.Message.MessageType
import com.varsel.firechat.data.local.Message.SystemMessageType
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.PublicPost.PublicPost
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.common._utils.DebugUtils

class Firebase(
    val mDbRef: DatabaseReference,
    val mAuth: FirebaseAuth,
    val firebaseStorage: FirebaseStorage
) {
    val users_ref = mDbRef.child("Users")
    val chat_room_ref = mDbRef.child("chatRooms")
    val group_room_ref = mDbRef.child("groupRooms")

    fun signUp(
        email: String,
        password: String,
        callbackSuccess: ()-> Unit,
        callbackFail: ()-> Unit
    ){
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if(task.isSuccessful){
                callbackSuccess()
                DebugUtils.log_firebase("Signup successful")
            } else {
                callbackFail()
            }
        }
    }

    fun signin(
        email: String,
        password: String,
        callbackSuccess: ()-> Unit,
        callbackFail: () -> Unit
    ){
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if(it.isSuccessful){
                callbackSuccess()
                DebugUtils.log_firebase("Signin successful")
            } else {
                callbackFail()
            }
        }
    }

    fun saveUser(
        name: String,
        email: String,
        UID: String,
        onSuccessCallback: ()-> Unit,
        onFailureCallback: ()-> Unit
    ){
        mDbRef.child("Users").child(UID).setValue(User(name, email, UID))
            .addOnCompleteListener {
                if(it.isSuccessful){
                    DebugUtils.log_firebase("save user to db successful")

                    onSuccessCallback()
                } else {
                    onFailureCallback()
                }
            }
    }

    fun checkFirebaseConnection(callback: (Boolean)-> Unit){
        val connectedRef = Firebase.database.getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    callback(true)
                    DebugUtils.log_firebase("connection established")
                } else {
                    callback(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun getCurrentUserRecurrent(successCallback: (user: User?) -> Unit, beforeCallback: () -> Unit = {}, afterCallback: () -> Unit = {}) : ValueEventListener{

        val listener = users_ref.orderByChild("userUID").equalTo(mAuth.currentUser?.uid.toString()).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                beforeCallback()

                for (item in snapshot.children){
                    val user = item.getValue(User::class.java)
                    DebugUtils.log_firebase("fetch current user recurrent successful")

                    successCallback(user)
                }
                afterCallback()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        return listener
    }

    fun getCurrentUserSingle(successCallback: (user: User) -> Unit, beforeCallback: () -> Unit = {}, afterCallback: () -> Unit = {}, cancelCallback: () -> Unit = {}){
        mDbRef.child("Users").orderByChild("userUID").equalTo(mAuth?.currentUser?.uid.toString()).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                beforeCallback()

                for (item in snapshot.children){
                    val user = item.getValue(User::class.java)
                    DebugUtils.log_firebase("fetch current user single successful")

                    if(user != null){
                        successCallback(user)
                    }
                }
                afterCallback()
            }

            override fun onCancelled(error: DatabaseError) {
                cancelCallback()
            }

        })
    }

    // used to fetch a user that is being displayed on a separate page
    fun getUserById(uid: String, beforeCallback: () -> Unit, successCallback: (user: User) -> Unit, afterCallback: ()-> Unit = {}, cancelCallback: () -> Unit = {}) {
        mDbRef.child("Users").orderByChild("userUID").equalTo(uid).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                beforeCallback()

                for(item in snapshot.children){
                    val user = item.getValue(User::class.java)
                    DebugUtils.log_firebase("get user by id single successful")

                    if(user != null){
                        successCallback(user)
                    }
                }

                afterCallback()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }


    // used to fetch a user for a recycler view
    fun getUserSingle(UID: String, loopCallback: (user: User) -> Unit, afterCallback: () -> Unit = {}){
        mDbRef.child("Users").orderByChild("userUID").equalTo(UID).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children){
                    var user = i.getValue(User::class.java)
                    DebugUtils.log_firebase("get user single successful")

                    if(user != null) {
                        loopCallback(user)
                    }
                }
                afterCallback()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    // TODO: Fix potential concurrency bug in pertinence to cancellation
    fun getUserRecurrent(UID: String, loopCallback: (user: User) -> Unit, afterCallback: () -> Unit, cancelCallback: ()-> Unit = {}): ValueEventListener{
        val listener = users_ref.orderByChild("userUID").equalTo(UID).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children){
                    var user = i.getValue(User::class.java)
                    DebugUtils.log_firebase("get user recurrent successful")

                    if(user != null) {
                        loopCallback(user)
                    }
                }
                afterCallback()
            }

            override fun onCancelled(error: DatabaseError) {
                cancelCallback()
            }

        })

        return listener
    }

    fun clearSelectedUser(){
//        selectedUser.value = null
    }

    fun getAllUsers(beforeCallback: ()-> Unit, successCallback: (users: List<User>) -> Unit = {}){
        mDbRef.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                beforeCallback()
                val users = arrayListOf<User>()
                for(item in snapshot.children){
                    val user = item.getValue(User::class.java)
                    DebugUtils.log_firebase("get all users successful")
                    if(mAuth.currentUser?.uid != user?.userUID){
                        users.add(user!!)
                    }
                }
                successCallback(users)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun queryUsers(queryString: String, successCallback: (users: List<User>) -> Unit, beforeCallback: ()-> Unit = {}, afterCallback: () -> Unit = {}){
        mDbRef.child("Users").orderByChild("name").startAt(queryString).endAt(queryString+"\uf8ff").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                beforeCallback()
                val users = arrayListOf<User>()
                for(item in snapshot.children){
                    val user = item.getValue(User::class.java)
                    if(mAuth.currentUser?.uid != user?.userUID && queryString.isNotEmpty()){
                        users.add(user!!)
                    }
                    DebugUtils.log_firebase("query users successful")

                }
                successCallback(users)
                afterCallback()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun sendFriendRequest(user: User, successCallback: () -> Unit, failureCallback: ()-> Unit){
        val currentUserUid = mAuth.currentUser!!.uid

        if(currentUserUid == user.userUID.toString()){
            return
        }

        mDbRef
            .child("Users")
            .child(user.userUID.toString())
            .child("friendRequests")
            .child(currentUserUid)
            .setValue(System.currentTimeMillis())
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                    DebugUtils.log_firebase("send friend request successful")
                }else{
                    failureCallback()
                }
            }
    }

    fun revokeFriendRequest(user: User, successCallback: () -> Unit, failureCallback: () -> Unit){
        mDbRef
            .child("Users")
            .child(user.userUID.toString())
            .child("friendRequests")
            .child(mAuth.currentUser!!.uid)
            .removeValue()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                    DebugUtils.log_firebase("revoke friend request successful")
                } else {
                    failureCallback()
                }
            }
    }

    fun acceptFriendRequest(user: User, successCallback: () -> Unit = {}, failureCallback: () -> Unit = {}) {
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
                                    .setValue(System.currentTimeMillis())
                                    .addOnCompleteListener {
                                        if(it.isSuccessful){
                                            currentUserRef
                                                .child("friends")
                                                .child(user.userUID)
                                                .setValue(System.currentTimeMillis())
                                                .addOnCompleteListener {
                                                    if (it.isSuccessful){
                                                        successCallback()
                                                        DebugUtils.log_firebase("accept friend request successful")
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

    fun rejectFriendRequest(user: User, successCallback: () -> Unit = {}, failureCallback: () -> Unit = {}){
        val currentUserId = mAuth.currentUser!!.uid
        val currentUserRef = mDbRef.child("Users").child(currentUserId)

        currentUserRef
            .child("friendRequests")
            .child(user.userUID)
            .removeValue()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                } else {
                    failureCallback()
                }
            }
    }

    fun unfriendUser(user: User, successCallback: () -> Unit = {}, failureCallback: () -> Unit = {}){

        val currentUserId = mAuth.currentUser!!.uid
        val currentUserRef = mDbRef.child("Users").child(currentUserId)
        val otherUserRef = mDbRef.child("Users").child(user.userUID.toString())

        currentUserRef
            .child("friends")
            .child(user.userUID)
            .removeValue()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    otherUserRef
                        .child("friends")
                        .child(currentUserId)
                        .removeValue()
                        .addOnCompleteListener {
                            if(it.isSuccessful){
                                successCallback()
                            } else {
                                failureCallback()
                            }
                        }
                } else {
                    failureCallback()
                }
            }
    }

    fun sendMessage(
        message: Message,
        chatRoomUID: String,
        successCallback: () -> Unit,
        failureCallback: () -> Unit){

        // push the message to the chatroom
        mDbRef
            .child("chatRooms")
            .child(chatRoomUID)
            .child("messages")
            .child(message.messageUID)  // TODO: Not Tested
            .setValue(message)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                    DebugUtils.log_firebase("send message successful")
                } else {
                    failureCallback()
                }
            }
    }


    fun appendParticipants(chatRoom: ChatRoom, successCallback: () -> Unit, failureCallback: () -> Unit){
        mDbRef
            .child("chatRooms")
            .child(chatRoom.roomUID!!)
            .setValue(chatRoom)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                    DebugUtils.log_firebase("append participants successful")
                } else {
                    failureCallback()
                }
            }
    }

    fun appendChatRoom(uid: String, otherUser: String, successCallback: () -> Unit, failureCallback: () -> Unit){
        val currentTime = System.currentTimeMillis()
        mDbRef
            .child("Users")
            .child(otherUser)
            .child("chatRooms")
            .child(uid)
            .setValue(currentTime)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    mDbRef
                        .child("Users")
                        .child(mAuth.currentUser?.uid.toString())
                        .child("chatRooms")
                        .child(uid)
                        .setValue(currentTime)
                        .addOnCompleteListener {
                            if(it.isSuccessful){
                                successCallback()
                                DebugUtils.log_firebase("append chat room successful")
                            } else {
                                failureCallback()
                            }
                        }
                } else {
                    failureCallback()
                }
            }
    }

    fun getChatRoomRecurrent(chatRoomUID: String, loopCallback: (chatRoom: ChatRoom) -> Unit, afterCallback: () -> Unit): ValueEventListener{

        val listener = chat_room_ref
            .orderByChild("roomUID")
            .equalTo(chatRoomUID)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (i in snapshot.children){
                        val item = i.getValue(ChatRoom::class.java)
                        if(item != null) {
                            loopCallback(item)
                        }
                        DebugUtils.log_firebase("get chat room recurrent successful")
                    }

                    afterCallback()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        return listener
    }

    fun getChatRoomSingle(chatRoomUID: String, loopCallback: (chatRoom: ChatRoom?) -> Unit, afterCallback: () -> Unit){
        mDbRef
            .child("chatRooms")
            .orderByChild("roomUID")
            .equalTo(chatRoomUID)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (i in snapshot.children){
                        val item = i.getValue(ChatRoom::class.java)
                        loopCallback(item)
                        DebugUtils.log_firebase("get chat room recurrent successful")
                    }

                    afterCallback()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    fun createGroup(groupObj: GroupRoom, successCallback: () -> Unit, failureCallback: () -> Unit){
        val groupRef = mDbRef.child("groupRooms")

        groupRef
            .child(groupObj.roomUID)
            .setValue(groupObj)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    groupCreateMessage(groupObj.roomUID, {
                        successCallback()
                        DebugUtils.log_firebase("create group successful")
                    },{
                        failureCallback()
                    })
                } else {
                    failureCallback()
                }
            }
    }

    fun appendGroupRoomsToUser(
        roomUid: String,
        otherUserID: String,
        successCallback: () -> Unit,
        failureCallback: () -> Unit){

        val currentUserRef = mDbRef.child("Users").child(mAuth.currentUser?.uid.toString())
        val otherUserRef = mDbRef.child("Users").child(otherUserID)
        val currentTime = System.currentTimeMillis()

        currentUserRef
            .child("groupRooms")
            .child(roomUid)
            .setValue(currentTime)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    otherUserRef
                        .child("groupRooms")
                        .child(roomUid)
                        .setValue(currentTime)
                        .addOnCompleteListener {
                            if(it.isSuccessful){
                                successCallback()
                                DebugUtils.log_firebase("append group to user successful")
                            } else {
                                failureCallback()
                            }
                        }
                } else {
                    failureCallback()
                }
            }
    }

    fun getGroupChatRoomSingle(chatRoomUID: String, loopCallback: (chatRoom: GroupRoom?) -> Unit, afterCallback: () -> Unit){
        mDbRef
            .child("groupRooms")
            .orderByChild("roomUID")
            .equalTo(chatRoomUID)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (i in snapshot.children){
                        val item = i.getValue(GroupRoom::class.java)
                        loopCallback(item)
                        DebugUtils.log_firebase("get group room single successful")

                    }

                    afterCallback()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    fun getGroupChatRoomRecurrent(chatRoomUID: String, loopCallback: (groupRoom: GroupRoom) -> Unit, afterCallback: () -> Unit): ValueEventListener{
        val listener = group_room_ref
            .orderByChild("roomUID")
            .equalTo(chatRoomUID)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (i in snapshot.children){
                        val item = i.getValue(GroupRoom::class.java)
                        if(item != null) {
                            loopCallback(item)
                        }
                        DebugUtils.log_firebase("get group room recurrent successful")
                    }

                    afterCallback()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        return listener
    }

    // TODO: Modify to accommodate simultaneous first message possibilities
    fun sendGroupMessage(
        message: Message,
        chatRoomID: String,
        successCallback: () -> Unit,
        failureCallback: () -> Unit){

        // push the message to the chatroom
        mDbRef
            .child("groupRooms")
            .child(chatRoomID)
            .child("messages")
            .child(message.messageUID)
            .setValue(message)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                    DebugUtils.log_firebase("send group message successful")
                } else {
                    failureCallback()
                }
            }
    }

    fun makeAdmin(userId: String, roomId: String, successCallback: () -> Unit, failureCallback: () -> Unit){
        val reference = mDbRef.child("groupRooms").child(roomId)

        reference
            .child("admins")
            .child(userId)
            .setValue(userId)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    groupNowAdminMessage(userId, roomId, {
                        successCallback()
                        DebugUtils.log_firebase("make admin successful")
                    },{
                        failureCallback()
                    })
                } else {
                    failureCallback()
                }
            }
    }


    fun removeAdmin(userId: String, selectedGroupRoom: GroupRoom, successCallback: () -> Unit, failureCallback: () -> Unit){
        val reference = mDbRef.child("groupRooms").child(selectedGroupRoom.roomUID)
        val admins = selectedGroupRoom.admins
        val currentUserId = mAuth.currentUser!!.uid

        if(admins!!.size < 2 && userId == currentUserId){
            failureCallback()
        } else {
            reference
                .child("admins")
                .child(userId)
                .removeValue()
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        groupNotAdminMessage(userId, selectedGroupRoom.roomUID, {
                            successCallback()
                            DebugUtils.log_firebase("remove admin successful")
                        }, {
                            failureCallback()
                        })
                    } else {
                        failureCallback()
                    }
                }
        }
    }

    fun removeFromGroup(userId: String, groupId: String, successCallback: () -> Unit, failureCallback: () -> Unit){
        val groupRef = mDbRef.child("groupRooms").child(groupId)
        val userRef = mDbRef.child("Users").child(userId)

        groupRef
            .child("participants")
            .child(userId)
            .removeValue()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    groupRemoveMessage(userId, groupId, {
                        // TODO: Remove from user object
                        userRef
                            .child("groupRooms")
                            .child(groupId)
                            .removeValue()
                            .addOnCompleteListener {
                                if(it.isSuccessful){
                                    successCallback()
                                    DebugUtils.log_firebase("remove from group successful")
                                } else {
                                    failureCallback()
                                }
                            }
                    }, {
                        failureCallback()
                    })
                } else {
                    failureCallback()
                }
            }
    }

    fun addGroupMembers(users: List<String>, groupId: String, successCallback: (userId: String) -> Unit, failureCallback: (userId: String) -> Unit, afterCallback: () -> Unit){
        val groupReference = mDbRef.child("groupRooms").child(groupId)
        val currentTime = System.currentTimeMillis()
        groupAddMessage(users, groupId, {
            for((i, v) in users.withIndex()){
                groupReference
                    .child("participants")
                    .child(v)
                    .setValue(v)
                    .addOnCompleteListener {
                        if(it.isSuccessful){

                            // append group room to user
                            mDbRef
                                .child("Users")
                                .child(v)
                                .child("groupRooms")
                                .child(groupId)
                                .setValue(currentTime)
                                .addOnCompleteListener {
                                    if(it.isSuccessful){
                                        successCallback(v)
                                        DebugUtils.log_firebase("add group members successful")

                                    } else {
                                        failureCallback(v)
                                    }
                                }
                        } else {
                            failureCallback(v)
                        }
                    }

                if(i == users.size -1){
                    afterCallback()
                }
            }
        }, {
        })
        // append user IDs to group object

    }

    fun leaveGroup(selectedGroupRoom: GroupRoom, successCallback: () -> Unit, failureCallback: () -> Unit){
        val groupReference = mDbRef.child("groupRooms").child(selectedGroupRoom.roomUID)
        val userReference = mDbRef.child("Users").child(mAuth.currentUser!!.uid)
        val currentUserId: String = mAuth.currentUser!!.uid
        val admins = selectedGroupRoom.admins

        if(admins!!.containsValue(currentUserId) && admins.size < 2){
            failureCallback()
        } else {
            // removes from admin list
            if(admins.containsValue(currentUserId)){
                groupReference
                    .child("admins")
                    .child(mAuth.currentUser!!.uid)
                    .removeValue()
                    .addOnCompleteListener {
                        if(it.isSuccessful){

                        } else {
                            failureCallback()
                            return@addOnCompleteListener
                        }
                    }
            }

            // sends exit message
            groupExitMessage(selectedGroupRoom.roomUID, {
                // Removes from participant list
                groupReference
                    .child("participants")
                    .child(mAuth.currentUser!!.uid)
                    .removeValue()
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            // Removes from user object
                            userReference
                                .child("groupRooms")
                                .child(selectedGroupRoom.roomUID)
                                .removeValue()
                                .addOnCompleteListener {
                                    if(it.isSuccessful){
                                        successCallback()
                                        DebugUtils.log_firebase("leave group successful")
                                    } else {
                                        failureCallback()
                                    }
                                }
                        } else {
                            failureCallback()
                        }
                    }
            }, {
                failureCallback()
            })
        }
    }

    fun groupJoinMessage(){

    }

    fun groupAddMessage(users: List<String>, roomId: String, successCallback: () -> Unit, failureCallback: () -> Unit){
        val usersInString = users.joinToString(
            separator = " ",
        )
        val message = Message(SystemMessageType.GROUP_ADD, "${mAuth.currentUser!!.uid} ${usersInString}", System.currentTimeMillis(), "SYSTEM", MessageType.TEXT)
        val databaseReference = mDbRef.child("groupRooms")

        databaseReference
            .child(roomId)
            .child("messages")
            .push()
            .setValue(message)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                    DebugUtils.log_firebase("group add message successful")
                } else {
                    failureCallback()
                }
            }
    }

    fun groupExitMessage(roomId: String, successCallback: () -> Unit, failureCallback: () -> Unit){
        val message = Message(SystemMessageType.GROUP_EXIT, mAuth.currentUser!!.uid, System.currentTimeMillis(), "SYSTEM", MessageType.TEXT)
        val databaseReference = mDbRef.child("groupRooms")

        databaseReference
            .child(roomId)
            .child("messages")
            .push()
            .setValue(message)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                    DebugUtils.log_firebase("group exit message successful")
                } else {
                    failureCallback()
                }
            }
    }

    fun groupRemoveMessage(userId: String, roomId: String, successCallback: () -> Unit, failureCallback: () -> Unit){
        val message = Message(SystemMessageType.GROUP_REMOVE, "${mAuth.currentUser!!.uid} ${userId}", System.currentTimeMillis(), "SYSTEM", MessageType.TEXT)
        val databaseReference = mDbRef.child("groupRooms")

        databaseReference
            .child(roomId)
            .child("messages")
            .push()
            .setValue(message)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                    DebugUtils.log_firebase("group remove message successful")
                } else {
                    failureCallback()
                }
            }
    }

    fun groupNowAdminMessage(userId: String, roomId: String, successCallback: () -> Unit, failureCallback: () -> Unit){
        val message = Message(SystemMessageType.NOW_ADMIN, userId, System.currentTimeMillis(), "SYSTEM", MessageType.TEXT)
        val databaseReference = mDbRef.child("groupRooms")

        databaseReference
            .child(roomId)
            .child("messages")
            .push()
            .setValue(message)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                    DebugUtils.log_firebase("group now admin message successful")
                } else {
                    failureCallback()
                }
            }
    }

    fun groupNotAdminMessage(userId: String, roomId: String, successCallback: () -> Unit, failureCallback: () -> Unit){
        val message = Message(SystemMessageType.NOT_ADMIN, userId, System.currentTimeMillis(), "SYSTEM", MessageType.TEXT)
        val databaseReference = mDbRef.child("groupRooms")

        databaseReference
            .child(roomId)
            .child("messages")
            .push()
            .setValue(message)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                    DebugUtils.log_firebase("group not admin message successful")
                } else {
                    failureCallback()
                }
            }
    }

    fun groupCreateMessage(roomId: String, successCallback: () -> Unit, failureCallback: () -> Unit){
        val message = Message(SystemMessageType.GROUP_CREATE, mAuth.currentUser!!.uid, System.currentTimeMillis(), "SYSTEM", MessageType.TEXT)
        val databaseReference = mDbRef.child("groupRooms")

        databaseReference
            .child(roomId)
            .child("messages")
            .push()
            .setValue(message)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                    DebugUtils.log_firebase("group create message successful")
                } else {
                    failureCallback()
                }
            }
    }

    fun groupDismantleMessage(){

    }

    fun editUser(key: String, value: String, successCallback: () -> Unit, failureCallback: () -> Unit){
        val databaseRef = mDbRef.child("Users").child(mAuth.currentUser?.uid.toString())

        if(value.isEmpty()){
            databaseRef.child(key).setValue(null).addOnCompleteListener {
                if(it.isSuccessful) {
                    DebugUtils.log_firebase("edit user ${key} successful")
                    successCallback()
                } else {
                    DebugUtils.log_firebase("edit user ${key} fail")
                    failureCallback()
                }
            }
        } else {
            databaseRef.child(key).setValue(value).addOnCompleteListener {
                if(it.isSuccessful){
                    DebugUtils.log_firebase("edit user ${key} successful")
                    successCallback()
                } else {
                    DebugUtils.log_firebase("edit user ${key} fail")
                    failureCallback()
                }
            }
        }
    }

    fun editGroup(key: String, value: String, groupId: String, successCallback: () -> Unit, failureCallback: () -> Unit){
        val databaseRef = mDbRef.child("groupRooms").child(groupId)

        if(value.isEmpty()){
            databaseRef.child(key).setValue(null).addOnCompleteListener {
                if(it.isSuccessful) {
                    successCallback()
                    DebugUtils.log_firebase("edit group ${key} successful")
                } else {
                    failureCallback()
                }
            }
        } else {
            databaseRef.child(key).setValue(value).addOnCompleteListener {
                if(it.isSuccessful) {
                    successCallback()
                    DebugUtils.log_firebase("edit group ${key} successful")
                } else {
                    failureCallback()
                }
            }
        }
    }

    fun addGroupToFavorites(groupId: String, successCallback: () -> Unit, failureCallback: () -> Unit){
        val currentUserId = mAuth.currentUser!!.uid
        val userReference = mDbRef.child("Users").child(currentUserId)

        userReference
            .child("favoriteGroups")
            .child(groupId)
            .setValue(System.currentTimeMillis())
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                    DebugUtils.log_firebase("Add group to favorites successful")
                } else {
                    failureCallback()
                }
            }
    }

    fun removeGroupFromFavorites(groupId: String, successCallback: () -> Unit, failureCallback: () -> Unit) {
        val currentUserId = mAuth.currentUser!!.uid
        val userReference = mDbRef.child("Users").child(currentUserId)

        userReference
            .child("favoriteGroups")
            .child(groupId)
            .removeValue()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                    DebugUtils.log_firebase("remove group from favorites successful")
                } else {
                    failureCallback()
                }
            }
    }

    // TODO: Implement delete chatroom
    fun deleteChatRoom(){
        // push to deleted
    }

    // TODO: Give option to clear user data before logout
    fun signOut(){
        // delete both user and image from the local db
        mAuth.signOut()
        DebugUtils.log_firebase("sign out called")
    }

    fun uploadProfileImage(profileImage: ProfileImage, base64: String, successCallback: ()-> Unit, failureCallback: ()-> Unit){
        val reference = mDbRef.child("ProfileImages").child(mAuth.currentUser!!.uid)
        val decoded = ImageUtils.base64ToByteArray(base64)

        firebaseStorage.getReference("/profileImages/${profileImage.ownerId}").putBytes(decoded).addOnCompleteListener {
            if(it.isSuccessful){
                reference
                    .setValue(profileImage)
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            successCallback()
                            DebugUtils.log_firebase("upload profile image successful")
                        } else {
                            failureCallback()
                        }
                    }
            } else {
                failureCallback()
            }
        }
    }

    fun uploadGroupImage(roomId: String, profileImage: ProfileImage, base64: String, successCallback: ()-> Unit, failureCallback: ()-> Unit){
        val reference = mDbRef.child("ProfileImages").child(roomId)
        val decoded = ImageUtils.base64ToByteArray(base64)

        firebaseStorage.getReference("/profileImages/${profileImage.ownerId}").putBytes(decoded).addOnCompleteListener {
            if(it.isSuccessful){
                reference
                    .setValue(profileImage)
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            successCallback()
                            DebugUtils.log_firebase("upload profile image successful")
                        } else {
                            failureCallback()
                        }
                    }
            } else {
                failureCallback()
            }
        }
    }

    fun removeProfileImage(ID: String, successCallback: ()-> Unit, failureCallback: ()-> Unit){
        val reference = mDbRef.child("ProfileImages").child(ID)

        firebaseStorage.getReference("/profileImages/${ID}").delete().addOnCompleteListener {
            if(it.isSuccessful){
                reference
                    .setValue(null)
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            successCallback()
                            DebugUtils.log_firebase("remove profile image successful")
                        } else {
                            failureCallback()
                        }
                    }
            } else {
                failureCallback()
            }
        }
    }

    // Added snapshot existence callback because the data usage was high even when image was null in DB
    fun getProfileImage(userId: String, loopCallback: (profileImage: ProfileImage) -> Unit, afterCallback: () -> Unit, snapshotExistenceCallback: (bool: Boolean)-> Unit){
        val storageRef = firebaseStorage.reference.child("/profileImages/${userId}")
        storageRef.getBytes(2_000_000).addOnCompleteListener {

            if(it.isSuccessful){
                mDbRef.child("ProfileImages").orderByChild("ownerId").equalTo(userId).addListenerForSingleValueEvent(object:
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            Log.d("SNAPSHOT_IMG", "profile image exists for ${userId}")

                            for(item in snapshot.children){
                                val profileImage = item.getValue(ProfileImage::class.java)
                                if(profileImage != null){
                                    val encoded = ImageUtils.byteArraytoBase64(it.result)
                                    val profileImage_withBase64 = ProfileImage(profileImage, encoded)
                                    loopCallback(profileImage_withBase64)

                                    DebugUtils.log_firebase("get profile image successful")
                                }
                            }
                            afterCallback()

                            snapshotExistenceCallback(true)
                        } else {
                            Log.d("SNAPSHOT_IMG", "profile image does not exist for ${userId}")
                            snapshotExistenceCallback(false)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })

            } else {

            }
        }

    }

    fun appendProfileImageTimestamp(timestamp: Long, successCallback: () -> Unit, failureCallback: () -> Unit){
        val currentuser = mAuth.currentUser!!.uid
        val databaseReference = mDbRef.child("Users").child(currentuser)

        databaseReference
            .child("imgChangeTimestamp")
            .setValue(timestamp)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                    DebugUtils.log_firebase("Append profile image timestamp successful")
                } else {
                    failureCallback()
                }
            }
    }

    fun appendGroupImageTimestamp(groupId: String, timestamp: Long, successCallback: () -> Unit, failureCallback: () -> Unit){
        val databaseReference = mDbRef.child("groupRooms").child(groupId)

        databaseReference
            .child("imgChangeTimestamp")
            .setValue(timestamp)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                    DebugUtils.log_firebase("append group image timestamp successful")
                } else {
                    failureCallback()
                }
            }
    }

    fun uploadChatImage(image: Image, chatRoomID: String, base64: String, successCallback: (image: Image)-> Unit, failureCallback: ()-> Unit){
        val reference = mDbRef.child("chatImages")

        val decoded = ImageUtils.base64ToByteArray(base64)
        firebaseStorage.getReference("/chatImages/${chatRoomID}/${image.imageId}").putBytes(decoded).addOnCompleteListener {
            if(it.isSuccessful){
                reference
                    .child(chatRoomID)
                    .push()
                    .setValue(image)
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            val image_with_base64 = Image(image, base64)
                            successCallback(image_with_base64)
                            DebugUtils.log_firebase("upload chat image successful")
                        } else {
                            failureCallback()
                        }
                    }
            } else {
                failureCallback()
            }
        }
    }

    fun getChatImage(imageId: String, chatRoomID: String, loopCallback: (image: Image) -> Unit, afterCallback: () -> Unit){
        val storageRef = firebaseStorage.reference.child("/chatImages/${chatRoomID}/${imageId}")
        storageRef.getBytes(2000000).addOnCompleteListener {
            if(it.isSuccessful){
                mDbRef.child("chatImages").child(chatRoomID).orderByChild("imageId").equalTo(imageId).addListenerForSingleValueEvent(object:
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(item in snapshot.children){

                            val image = item.getValue(Image::class.java)
                            val encoded = ImageUtils.byteArraytoBase64(it.result)
                            if(image != null){
                                val image_withBase64 = Image(image, encoded)
                                loopCallback(image_withBase64)
                                DebugUtils.log_firebase("get chat image successful")
                            }
                        }
                        afterCallback()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            } else {

            }
        }
    }

    fun uploadPublicPost(publicPost: PublicPost, base64: String, successCallback: (publicPost: PublicPost)-> Unit, failureCallback: ()-> Unit){
        val reference = mDbRef.child("public_posts")
        val decoded = ImageUtils.base64ToByteArray(base64)

        firebaseStorage.getReference("/publicPosts/${publicPost.postId}").putBytes(decoded).addOnCompleteListener {
            if(it.isSuccessful){
                reference
                    .child(publicPost.postId)
                    .setValue(publicPost)
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            val publicPost_withBase64 = PublicPost(publicPost, base64)
                            successCallback(publicPost_withBase64)
                            DebugUtils.log_firebase("upload public post successful")
                        } else {
                            failureCallback()
                        }
                    }
            } else {
                failureCallback()
            }
        }
    }

    // TODO: Not tested
    fun removePublicPost(publicPost: PublicPost, successCallback: ()-> Unit = {}, failureCallback: ()-> Unit = {}){
        val publicPostReference = mDbRef.child("public_posts")
        val currentUserId = mAuth.currentUser!!.uid
        val currentUserReference = mDbRef.child("Users")

        firebaseStorage.getReference("/publicPosts/${publicPost.postId}").delete().addOnCompleteListener {
            if(it.isSuccessful){
                publicPostReference
                    .child(publicPost.postId)
                    .removeValue()
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            currentUserReference
                                .child(currentUserId)
                                .child("public_posts")
                                .child(publicPost.postId)
                                .removeValue()
                                .addOnCompleteListener {
                                    if(it.isSuccessful){
                                        successCallback()
                                        DebugUtils.log_firebase("remove public post successful")
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

    fun getPublicPost(postId: String, loopCallback: (publicPost: PublicPost) -> Unit, afterCallback: () -> Unit){
        val storageRef = firebaseStorage.reference.child("/publicPosts/${postId}")
        storageRef.getBytes(2000000).addOnCompleteListener {
            if(it.isSuccessful){
                mDbRef.child("public_posts").orderByChild("postId").equalTo(postId).addListenerForSingleValueEvent(object:
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(item in snapshot.children){

                            val post = item.getValue(PublicPost::class.java)
                            val encoded = ImageUtils.byteArraytoBase64(it.result)
                            if(post != null){
                                val post_withImage = PublicPost(post, encoded)
                                loopCallback(post_withImage)
                                DebugUtils.log_firebase("get public post successful")
                            }
                        }
                        afterCallback()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            } else {

            }
        }
    }

    fun appendPublicPostIdToUser(postId: String, successCallback: () -> Unit, failureCallback: () -> Unit){
        val databaseReference = mDbRef.child("Users").child(mAuth.currentUser!!.uid)

        databaseReference
            .child("public_posts")
            .child(postId)
            .setValue(System.currentTimeMillis())
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                    DebugUtils.log_firebase("append public post id to user successful")
                } else {
                    failureCallback()
                }
            }
    }

    // TODO: Not Tested
    fun deleteRecentSearchHistory(successCallback: () -> Unit = {}, failureCallback: () -> Unit = {}){
        mDbRef
            .child("Users")
            .child(mAuth.currentUser!!.uid)
            .child("recent_search")
            .setValue(null)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                    DebugUtils.log_firebase("Delete recent search history successful")
                }else{
                    failureCallback()
                    DebugUtils.log_firebase("DELETE RECENT SEARCH HISTORY UNSUCCESSFUL")
                }
            }
    }

    fun addToRecentSearch(userId: String, successCallback: () -> Unit = {}, failureCallback: () -> Unit = {}){
        mDbRef
            .child("Users")
            .child(mAuth.currentUser!!.uid)
            .child("recent_search")
            .child(userId)
            .setValue(System.currentTimeMillis())
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                    DebugUtils.log_firebase("Add to recent search successful")
                }else{
                    failureCallback()
                    DebugUtils.log_firebase("ADD TO RECENT SEARCH UNSUCCESSFUL")
                }
            }
    }

    // TODO: Not tested
    fun uploadBugReport(bugReportEntity: BugReport, successCallback: () -> Unit = {}, failureCallback: () -> Unit = {}){
        val databaseReference = mDbRef.child("bug_reports").child(mAuth.currentUser!!.uid)
        databaseReference.child(bugReportEntity.reportId).setValue(bugReportEntity)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    DebugUtils.log_firebase("upload bug report successful")

                    successCallback()
                } else {
                    failureCallback()
                }
            }
    }

    fun uploadReadReceipt_ChatRoom(
        messageId: String,
        chatRoomUID: String,
        successCallback: () -> Unit,
        failureCallback: () -> Unit
    ){
        Log.d("RECEIPT", "Upload ran")

        // push the message to the chatroom
        mDbRef
            .child("chatRooms")
            .child(chatRoomUID)
            .child("messages")
            .child(messageId)
            .child("readBy")
            .child(mAuth.currentUser!!.uid)
            .setValue(System.currentTimeMillis())
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                    DebugUtils.log_firebase("send message successful")
                } else {
                    failureCallback()
                }
            }
    }

    fun uploadReadReceipt_GroupRoom(
        messageId: String,
        groupRoomId: String,
        successCallback: () -> Unit,
        failureCallback: () -> Unit
    ){
        Log.d("RECEIPT", "Upload ran")

        // push the message to the chatroom
        mDbRef
            .child("groupRooms")
            .child(groupRoomId)
            .child("messages")
            .child(messageId)
            .child("readBy")
            .child(mAuth.currentUser!!.uid)
            .setValue(System.currentTimeMillis())
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                    DebugUtils.log_firebase("send message successful")
                } else {
                    failureCallback()
                }
            }
    }

    fun deleteMessageForAll_ChatRoom(
        message: Message,
        chatRoomUID: String,
        successCallback: () -> Unit,
        failureCallback: () -> Unit
    ){
        if(mAuth.currentUser!!.uid != message.sender) {
            failureCallback()
            return
        }
        // push the message to the chatroom
        mDbRef
            .child("chatRooms")
            .child(chatRoomUID)
            .child("messages")
            .child(message.messageUID)
            .child("deletedBySender")
            .setValue(true)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                    DebugUtils.log_firebase("delete message successful")
                } else {
                    failureCallback()
                }
            }
    }

    fun deleteMessageForAll_GroupRoom(
        message: Message,
        groupRoomId: String,
        successCallback: () -> Unit,
        failureCallback: () -> Unit
    ){
        if(mAuth.currentUser!!.uid != message.sender) {
            failureCallback()
            return
        }
        // push the message to the chatroom
        mDbRef
            .child("groupRooms")
            .child(groupRoomId)
            .child("messages")
            .child(message.messageUID)
            .child("deletedBySender")
            .setValue(true)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                    DebugUtils.log_firebase("delete message successful")
                } else {
                    failureCallback()
                }
            }
    }

    fun deleteMessage_chatRoom(message: Message, chatRoomId: String, successCallback: () -> Unit, failureCallback: ()-> Unit) {
        mDbRef
            .child("chatRooms")
            .child(chatRoomId)
            .child("messages")
            .child(message.messageUID)
            .child("deletedBy")
            .child(mAuth.currentUser!!.uid)
            .setValue(System.currentTimeMillis())
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                    DebugUtils.log_firebase("delete message successful")
                } else {
                    failureCallback()
                }
            }
    }

    fun deleteMessage_groupRoom(message: Message, groupRoomId: String, successCallback: () -> Unit, failureCallback: ()-> Unit) {
        mDbRef
            .child("groupRooms")
            .child(groupRoomId)
            .child("messages")
            .child(message.messageUID)
            .child("deletedBy")
            .child(mAuth.currentUser!!.uid)
            .setValue(System.currentTimeMillis())
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
                    DebugUtils.log_firebase("delete message successful")
                } else {
                    failureCallback()
                }
            }
    }

    fun updateLastOnline(successCallback: () -> Unit, failureCallback: () -> Unit) {
        val databaseRef = mDbRef.child("Users").child(mAuth.currentUser?.uid.toString())

        databaseRef
            .child("lastOnline")
            .setValue(System.currentTimeMillis()).addOnCompleteListener {
            if(it.isSuccessful) {
                successCallback()
            } else {
                failureCallback()
            }
        }
    }

    fun getOtherUserLastOnlineRecurrent (
        userId: String,
        successCallback: (stamp: Long?) -> Unit,
        beforeCallback: () -> Unit = {},
        afterCallback: () -> Unit = {}
    ) : ValueEventListener {

        val listener = users_ref
            .child("Users")
            .child(userId)
            .child("lastOnline")
            .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                beforeCallback()

                for (item in snapshot.children){
                    val stamp = item.getValue(Long::class.java)
                    Log.d("LLL", "Last Online fb success: ${stamp}")

                    DebugUtils.log_firebase("fetch current user recurrent successful")

                    successCallback(stamp)
                }
                afterCallback()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        return listener
    }

    // TODO: Implement delete account
    fun deleteAccount(){
        // delete from auth db

        // delete from realtime db

        // also delete user images
    }
}

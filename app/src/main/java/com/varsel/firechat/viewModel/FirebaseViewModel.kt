package com.varsel.firechat.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.varsel.firechat.model.BugReport.BugReport
import com.varsel.firechat.model.Chat.ChatRoom
import com.varsel.firechat.model.Chat.GroupRoom
import com.varsel.firechat.model.Image.Image
import com.varsel.firechat.model.ProfileImage.ProfileImage
import com.varsel.firechat.model.User.User
import com.varsel.firechat.model.Message.Message
import com.varsel.firechat.model.Message.MessageType
import com.varsel.firechat.model.Message.SystemMessageType
import com.varsel.firechat.model.PublicPost.PublicPost
import com.varsel.firechat.utils.DebugUtils
import com.varsel.firechat.utils.ImageUtils

class FirebaseViewModel: ViewModel() {
    val usersLiveData = MutableLiveData<List<User>>()
    val selectedUser = MutableLiveData<User>()
    var selectedChatRoomUser = MutableLiveData<User>()
    val currentUser = MutableLiveData<User>()
    val currentUserSingle = MutableLiveData<User>()
    val friendRequests = MutableLiveData<List<User>>()
    private var _friends = MutableLiveData<List<User>>()
    val friends get() = _friends
    var chatRooms = MutableLiveData<MutableList<ChatRoom>>()
    var selectedChatRoom = MutableLiveData<ChatRoom>()
    val groupRooms = MutableLiveData<MutableList<GroupRoom>>()
    val selectedGroupRoom = MutableLiveData<GroupRoom>()
    val usersQuery = MutableLiveData<List<User>>()
    val selectedGroupParticipants = MutableLiveData<List<User>>()
    val selectedGroup_nonParticipants = MutableLiveData<List<User>>()
    val isConnectedToDatabase = MutableLiveData<Boolean>(false)

    fun setFriends(friends: List<User>){
        this._friends.value = friends
    }

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
                DebugUtils.log_firebase("Signup successful")
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
        mDbRef: DatabaseReference,
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

    fun getCurrentUserRecurrent(mAuth: FirebaseAuth?, mDbRef: DatabaseReference, beforeCallback: () -> Unit, afterCallback: () -> Unit){
        mDbRef.child("Users").orderByChild("userUID").equalTo(mAuth?.currentUser?.uid.toString()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                beforeCallback()

                for (item in snapshot.children){
                    val user = item.getValue(User::class.java)
                    DebugUtils.log_firebase("fetch current user recurrent successful")

                    if(user != null){
                        currentUser.value = user as User
                    }
                }
                afterCallback()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun getCurrentUserSingle(mAuth: FirebaseAuth?, mDbRef: DatabaseReference, beforeCallback: () -> Unit, afterCallback: () -> Unit){
        mDbRef.child("Users").orderByChild("userUID").equalTo(mAuth?.currentUser?.uid.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                beforeCallback()

                for (item in snapshot.children){
                    val user = item.getValue(User::class.java)
                    DebugUtils.log_firebase("fetch current user single successful")

                    if(user != null){
                        currentUserSingle.value = user as User
                    }
                }
                afterCallback()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    // used to fetch a user that is being displayed on a separate page
    fun getUserById(uid: String, mDbRef: DatabaseReference, beforeCallback: () -> Unit, afterCallback: ()-> Unit = {}) {
        mDbRef.child("Users").orderByChild("userUID").equalTo(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                beforeCallback()

                for(item in snapshot.children){
                    val user = item.getValue(User::class.java)
                    DebugUtils.log_firebase("get user by id single successful")

                    if(user != null){
                        selectedUser.value = user as User
                    }
                }

                afterCallback()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }


    // used to fetch a user for a recycler view
    fun getUserSingle(UID: String, mDbRef: DatabaseReference, loopCallback: (user: User?) -> Unit, afterCallback: () -> Unit){
        mDbRef.child("Users").orderByChild("userUID").equalTo(UID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children){
                    var user = i.getValue(User::class.java)
                    DebugUtils.log_firebase("get user single successful")

                    loopCallback(user)
                }
                afterCallback()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun getUserRecurrent(UID: String, mDbRef: DatabaseReference, loopCallback: (user: User?) -> Unit, afterCallback: () -> Unit){
        mDbRef.child("Users").orderByChild("userUID").equalTo(UID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children){
                    var user = i.getValue(User::class.java)
                    DebugUtils.log_firebase("get user recurrent successful")

                    loopCallback(user)
                }
                afterCallback()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun clearSelectedUser(){
//        selectedUser.value = null
    }

    fun getAllUsers(mDbRef: DatabaseReference, mAuth: FirebaseAuth, beforeCallback: ()-> Unit, afterCallback: () -> Unit = {}){
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
                usersLiveData.value = users
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun queryUsers(queryString: String, mDbRef: DatabaseReference, mAuth: FirebaseAuth, beforeCallback: ()-> Unit, afterCallback: () -> Unit = {}){
        mDbRef.child("Users").orderByChild("name").startAt(queryString).endAt(queryString+"\uf8ff").addListenerForSingleValueEvent(object : ValueEventListener {
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
                usersQuery.value = users
                afterCallback()
            }

            override fun onCancelled(error: DatabaseError) {

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
                    DebugUtils.log_firebase("revoke friend request successful")
                } else {
                    failureCallback()
                }
            }
    }

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

    fun rejectFriendRequest(user: User, mDbRef: DatabaseReference, mAuth: FirebaseAuth, successCallback: () -> Unit = {}, failureCallback: () -> Unit = {}){
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

    fun unfriendUser(user: User, mAuth: FirebaseAuth, mDbRef: DatabaseReference, successCallback: () -> Unit = {}, failureCallback: () -> Unit = {}){

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
        mDbRef: DatabaseReference,
        successCallback: () -> Unit,
        failureCallback: () -> Unit){

        // push the message to the chatroom
        mDbRef
            .child("chatRooms")
            .child(chatRoomUID)
            .child("messages")
            .push()
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


    fun appendParticipants(chatRoom: ChatRoom, mDbRef: DatabaseReference, successCallback: () -> Unit, failureCallback: () -> Unit){
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

    fun appendChatRoom(uid: String, otherUser: String, mAuth: FirebaseAuth, mDbRef: DatabaseReference, successCallback: () -> Unit, failureCallback: () -> Unit){
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

    fun getChatRoomRecurrent(chatRoomUID: String, mDbRef: DatabaseReference, loopCallback: (chatRoom: ChatRoom?) -> Unit, afterCallback: () -> Unit){
        mDbRef
            .child("chatRooms")
            .orderByChild("roomUID")
            .equalTo(chatRoomUID)
            .addValueEventListener(object : ValueEventListener {
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

    fun getChatRoomSingle(chatRoomUID: String, mDbRef: DatabaseReference, loopCallback: (chatRoom: ChatRoom?) -> Unit, afterCallback: () -> Unit){
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

    fun createGroup(groupObj: GroupRoom, mDbRef: DatabaseReference, mAuth: FirebaseAuth, successCallback: () -> Unit, failureCallback: () -> Unit){
        val groupRef = mDbRef.child("groupRooms")

        groupRef
            .child(groupObj.roomUID)
            .setValue(groupObj)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    groupCreateMessage(mAuth.currentUser!!.uid, groupObj.roomUID, mDbRef,{
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
        mAuth: FirebaseAuth,
        mDbRef: DatabaseReference,
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

    fun getGroupChatRoomSingle(chatRoomUID: String, mDbRef: DatabaseReference, loopCallback: (chatRoom: GroupRoom?) -> Unit, afterCallback: () -> Unit){
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

    fun getGroupChatRoomRecurrent(chatRoomUID: String, mDbRef: DatabaseReference, loopCallback: (groupRoom: GroupRoom?) -> Unit, afterCallback: () -> Unit){
        mDbRef
            .child("groupRooms")
            .orderByChild("roomUID")
            .equalTo(chatRoomUID)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (i in snapshot.children){
                        val item = i.getValue(GroupRoom::class.java)
                        loopCallback(item)
                        DebugUtils.log_firebase("get group room recurrent successful")
                    }

                    afterCallback()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    // TODO: Modify to accommodate simultaneous first message possibilities
    fun sendGroupMessage(
        message: Message,
        chatRoomID: String,
        mDbRef: DatabaseReference,
        successCallback: () -> Unit,
        failureCallback: () -> Unit){

        // push the message to the chatroom
        mDbRef
            .child("groupRooms")
            .child(chatRoomID)
            .child("messages")
            .push()
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

    fun makeAdmin(userId: String, roomId: String, mDbRef: DatabaseReference, successCallback: () -> Unit, failureCallback: () -> Unit){
        val reference = mDbRef.child("groupRooms").child(roomId)

        reference
            .child("admins")
            .child(userId)
            .setValue(userId)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    groupNowAdminMessage(userId, roomId, mDbRef, {
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

    // TODO: implement Remove Admin
    fun removeAdmin(userId: String, roomId: String, mAuth: FirebaseAuth, mDbRef: DatabaseReference, successCallback: () -> Unit, failureCallback: () -> Unit){
        val reference = mDbRef.child("groupRooms").child(roomId)
        val admins = selectedGroupRoom.value!!.admins
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
                        groupNotAdminMessage(userId, roomId, mDbRef, {
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

    fun removeFromGroup(firebaseAuth: FirebaseAuth, userId: String, groupId: String, mDbRef: DatabaseReference, successCallback: () -> Unit, failureCallback: () -> Unit){
        val groupRef = mDbRef.child("groupRooms").child(groupId)
        val userRef = mDbRef.child("Users").child(userId)

        groupRef
            .child("participants")
            .child(userId)
            .removeValue()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    groupRemoveMessage(firebaseAuth.currentUser!!.uid, userId, groupId, mDbRef, {
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

    fun addGroupMembers(users: List<String>, currentUserId: String, groupId: String, mDbRef: DatabaseReference, successCallback: (userId: String) -> Unit, failureCallback: (userId: String) -> Unit, afterCallback: () -> Unit){
        val groupReference = mDbRef.child("groupRooms").child(groupId)

        groupAddMessage(users, currentUserId, groupId, mDbRef, {
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
                                .setValue(groupId)
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

    fun leaveGroup(groupId: String, mDbRef: DatabaseReference, mAuth: FirebaseAuth, successCallback: () -> Unit, failureCallback: () -> Unit){
        val groupReference = mDbRef.child("groupRooms").child(groupId)
        val userReference = mDbRef.child("Users").child(mAuth.currentUser!!.uid)
        val currentUserId: String = mAuth.currentUser!!.uid
        val admins = selectedGroupRoom.value!!.admins

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
            groupExitMessage(currentUserId, groupId, mDbRef, {
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
                                .child(groupId)
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

    fun groupAddMessage(users: List<String>, currentUserId: String, roomId: String, mDbRef: DatabaseReference, successCallback: () -> Unit, failureCallback: () -> Unit){
        val usersInString = users.joinToString(
            separator = " ",
        )
        val message = Message(SystemMessageType.GROUP_ADD, "${currentUserId} ${usersInString}", System.currentTimeMillis(), "SYSTEM", MessageType.TEXT)
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

    fun groupExitMessage(userId: String, roomId: String, mDbRef: DatabaseReference, successCallback: () -> Unit, failureCallback: () -> Unit){
        val message = Message(SystemMessageType.GROUP_EXIT, userId, System.currentTimeMillis(), "SYSTEM", MessageType.TEXT)
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

    fun groupRemoveMessage(currentUserId: String, userId: String, roomId: String, mDbRef: DatabaseReference, successCallback: () -> Unit, failureCallback: () -> Unit){
        val message = Message(SystemMessageType.GROUP_REMOVE, "${currentUserId} ${userId}", System.currentTimeMillis(), "SYSTEM", MessageType.TEXT)
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

    fun groupNowAdminMessage(userId: String, roomId: String, mDbRef: DatabaseReference, successCallback: () -> Unit, failureCallback: () -> Unit){
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

    fun groupNotAdminMessage(userId: String, roomId: String, mDbRef: DatabaseReference, successCallback: () -> Unit, failureCallback: () -> Unit){
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

    fun groupCreateMessage(currentUserId: String, roomId: String, mDbRef: DatabaseReference, successCallback: () -> Unit, failureCallback: () -> Unit){
        val message = Message(SystemMessageType.GROUP_CREATE, currentUserId, System.currentTimeMillis(), "SYSTEM", MessageType.TEXT)
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

    fun editUser(key: String, value: String, firebaseAuth: FirebaseAuth, mDbRef: DatabaseReference){
        val databaseRef = mDbRef.child("Users").child(firebaseAuth.currentUser?.uid.toString())

        if(value.isEmpty()){
            databaseRef.child(key).setValue(null).addOnCompleteListener {
                DebugUtils.log_firebase("edit user ${key} successful")
            }
        } else {
            databaseRef.child(key).setValue(value).addOnCompleteListener {
                if(it.isSuccessful){
                    DebugUtils.log_firebase("edit user ${key} successful")
                }
            }
        }
        // TODO: Implement success and failure callbacks
    }

    fun editGroup(key: String, value: String, groupId: String, mDbRef: DatabaseReference){
        val databaseRef = mDbRef.child("groupRooms").child(groupId)

        if(value.isEmpty()){
            databaseRef.child(key).setValue(null)
            DebugUtils.log_firebase("edit group ${key} successful")
        } else {
            databaseRef.child(key).setValue(value)
            DebugUtils.log_firebase("edit group ${key} successful")

        }
    }

    fun addGroupToFavorites(groupId: String, mAuth: FirebaseAuth, mDbRef: DatabaseReference, successCallback: () -> Unit, failureCallback: () -> Unit){
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

    fun removeGroupFromFavorites(groupId: String, mAuth: FirebaseAuth, mDbRef: DatabaseReference, successCallback: () -> Unit, failureCallback: () -> Unit) {
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
    fun signOut(mAuth: FirebaseAuth){
        // delete both user and image from the local db
        mAuth.signOut()
        DebugUtils.log_firebase("sign out called")
    }

    fun uploadProfileImage(profileImage: ProfileImage, base64: String, firebaseStorage: FirebaseStorage, mDbRef: DatabaseReference, userId: String, successCallback: ()-> Unit, failureCallback: ()-> Unit){
        val reference = mDbRef.child("ProfileImages").child(userId)
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

    fun removeProfileImage(mDbRef: DatabaseReference, userId: String, firebaseStorage: FirebaseStorage, successCallback: ()-> Unit, failureCallback: ()-> Unit){
        val reference = mDbRef.child("ProfileImages").child(userId)

        firebaseStorage.getReference("/profileImages/${userId}").delete().addOnCompleteListener {
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

    fun getProfileImage(userId: String, firebaseStorage: FirebaseStorage, mDbRef: DatabaseReference, loopCallback: (profileImage: ProfileImage?) -> Unit, afterCallback: () -> Unit, snapshotExistenceCallback: (bool: Boolean)-> Unit){
        val storageRef = firebaseStorage.reference.child("/profileImages/${userId}")
        storageRef.getBytes(2_000_000).addOnCompleteListener {

            if(it.isSuccessful){
                mDbRef.child("ProfileImages").orderByChild("ownerId").equalTo(userId).addListenerForSingleValueEvent(object: ValueEventListener{
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

    fun appendProfileImageTimestamp(mAuth: FirebaseAuth, mDbRef: DatabaseReference, timestamp: Long, successCallback: () -> Unit, failureCallback: () -> Unit){
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

    fun appendGroupImageTimestamp(groupId: String, mDbRef: DatabaseReference, timestamp: Long, successCallback: () -> Unit, failureCallback: () -> Unit){
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

    fun uploadChatImage(image: Image, chatRoomID: String, base64: String, firebaseStorage: FirebaseStorage, mDbRef: DatabaseReference, successCallback: (image: Image)-> Unit, failureCallback: ()-> Unit){
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

    fun getChatImage(imageId: String, chatRoomID: String, mDbRef: DatabaseReference, firebaseStorage: FirebaseStorage, loopCallback: (image: Image?) -> Unit, afterCallback: () -> Unit){
        val storageRef = firebaseStorage.reference.child("/chatImages/${chatRoomID}/${imageId}")
        storageRef.getBytes(2000000).addOnCompleteListener {
            if(it.isSuccessful){
                mDbRef.child("chatImages").child(chatRoomID).orderByChild("imageId").equalTo(imageId).addListenerForSingleValueEvent(object: ValueEventListener{
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

    fun uploadPublicPost(publicPost: PublicPost, base64: String, firebaseStorage: FirebaseStorage, mDbRef: DatabaseReference, successCallback: (publicPost: PublicPost)-> Unit, failureCallback: ()-> Unit){
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
    fun removePublicPost(publicPost: PublicPost, firebaseStorage: FirebaseStorage, mDbRef: DatabaseReference, mAuth: FirebaseAuth, successCallback: ()-> Unit = {}, failureCallback: ()-> Unit = {}){
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

    fun getPublicPost(postId: String, firebaseStorage: FirebaseStorage, mDbRef: DatabaseReference, loopCallback: (publicPost: PublicPost?) -> Unit, afterCallback: () -> Unit){
        val storageRef = firebaseStorage.reference.child("/publicPosts/${postId}")
        storageRef.getBytes(2000000).addOnCompleteListener {
            if(it.isSuccessful){
                mDbRef.child("public_posts").orderByChild("postId").equalTo(postId).addListenerForSingleValueEvent(object: ValueEventListener{
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

    fun appendPublicPostIdToUser(mAuth: FirebaseAuth, mDbRef: DatabaseReference, postId: String, successCallback: () -> Unit, failureCallback: () -> Unit){
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
    fun deleteRecentSearchHistory(mAuth: FirebaseAuth, mDbRef: DatabaseReference, successCallback: () -> Unit = {}, failureCallback: () -> Unit = {}){
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

    fun addToRecentSearch(userId: String, firebaseAuth: FirebaseAuth, mDbRef: DatabaseReference, successCallback: () -> Unit = {}, failureCallback: () -> Unit = {}){
        mDbRef
            .child("Users")
            .child(firebaseAuth.currentUser!!.uid)
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
    fun uploadBugReport(bugReport: BugReport, mDbRef: DatabaseReference, mAuth: FirebaseAuth, successCallback: () -> Unit = {}, failureCallback: () -> Unit = {}){
        val databaseReference = mDbRef.child("bug_reports").child(mAuth.currentUser!!.uid)
        databaseReference.child(bugReport.reportId).setValue(bugReport)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    DebugUtils.log_firebase("upload bug report successful")

                    successCallback()
                } else {
                    failureCallback()
                }
            }
    }

    // TODO: Implement delete account
    fun deleteAccount(){
        // delete from auth db

        // delete from realtime db

        // also delete user images
    }
}
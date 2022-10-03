package com.varsel.firechat.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.varsel.firechat.model.Chat.ChatRoom
import com.varsel.firechat.model.Chat.GroupRoom
import com.varsel.firechat.model.User.User
import com.varsel.firechat.model.Message.Message
import com.varsel.firechat.model.Message.MessageType
import com.varsel.firechat.model.Message.SystemMessageType

class FirebaseViewModel: ViewModel() {
    val usersLiveData = MutableLiveData<List<User>>()
    val selectedUser = MutableLiveData<User?>()
    val currentUser = MutableLiveData<User?>()
    val currentUserSingle = MutableLiveData<User?>()
    val friendRequests = MutableLiveData<List<User?>>()
    var friends = MutableLiveData<List<User?>>()
    var chatRooms = MutableLiveData<MutableList<ChatRoom?>>()
    var selectedChatRoom = MutableLiveData<ChatRoom?>()
    val groupRooms = MutableLiveData<MutableList<GroupRoom?>>()
    val selectedGroupRoom = MutableLiveData<GroupRoom?>()
    val usersQuery = MutableLiveData<List<User>>()
    val selectedGroupParticipants = MutableLiveData<List<User>>()

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

    fun getCurrentUserRecurrent(mAuth: FirebaseAuth?, mDbRef: DatabaseReference, beforeCallback: () -> Unit, afterCallback: () -> Unit){
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

            }

        })
    }

    fun getCurrentUserSingle(mAuth: FirebaseAuth?, mDbRef: DatabaseReference, beforeCallback: () -> Unit, afterCallback: () -> Unit){
        mDbRef.child("Users").orderByChild("userUID").equalTo(mAuth?.currentUser?.uid.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                beforeCallback()

                for (item in snapshot.children){
                    val user = item.getValue(User::class.java)
                    currentUserSingle.value = user
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
                    selectedUser.value = user
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
                    loopCallback(user)
                }
                afterCallback()
            }

            override fun onCancelled(error: DatabaseError) {

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

    fun rejectFriendRequest(user: User){
        // remove from request list
    }

    // TODO: Implement unfriend user
    fun unfriendUser(user: User){

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
                } else {
                    failureCallback()
                }
            }
    }

    fun appendChatRoom(uid: String, otherUser: String, mAuth: FirebaseAuth, mDbRef: DatabaseReference, successCallback: () -> Unit, failureCallback: () -> Unit){
        mDbRef
            .child("Users")
            .child(otherUser)
            .child("chatRooms")
            .child(uid)
            .setValue(uid)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    mDbRef
                        .child("Users")
                        .child(mAuth.currentUser?.uid.toString())
                        .child("chatRooms")
                        .child(uid)
                        .setValue(uid)
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

    fun getChatRoom(chatRoomUID: String, mDbRef: DatabaseReference, loopCallback: (chatRoom: ChatRoom?) -> Unit, afterCallback: () -> Unit){
        mDbRef
            .child("chatRooms")
            .orderByChild("roomUID")
            .equalTo(chatRoomUID)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (i in snapshot.children){
                        val item = i.getValue(ChatRoom::class.java)
                        loopCallback(item)
                    }

                    afterCallback()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
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

        currentUserRef
            .child("groupRooms")
            .child(roomUid)
            .setValue(roomUid)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    otherUserRef
                        .child("groupRooms")
                        .child(roomUid)
                        .setValue(roomUid)
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
            databaseRef.child(key).setValue(null)
        } else {
            databaseRef.child(key).setValue(value)
        }
        // TODO: Implement success and failure callbacks
    }

    fun editGroup(key: String, value: String, groupId: String, mDbRef: DatabaseReference){
        val databaseRef = mDbRef.child("groupRooms").child(groupId)

        if(value.isEmpty()){
            databaseRef.child(key).setValue(null)
        } else {
            databaseRef.child(key).setValue(value)
        }
    }

    fun addGroupToFavorites(groupId: String, mAuth: FirebaseAuth, mDbRef: DatabaseReference, successCallback: () -> Unit, failureCallback: () -> Unit){
        val currentUserId = mAuth.currentUser!!.uid
        val userReference = mDbRef.child("Users").child(currentUserId)

        userReference
            .child("favoriteGroups")
            .child(groupId)
            .setValue(groupId)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    successCallback()
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
                } else {
                    failureCallback()
                }
            }
    }

        // TODO: Implement delete chatroom
    fun deleteChatRoom(){
        // push to deleted
    }

    fun signOut(mAuth: FirebaseAuth){
        mAuth.signOut()
    }

    // TODO: Implement delete account
    fun deleteAccount(){
        // delete from auth db

        // delete from realtime db
    }
}
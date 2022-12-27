package com.varsel.firechat.data.repository

import android.os.Message
import android.util.Log
import com.google.firebase.database.ValueEventListener
import com.varsel.firechat.common.Resource
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.data.remote.Firebase
import com.varsel.firechat.domain.repository.CurrentUserRepository
import com.varsel.firechat.domain.repository.MessageRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    val firebase: Firebase,
    val currentUserRepository: CurrentUserRepository
): MessageRepository {
    private val chatRoomsFlow = MutableStateFlow<Resource<MutableList<ChatRoom>>>(Resource.Loading())
    private val groupRooms = MutableStateFlow<Resource<MutableList<GroupRoom>>>(Resource.Loading())

    override fun sendMessage(message: Message, chatRoomId: String): Flow<Response> {
        TODO("Not yet implemented")
    }

    override fun appendParticipants(chatRoom: ChatRoom): Flow<Response> {
        TODO("Not yet implemented")
    }

    override fun appendChatroomToUser(chatRoom: ChatRoom, otherUser: User): Flow<Response> {
        TODO("Not yet implemented")
    }

    override fun getChatRoomsRecurrent(): MutableStateFlow<Resource<List<ChatRoom>>> {
        return chatRoomsFlow as MutableStateFlow<Resource<List<ChatRoom>>>
    }

    override fun initialiseGetChatRoomsRecurrentStream(): Flow<Response> = callbackFlow {
        chatRoomsFlow.value = Resource.Loading()
        trySend(Response.Loading())

        val chatRoomsUID = currentUserRepository.getCurrentUserRecurrent().value.data?.chatRooms?.keys
        val chatRooms = mutableListOf<ChatRoom>()
        val listeners = mutableListOf<ValueEventListener>()

        for(i in chatRoomsUID ?: listOf()){
            val listener = firebase.getChatRoomRecurrent(i, { chatRoom ->
                if(chatRoomsFlow.value.data != null) {
                    val groupIterator = chatRoomsFlow.value.data!!.iterator()
                    while (groupIterator.hasNext()){
                        val g = groupIterator.next()
                        if(g.roomUID == chatRoom.roomUID){
                            groupIterator.remove()

                            // TODO: Remove listener here
                        }
                    }
                }
                chatRooms.add(chatRoom)
            }, {
                chatRoomsFlow.value = Resource.Success(chatRooms)
                trySend(Response.Success())
            })

            listeners.add(listener)
        }

        awaitClose {
            Log.d("CLEAN", "Await close for chat room ran")
            Log.d("CLEAN", "Listener Count: ${listeners.size}")
            for (i in listeners) {
                // TODO: Remove value event listeners
//                firebase.mDbRef.removeEventListener(i)
            }
        }
    }

    override fun getChatRoomRecurrent(id: String): MutableStateFlow<Resource<ChatRoom>> {
        TODO("Not yet implemented")
    }

    override fun getChatRoomSingle(id: String): ChatRoom {
        TODO("Not yet implemented")
    }

    override fun createGroup(group: GroupRoom): Flow<Response> = callbackFlow {
        firebase.createGroup(group, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override fun appendGroupRoomsToUser(group: GroupRoom, user: String): Flow<Response> = callbackFlow {
        firebase.appendGroupRoomsToUser(group.roomUID, user, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override fun getGroupRoomSingle(id: String): GroupRoom {
        TODO("Not yet implemented")
    }

    override fun getGroupRoomsRecurrent(): MutableStateFlow<Resource<List<GroupRoom>>> {
        TODO("Not yet implemented")
    }

    override fun getGroupRoomRecurrent(id: String): MutableStateFlow<Resource<GroupRoom>> {
        TODO("Not yet implemented")
    }

    override fun sendGroupMessage(message: Message, group: GroupRoom): Flow<Response> {
        TODO("Not yet implemented")
    }

    override fun makeAdmin(user: User, group: GroupRoom): Flow<Response> {
        TODO("Not yet implemented")
    }

    override fun removeAdmin(user: User, group: GroupRoom): Flow<Response> {
        TODO("Not yet implemented")
    }

    override fun removeFromGroup(user: User, group: GroupRoom): Flow<Response> {
        TODO("Not yet implemented")
    }

    override fun addGroupMembers(user: User, group: GroupRoom): Flow<Response> {
        TODO("Not yet implemented")
    }

    override fun leaveGroup(chatRoom: GroupRoom): Flow<Response> {
        TODO("Not yet implemented")
    }

    override fun sendGroupAddMessage(users: List<String>, group: GroupRoom): Flow<Response> {
        TODO("Not yet implemented")
    }

    override fun sendGroupExitMessage(group: GroupRoom): Flow<Response> {
        TODO("Not yet implemented")
    }

    override fun sendGroupRemoveMessage(user: String, group: GroupRoom): Flow<Response> {
        TODO("Not yet implemented")
    }

    override fun sendGroupNowAdminMessage(user: String, group: GroupRoom): Flow<Response> {
        TODO("Not yet implemented")
    }

    override fun sendGroupNotAdminMessage(user: String, group: GroupRoom): Flow<Response> {
        TODO("Not yet implemented")
    }

    override fun groupCreateMessage(group: GroupRoom): Flow<Response> {
        TODO("Not yet implemented")
    }

    override fun editGroup(key: String, value: String, groupId: String): Flow<Response> {
        TODO("Not yet implemented")
    }
}
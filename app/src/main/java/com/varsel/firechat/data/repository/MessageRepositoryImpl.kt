package com.varsel.firechat.data.repository

import com.google.firebase.database.ValueEventListener
import com.varsel.firechat.common.Resource
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.Message.Message
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
    private val groupRoomsFlow = MutableStateFlow<Resource<MutableList<GroupRoom>>>(Resource.Loading())

    override fun sendMessage(message: Message, chatRoomId: String): Flow<Response> = callbackFlow {
        firebase.sendMessage(message, chatRoomId, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override fun appendParticipants(chatRoom: ChatRoom): Flow<Response> = callbackFlow {
        firebase.appendParticipants(chatRoom, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override fun appendChatroomToUser(roomId: String, otherUser: String): Flow<Response> = callbackFlow {
        firebase.appendGroupRoomsToUser(roomId, otherUser, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
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
            for (i in listeners) {
                // TODO: Remove value event listeners
                firebase.chat_room_ref.removeEventListener(i)
            }
        }
    }

    override fun initialiseGetGroupRoomsRecurrentStream(): Flow<Response> = callbackFlow {
        groupRoomsFlow.value = Resource.Loading()
        trySend(Response.Loading())

        val groupRoomIDs = currentUserRepository.getCurrentUserRecurrent().value.data?.groupRooms?.keys
        val groupRooms = mutableListOf<GroupRoom>()
        val listeners = mutableListOf<ValueEventListener>()

        // TODO: Fix potential memory leak
        for (i in groupRoomIDs ?: listOf()){
            // TODO: Fix bug here:
            val listener = firebase.getGroupChatRoomRecurrent(i, {
                if(groupRoomsFlow.value.data != null){
                    val groupIterator = groupRoomsFlow.value.data!!.iterator()
                    while (groupIterator.hasNext()){
                        val g = groupIterator.next()
                        if(g.roomUID == it.roomUID){
                            groupIterator.remove()
                        }
                    }
                }
                groupRooms.add(it)
            }, {
                groupRoomsFlow.value = Resource.Success(groupRooms)
                trySend(Response.Success())

            })

            listeners.add(listener)
        }

        awaitClose {
            for (i in listeners) {
                // TODO: Remove value event listeners
                firebase.group_room_ref.removeEventListener(i)
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
        return groupRoomsFlow as MutableStateFlow<Resource<List<GroupRoom>>>
    }

    override fun getGroupRoomRecurrent(id: String): Flow<Resource<GroupRoom>> = callbackFlow {
        trySend(Resource.Loading())
        groupRoomsFlow.value.data?.map {
            if(it.roomUID == id) {
                trySend(Resource.Success(it))
            }
        }

        awaitClose {  }
    }

    override fun sendGroupMessage(message: Message, roomId: String): Flow<Response> = callbackFlow {
        firebase.sendGroupMessage(message, roomId, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override fun makeAdmin(userId: String, group: GroupRoom): Flow<Response> = callbackFlow {
        firebase.makeAdmin(userId, group.roomUID, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override fun removeAdmin(userId: String, group: GroupRoom): Flow<Response> = callbackFlow {
        firebase.removeAdmin(userId, group, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override fun removeFromGroup(userId: String, group: GroupRoom): Flow<Response> = callbackFlow {
        firebase.removeFromGroup(userId, group.roomUID, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override fun addGroupMembers(users: List<String>, groupID: String): Flow<Response> = callbackFlow {
        firebase.addGroupMembers(users, groupID, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        }, {

        })

        awaitClose {  }
    }

    override fun leaveGroup(chatRoom: GroupRoom): Flow<Response> = callbackFlow {
        firebase.leaveGroup(chatRoom, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override fun sendGroupAddMessage(users: List<String>, group: GroupRoom): Flow<Response> = callbackFlow {
        firebase.groupAddMessage(users, group.roomUID, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override fun sendGroupExitMessage(group: GroupRoom): Flow<Response> = callbackFlow {
        firebase.groupExitMessage(group.roomUID, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override fun sendGroupRemoveMessage(user: String, group: GroupRoom): Flow<Response> = callbackFlow {
        firebase.groupRemoveMessage(user, group.roomUID, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override fun sendGroupNowAdminMessage(user: String, group: GroupRoom): Flow<Response> = callbackFlow {
        firebase.groupNowAdminMessage(user, group.roomUID, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override fun sendGroupNotAdminMessage(user: String, group: GroupRoom): Flow<Response> = callbackFlow {
        firebase.groupNotAdminMessage(user, group.roomUID, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override fun groupCreateMessage(group: GroupRoom): Flow<Response> = callbackFlow {
        firebase.groupCreateMessage(group.roomUID, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override fun editGroup(key: String, value: String, groupId: String): Flow<Response> = callbackFlow {
        firebase.editGroup(key, value, groupId, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }
}
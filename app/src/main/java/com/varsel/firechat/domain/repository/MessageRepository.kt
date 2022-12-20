package com.varsel.firechat.domain.repository

import android.os.Message
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Chat.ChatRoomEntity
import com.varsel.firechat.data.local.Chat.GroupRoomEntity
import com.varsel.firechat.domain.model.ChatRoom
import com.varsel.firechat.domain.model.User

interface MessageRepository {

    suspend fun sendMessage(message: Message, chatRoomId: String) : Response
    suspend fun appendParticipants(chatRoom: ChatRoom) : Response
    suspend fun appendChatroomToUser(chatRoom: ChatRoomEntity, otherUser: User) : Response
    suspend fun getChatRoomRecurrent(id: String) : ChatRoom
    suspend fun getChatRoomSingle(id: String) : ChatRoom


    suspend fun createGroup(group: GroupRoomEntity) : Response
    suspend fun appendGroupRoomsToUser(group: GroupRoomEntity, user: User) : Response
    suspend fun getGroupRoomSingle(id: String) : GroupRoomEntity
    suspend fun getGroupRoomRecurrent(id: String) : GroupRoomEntity
    suspend fun sendGroupMessage(message: Message, group: GroupRoomEntity) : Response
    suspend fun makeAdmin(user: User, group: GroupRoomEntity) : Response
    suspend fun removeAdmin(user: User, group: GroupRoomEntity) : Response
    suspend fun removeFromGroup(user: User, group: GroupRoomEntity) : Response
    suspend fun addGroupMembers(user: User, group: GroupRoomEntity) : Response
    suspend fun leaveGroup(chatRoom: GroupRoomEntity) : Response

    suspend fun sendGroupAddMessage(users: List<String>, group: GroupRoomEntity) : Response
    suspend fun sendGroupExitMessage(group: GroupRoomEntity) : Response
    suspend fun sendGroupRemoveMessage(user: String, group: GroupRoomEntity) : Response
    suspend fun sendGroupNowAdminMessage(user: String, group: GroupRoomEntity) : Response
    suspend fun sendGroupNotAdminMessage(user: String, group: GroupRoomEntity) : Response
    suspend fun groupCreateMessage(group: GroupRoomEntity) : Response
    suspend fun editGroup(key: String, value: String, groupId: String) : Response


}
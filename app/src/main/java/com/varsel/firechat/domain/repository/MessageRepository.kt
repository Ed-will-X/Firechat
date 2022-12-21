package com.varsel.firechat.domain.repository

import android.os.Message
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.User.User

interface MessageRepository {

    suspend fun sendMessage(message: Message, chatRoomId: String) : Response
    suspend fun appendParticipants(chatRoom: ChatRoom) : Response
    suspend fun appendChatroomToUser(chatRoom: ChatRoom, otherUser: User) : Response
    suspend fun getChatRoomRecurrent(id: String) : ChatRoom
    suspend fun getChatRoomSingle(id: String) : ChatRoom


    suspend fun createGroup(group: GroupRoom) : Response
    suspend fun appendGroupRoomsToUser(group: GroupRoom, user: User) : Response
    suspend fun getGroupRoomSingle(id: String) : GroupRoom
    suspend fun getGroupRoomRecurrent(id: String) : GroupRoom
    suspend fun sendGroupMessage(message: Message, group: GroupRoom) : Response
    suspend fun makeAdmin(user: User, group: GroupRoom) : Response
    suspend fun removeAdmin(user: User, group: GroupRoom) : Response
    suspend fun removeFromGroup(user: User, group: GroupRoom) : Response
    suspend fun addGroupMembers(user: User, group: GroupRoom) : Response
    suspend fun leaveGroup(chatRoom: GroupRoom) : Response

    suspend fun sendGroupAddMessage(users: List<String>, group: GroupRoom) : Response
    suspend fun sendGroupExitMessage(group: GroupRoom) : Response
    suspend fun sendGroupRemoveMessage(user: String, group: GroupRoom) : Response
    suspend fun sendGroupNowAdminMessage(user: String, group: GroupRoom) : Response
    suspend fun sendGroupNotAdminMessage(user: String, group: GroupRoom) : Response
    suspend fun groupCreateMessage(group: GroupRoom) : Response
    suspend fun editGroup(key: String, value: String, groupId: String) : Response


}
package com.varsel.firechat.domain.repository

import android.os.Message
import com.varsel.firechat.common.Resource
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.User.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface MessageRepository {
    fun sendMessage(message: Message, chatRoomId: String) : Flow<Response>
    fun appendParticipants(chatRoom: ChatRoom) : Flow<Response>
    fun appendChatroomToUser(chatRoom: ChatRoom, otherUser: User) : Flow<Response>
    fun getChatRoomsRecurrent(): MutableStateFlow<Resource<List<ChatRoom>>>
    fun initialiseGetChatRoomsRecurrentStream(): Flow<Response>
    fun getChatRoomRecurrent(id: String) : MutableStateFlow<Resource<ChatRoom>>
    fun getChatRoomSingle(id: String) : ChatRoom


    fun createGroup(group: GroupRoom) : Flow<Response>
    fun appendGroupRoomsToUser(group: GroupRoom, user: String) : Flow<Response>
    fun initialiseGetGroupRoomsRecurrentStream(): Flow<Response>
    fun getGroupRoomSingle(id: String) : GroupRoom
    fun getGroupRoomsRecurrent(): MutableStateFlow<Resource<List<GroupRoom>>>
    fun getGroupRoomRecurrent(id: String) : MutableStateFlow<Resource<GroupRoom>>
    fun sendGroupMessage(message: Message, group: GroupRoom) : Flow<Response>
    fun makeAdmin(user: User, group: GroupRoom) : Flow<Response>
    fun removeAdmin(user: User, group: GroupRoom) : Flow<Response>
    fun removeFromGroup(user: User, group: GroupRoom) : Flow<Response>
    fun addGroupMembers(user: User, group: GroupRoom) : Flow<Response>
    fun leaveGroup(chatRoom: GroupRoom) : Flow<Response>


    fun sendGroupAddMessage(users: List<String>, group: GroupRoom) : Flow<Response>
    fun sendGroupExitMessage(group: GroupRoom) : Flow<Response>
    fun sendGroupRemoveMessage(user: String, group: GroupRoom) : Flow<Response>
    fun sendGroupNowAdminMessage(user: String, group: GroupRoom) : Flow<Response>
    fun sendGroupNotAdminMessage(user: String, group: GroupRoom) : Flow<Response>
    fun groupCreateMessage(group: GroupRoom) : Flow<Response>
    fun editGroup(key: String, value: String, groupId: String) : Flow<Response>


}
package com.varsel.firechat.domain.repository

import com.varsel.firechat.common.Resource
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.Message.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface MessageRepository {
    fun sendMessage(message: Message, chatRoomId: String) : Flow<Response>
    fun appendParticipants(chatRoom: ChatRoom) : Flow<Response>
    fun appendChatroomToUser(roomID: String, otherUser: String) : Flow<Response>
    fun getChatRoomsRecurrent(): MutableStateFlow<Resource<List<ChatRoom>>>
    fun initialiseGetChatRoomsRecurrentStream(scope: CoroutineScope): Flow<Response>
    fun getChatRoomRecurrent(id: String) : MutableStateFlow<Resource<ChatRoom>>
    fun getChatRoomSingle(id: String) : ChatRoom


    fun createGroup(group: GroupRoom) : Flow<Response>
    fun appendGroupRoomsToUser(group: GroupRoom, user: String) : Flow<Response>
    fun initialiseGetGroupRoomsRecurrentStream(): Flow<Response>
    fun getGroupRoomSingle(id: String) : GroupRoom
    fun getGroupRoomsRecurrent(): MutableStateFlow<Resource<List<GroupRoom>>>
    fun getGroupRoomRecurrent(id: String) : Flow<Resource<GroupRoom>>
    fun sendGroupMessage(message: Message, roomID: String) : Flow<Response>
    fun makeAdmin(user: String, group: GroupRoom) : Flow<Response>
    fun removeAdmin(user: String, group: GroupRoom) : Flow<Response>
    fun removeFromGroup(user: String, group: GroupRoom) : Flow<Response>
    fun addGroupMembers(users: List<String>, groupID: String) : Flow<Response>
    fun leaveGroup(chatRoom: GroupRoom) : Flow<Response>


    fun sendGroupAddMessage(users: List<String>, group: GroupRoom) : Flow<Response>
    fun sendGroupExitMessage(group: GroupRoom) : Flow<Response>
    fun sendGroupRemoveMessage(user: String, group: GroupRoom) : Flow<Response>
    fun sendGroupNowAdminMessage(user: String, group: GroupRoom) : Flow<Response>
    fun sendGroupNotAdminMessage(user: String, group: GroupRoom) : Flow<Response>
    fun groupCreateMessage(group: GroupRoom) : Flow<Response>
    fun editGroup(key: String, value: String, groupId: String) : Flow<Response>

    fun storeReceipt_chatRoom(message: Message, chatRoomId: String) : Flow<Response>
    fun storeReceipt_groupRoom(message: Message, groupRoomId: String) : Flow<Response>

    fun deleteMessageForAll_chatRoom(message: Message, chatRoomId: String) : Flow<Response>
    fun deleteMessageForAll_groupRoom(message: Message, groupRoomId: String) : Flow<Response>

    fun deleteMessage_chatRoom(message: Message, chatRoomId: String) : Flow<Response>
    fun deleteMessage_groupRoom(message: Message, groupRoomId: String) : Flow<Response>
}
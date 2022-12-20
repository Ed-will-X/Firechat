package com.varsel.firechat.domain.repository

import android.os.Message
import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.data.remote.dto.ChatRoomDto
import com.varsel.firechat.data.remote.dto.GroupRoomDto

interface MessageRepository {

    fun sendMessage(message: Message, chatRoomId: String, success: ()-> Unit, fail: ()-> Unit)
    fun appendParticipants(chatRoom: ChatRoom, success: ()-> Unit, failure: ()-> Unit)
    fun appendChatroomToUser(chatRoom: ChatRoom, otherUser: User, success: ()-> Unit, failure: ()-> Unit)
    fun getChatRoomRecurrent(id: String, success: (chatRoom: ChatRoomDto) -> Unit, after: ()-> Unit)
    fun getChatRoomSingle(id: String, success: (chatRoom: ChatRoomDto) -> Unit, after: ()-> Unit)


    fun createGroup(group: GroupRoom, success: () -> Unit, fail: () -> Unit)
    fun appendGroupRoomsToUser(group: GroupRoom, user: User, success: () -> Unit, fail: () -> Unit)
    fun getGroupRoomSingle(id: String, success: (groupRoom: GroupRoomDto) -> Unit, after: ()-> Unit)
    fun getGroupRoomRecurrent(id: String, success: (groupRoom: GroupRoomDto) -> Unit, after: ()-> Unit)
    fun sendGroupMessage(message: Message, group: GroupRoom, success: () -> Unit, fail: () -> Unit)
    fun makeAdmin(user: User, group: GroupRoom, success: () -> Unit, fail: () -> Unit)
    fun removeAdmin(user: User, group: GroupRoom, success: () -> Unit, fail: () -> Unit)
    fun removeFromGroup(user: User, group: GroupRoom, success: () -> Unit, fail: () -> Unit)
    fun addGroupMembers(user: User, group: GroupRoom, success: () -> Unit, fail: () -> Unit, after: () -> Unit)
    fun leaveGroup(chatRoom: GroupRoom, success: () -> Unit, fail: () -> Unit)

    fun sendGroupAddMessage(users: List<String>, group: GroupRoom, success: () -> Unit, fail: () -> Unit)
    fun sendGroupExitMessage(group: GroupRoom, success: () -> Unit, fail: () -> Unit)
    fun sendGroupRemoveMessage(user: String, group: GroupRoom, success: () -> Unit, fail: () -> Unit)
    fun sendGroupNowAdminMessage(user: String, group: GroupRoom, success: () -> Unit, fail: () -> Unit)
    fun sendGroupNotAdminMessage(user: String, group: GroupRoom, success: () -> Unit, fail: () -> Unit)
    fun groupCreateMessage(group: GroupRoom, success: () -> Unit, fail: () -> Unit)
    fun editGroup(key: String, value: String, groupId: String)


}
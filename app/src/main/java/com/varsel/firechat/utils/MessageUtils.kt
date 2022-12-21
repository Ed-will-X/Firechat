package com.varsel.firechat.utils

import com.varsel.firechat.R
import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.Message.Message
import com.varsel.firechat.data.local.Message.SystemMessageType
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

class MessageUtils {
    // TODO: Change the >1m text to "moments ago"
    companion object{
        fun formatStampMessage(timeString: String): String{
            val prettyTime = PrettyTime(Locale.getDefault())
            val ago: String = prettyTime.format(Date(timeString.toLong()))

            if(ago == "moments ago"){
                return "1s"
            } else if(ago == "moments from now"){
                return "1s"
            }else {
                val arr = ago.split(" ").toTypedArray()
                return "${arr[0]}${arr[1][0]}"
            }
        }

        // TODO: Use string resources
        fun formatStampChatsPage(timeString: String): String{
            val prettyTime = PrettyTime(Locale.getDefault())
            val ago: String = prettyTime.format(Date(timeString.toLong()))

            if(ago == "moments ago"){
                return "1s ago"
            } else if(ago == "moments from now"){
                return "1s ago"
            } else {
                val arr = ago.split(" ").toTypedArray()
                return "${arr[0]}${arr[1][0]} ${arr[2]}"
            }
        }

        // TODO: Remove length
        fun generateUID(length: Int) : String {
            return UUID.randomUUID().toString()
        }

        fun sortMessages(chatRoom: ChatRoom?): List<Message>?{
            val sorted = chatRoom?.messages?.values?.sortedBy {
                it.time
            }

            return sorted
        }

        fun sortChats(chatRooms: List<ChatRoom>?): MutableList<ChatRoom>?{
            val sorted = chatRooms?.sortedBy {
                val sortedMessages = sortMessages(it)

                sortedMessages?.last()?.time
            }?.reversed()?.toMutableList()

            return sorted
        }

        fun findChatRoom(list: List<ChatRoom>, roomID: String, chatRoom: (chatRoom: ChatRoom)-> Unit){
            list.map {
                if(it.roomUID == roomID){
                    chatRoom(it)
                    return@map
                }
            }
        }

        fun findGroupRoom(list: List<GroupRoom>, roomID: String, groupRoom: (groupRoom: GroupRoom)-> Unit){
            list.map {
                if(it.roomUID == roomID){
                    groupRoom(it)
                    return@map
                }
            }
        }

        // TODO: Fix bug
        fun calculateTimestampDifferenceLess(currentMessageTime: Long, previousMessageTime: Long): Boolean{
            val day: Long = 86400000
            val hour: Long = 3600000
            val timestampNow = System.currentTimeMillis()

            if((currentMessageTime + day) > timestampNow){
                // apply hour rule
                if ((currentMessageTime - previousMessageTime) < hour){
                    return true
                } else {
                    return false
                }
            } else {
                // apply day rule
                if ((currentMessageTime - previousMessageTime) < day){
                    return true
                } else {
                    return false
                }
            }

        }

        fun calculateTimestampDifferenceMore(currentMessageTime: Long, previousMessageTime: Long, timeDifference: Long): Boolean{
            val day: Long = 86400000
            if((currentMessageTime - previousMessageTime) < day){
                return true
            } else {
                return false
            }
        }

        fun getLastMessage(chatRoom: ChatRoom): String {
            val sortedMessages: List<Message>? = MessageUtils.sortMessages(chatRoom)

            return (sortedMessages?.last()?.message ?: "")
        }

        fun getLastMessageObject(chatRoom: ChatRoom): Message? {
            val sortedMessages: List<Message>? = MessageUtils.sortMessages(chatRoom)

            return (sortedMessages?.last())
        }

        fun getLastMessageTimestamp(chatRoom: ChatRoom): String {
            val sortedMessages: List<Message>? = MessageUtils.sortMessages(chatRoom)

            return (sortedMessages?.last()?.time.toString() ?: "")
        }

        fun formatSystemMessage(message: Message, activity: SignedinActivity, afterCallback: (message: String)-> Unit){
            val currentUser = activity.firebaseAuth.currentUser!!.uid
            if(message.messageUID == SystemMessageType.GROUP_REMOVE){
                val messageArr: Array<String> = message.message.split(" ").toTypedArray()

                UserUtils.getUser(messageArr[0], activity){ remover ->
                    UserUtils.getUser(messageArr[1], activity) { removed ->
//                    afterCallback("${if (remover.userUID == currentUser) "You" else "${remover.name}"} removed ${if(removed.userUID == currentUser) "You" else "${removed.name}"}")
                        afterCallback(activity.getString(R.string.group_removed, if (remover.userUID == currentUser) "You" else "${remover.name}", if(removed.userUID == currentUser) "You" else "${removed.name}"))
                    }
                }
                // TODO: Add lone return
            }

            if(message.messageUID == SystemMessageType.GROUP_ADD){
                val users: Array<String> = message.message.split(" ").toTypedArray()
                UserUtils.getUser(users[0], activity) {
                    if (users.size < 3) {
                        formatPerson(it.userUID, activity, {
                            afterCallback(activity.getString(R.string.group_add_second_person_singular))
                        }, {
                            afterCallback(activity.getString(R.string.group_add_third_person_singular, it.name))
                        })
                    } else {
                        formatPerson(it.userUID, activity, {
                            afterCallback(activity.getString(R.string.group_add_second_person, users.size -1))
                        }, {
                            afterCallback(activity.getString(R.string.group_add_third_person, it.name, users.size -1))
                        })
                    }
                }
                // TODO: Add lone return statement
            }

            UserUtils.getUser(message.message, activity) {
                if(message.messageUID == SystemMessageType.GROUP_CREATE){
                    formatPerson(it.userUID, activity, {
                        afterCallback(activity.getString(R.string.group_create_second_person, MessageUtils.formatStampChatsPage(message.time.toString())))
                    }, {
                        afterCallback(activity.getString(R.string.group_create_third_person, it.name, MessageUtils.formatStampChatsPage(message.time.toString())))
                    })
                }
                else if(message.messageUID == SystemMessageType.NOW_ADMIN){
                    formatPerson(it.userUID, activity, {
                        afterCallback(activity.getString(R.string.now_admin_second_person))
                    },{
                        afterCallback(activity.getString(R.string.now_admin_third_person, it.name))
                    })
                }
                else if(message.messageUID == SystemMessageType.NOT_ADMIN){
                    formatPerson(it.userUID, activity, {
                        afterCallback(activity.getString(R.string.not_admin_second_person))
                    },{
                        afterCallback(activity.getString(R.string.not_admin_third_person, it.name))
                    })
                }
                else if(message.messageUID == SystemMessageType.GROUP_EXIT){
                    formatPerson(it.userUID, activity, {
                        afterCallback(activity.getString(R.string.group_exit_second_person))
                    }, {
                        afterCallback(activity.getString(R.string.group_exit_third_person, it.name))
                    })
                }
            }
        }

        private fun formatPerson(userId: String, activity: SignedinActivity, secondPersonCallback: ()-> Unit, thirdPersonCallback: ()-> Unit){
            if(userId == activity.firebaseAuth.currentUser!!.uid){
                secondPersonCallback()
            } else {
                thirdPersonCallback()
            }
        }
    }
}
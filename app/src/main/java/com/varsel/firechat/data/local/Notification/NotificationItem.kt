package com.varsel.firechat.data.local.Notification

data class NotificationItem (
    val sender: String,
    val messageBody: String,
    var profile_image: String?,
    val user_id: String,
    val message_type: MessageType,
    val timestamp: String
)

val mock_notifications = mutableListOf(
    NotificationItem("Ligma", "Hiii", null, "10ennmdoie", MessageType.Text, "2h"),
    NotificationItem("Annalise", "Good morning, have you received the documents. If you've seen them, confirm this message", null, "ein30m", MessageType.Text, "6h"),
    NotificationItem("Balls Gulls", "What's up man?", null, "94n49md0", MessageType.Text, "8h"),
    NotificationItem("Eddie Williams", "Niceeee", null, "49jfj4", MessageType.Text, "18h"),
)

sealed class MessageType {
    object Text: MessageType()
    object Image: MessageType()
    object Location: MessageType()
    object Video: MessageType()
    object Gif: MessageType()
}
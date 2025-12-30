package project.c14230225.c14230235

import com.google.firebase.Timestamp

data class ChatList(
    var user1: String = "",
    var user2: String = "",
    var lastMessage: String = "",
    var lastMessageTime: Timestamp? = null,
    var unreadCount: Int = 0
)
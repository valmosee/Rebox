package project.c14230225.c14230235

import com.google.firebase.Timestamp

data class ChatList(
    var channelname: String = "",
    var otherUserEmail: String = "",
    var lastMessage: String = "",
    var lastMessageTime: Timestamp? = null,
    var unreadCount: Int = 0
)
package project.c14230225.c14230235

import com.google.firebase.Timestamp

class Chat(
    var id: String,
    var channelname: String,
    var user1: String,
    var user2: String,
    var tanggal: Timestamp?,
    var teks: String
) {
}
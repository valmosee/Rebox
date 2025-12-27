package project.c14230225.c14230235

import com.google.firebase.Timestamp

class Chat {
    var id: String = ""
    var channelname: String = ""
    var user1: String = ""
    var user2: String = ""
    var tanggal: Timestamp? = null
    var teks: String = ""

    // No-argument constructor (required by Firebase)
    constructor()

    // Constructor dengan parameter (untuk kemudahan penggunaan)
    constructor(
        id: String,
        channelname: String,
        user1: String,
        user2: String,
        tanggal: Timestamp?,
        teks: String
    ) {
        this.id = id
        this.channelname = channelname
        this.user1 = user1
        this.user2 = user2
        this.tanggal = tanggal
        this.teks = teks
    }
}
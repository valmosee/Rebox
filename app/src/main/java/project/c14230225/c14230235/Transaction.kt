package project.c14230225.c14230235

import com.google.firebase.Timestamp

data class Transaction(
    var id: String = "",
    var buyerEmail: String = "",
    var buyerUsername: String = "",
    var productId: String = "",
    var productName: String = "",
    var productImage: String = "",
    var productPrice: String = "",
    var productSize: String = "",
    var sellerUsername: String = "",
    var purchaseDate: Timestamp = Timestamp.now(),
    var status: String = "completed" // completed, pending, cancelled
) {
    constructor() : this("", "", "", "", "", "", "", "", "", Timestamp.now(), "completed")
}
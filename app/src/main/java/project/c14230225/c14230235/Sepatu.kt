package project.c14230225.c14230235

data class Sepatu(
    var id: String = "", // This will be the Firestore document ID
    var nama: String = "",
    var jenis: String = "",
    var ukuran: String = "",
    var harga: String = "",
    var deskripsi: String = "",
    var image: String = "",
    var username: String = "", // Seller's username for display
    var sellerEmail: String = "", // Seller's email for queries
    var sellerId: String = "" // Keep for backward compatibility (can be email)
)
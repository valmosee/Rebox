package project.c14230225.c14230235

data class sepatuFilter(
    var searchQuery: String = "",
    var jenisList: MutableList<String> = mutableListOf(),
    var ukuranList: MutableList<String> = mutableListOf(),
    var minPrice: Int? = null,
    var maxPrice: Int? = null
)

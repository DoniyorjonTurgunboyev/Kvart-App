package uz.gita.kvartarena.model

data class ItemUserGenerate(
    val uid: String,
    val name: String,
    var amount: Int,
    var negAmount: Int,
    var posAmount: Int,
    var visibility: Boolean = false
)

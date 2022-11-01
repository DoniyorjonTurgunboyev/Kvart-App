package uz.gita.kvartarena.model

data class User(
    var birthday: String? = "",
    var address1: String? = "",
    var name: String? = "",
    var surname: String? = "",
    var telegram: String? = "",
    var address2: String? = "",
    var kid: String? = "",
    var number: String? = ""
) : java.io.Serializable
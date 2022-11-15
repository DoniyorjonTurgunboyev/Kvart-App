package uz.gita.kvartarena.model

data class Apartment(
    var uid: String? = "",
    var name: String? = "",
    var address: String? = "",
    var lat: Double? = 0.0,
    var long: Double? = 0.0,
    var ownerid: String? = "",
    var owner: String? = "",
    var bio: String? = ""
)
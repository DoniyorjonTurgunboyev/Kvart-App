package uz.gita.kvartarena.model.geocoder

data class GeocoderResponseMetaData(
    val Point: Point,
    val found: String,
    val request: String,
    val results: String,
    val skip: String
)
package uz.gita.kvartarena.data.remote

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import uz.gita.kvartarena.model.geocoder.GeoApi

interface GeocoderService {
    @GET("1.x/")
    fun get(
        @Query("format") format: String,
        @Query("apikey") apikey: String,
        @Query("lang") lang: String,
        @Query("geocode") geocode: String,
    ): Call<GeoApi>
}
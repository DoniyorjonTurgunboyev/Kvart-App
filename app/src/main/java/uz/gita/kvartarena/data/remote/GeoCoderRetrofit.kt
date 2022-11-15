package uz.gita.kvartarena.data.remote

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GeoCoderRetrofit {
    companion object {
        private var instance: Retrofit? = null
        fun getRetrofit(): GeocoderService {
            if (instance == null) {
                instance = Retrofit.Builder()
                    .baseUrl("https://geocode-maps.yandex.ru/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return instance!!.create(GeocoderService::class.java)
        }
    }
}
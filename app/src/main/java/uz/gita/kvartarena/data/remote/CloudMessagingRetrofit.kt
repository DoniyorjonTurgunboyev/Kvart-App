package uz.gita.kvartarena.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CloudMessagingRetrofit {
    companion object {
        private var instance: Retrofit? = null
        fun getRetrofit(): CloudMessagingService {
            if (instance == null) {
                val okClient = OkHttpClient.Builder()
//                    .addInterceptor(ChuckerInterceptor(App.instance))
                    .addInterceptor { chain ->
                        val original = chain.request()
                        val requestBuilder = original.newBuilder()
                            .header(
                                "Authorization",
                                "key=AAAAvHSzi7U:APA91bF_z2ADKS6iBiXe4DbDGDm7rNxekk_kRHqRZHErwl4wWQyaQYVvMoTWB0PDc3iuyCo1FWcw1JMVQVWf4V2d4M5hc8umcce4nAYrd4MHCFtFFU1yuS16MYK8YEhcf9-LURS8a_oI"
                            )
                            .method(original.method(), original.body())
                        val request = requestBuilder.build()
                        chain.proceed(request)
                    }
                    .build()
//                val client = OkHttpClient.Builder()
//                    .addInterceptor(ChuckerInterceptor(App.instance))
//                    .build()
//                val httpClient = OkHttpClient()
//                httpClient.networkInterceptors().add(Interceptor { chain ->
//                    val requestBuilder: Request.Builder = chain.request().newBuilder()
//                    requestBuilder.header(
//                        "Authorization",
//                        "key=AAAAvHSzi7U:APA91bF_z2ADKS6iBiXe4DbDGDm7rNxekk_kRHqRZHErwl4wWQyaQYVvMoTWB0PDc3iuyCo1FWcw1JMVQVWf4V2d4M5hc8umcce4nAYrd4MHCFtFFU1yuS16MYK8YEhcf9-LURS8a_oI"
//                    )
//                    chain.proceed(requestBuilder.build())
//                })
                instance = Retrofit.Builder()
                    .baseUrl("https://fcm.googleapis.com/")
                    .client(okClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return instance!!.create(CloudMessagingService::class.java)
        }
    }
}
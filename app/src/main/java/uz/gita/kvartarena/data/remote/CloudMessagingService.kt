package uz.gita.kvartarena.data.remote

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import uz.gita.kvartarena.model.Notification
import uz.gita.kvartarena.model.NotificationResponse

interface CloudMessagingService {
    @POST("fcm/send")
    fun sendNotification(@Body notification: Notification): Call<NotificationResponse>
}
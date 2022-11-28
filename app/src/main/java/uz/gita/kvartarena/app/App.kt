package uz.gita.kvartarena.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.yandex.mapkit.MapKitFactory
//import com.yandex.mapkit.MapKitFactory
import uz.gita.kvartarena.data.local.EncryptedLocalStorage
import uz.gita.kvartarena.data.remote.FirebaseRemote
import uz.gita.kvartarena.model.Apartment
import uz.gita.kvartarena.model.User

class App : Application() {
    private val MAPKIT_API_KEY = "Your API Key"
    companion object {
        lateinit var instance: App
        lateinit var user: User
        lateinit var apart: Apartment
        fun initUser() {
            FirebaseRemote.getInstance().getUser("") {
                user = it
            }
        }

        fun initApart() {
            FirebaseRemote.getInstance().getApart(user.kid!!) {
                apart = it
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        MapKitFactory.setApiKey(MAPKIT_API_KEY)
        instance = this
        EncryptedLocalStorage.init(this)
    }
}
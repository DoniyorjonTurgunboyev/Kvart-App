package uz.gita.kvartarena.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import uz.gita.kvartarena.data.local.EncryptedLocalStorage
import uz.gita.kvartarena.data.remote.FirebaseRemote
import uz.gita.kvartarena.model.Apartment
import uz.gita.kvartarena.model.User

class App : Application() {
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
//        MapKitFactory.setApiKey("ecdc9b44-18a2-4e46-a701-139d0435f94e")
        instance = this
        EncryptedLocalStorage.init(this)
        initUser()
    }
}
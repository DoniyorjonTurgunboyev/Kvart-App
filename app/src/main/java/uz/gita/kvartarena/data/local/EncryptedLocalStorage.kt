package uz.gita.kvartarena.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.MasterKey
import uz.gita.kvartarena.utils.get
import uz.gita.kvartarena.utils.save

class EncryptedLocalStorage private constructor() {
    companion object {
        private lateinit var sharedPref: SharedPreferences
        private lateinit var instance: EncryptedLocalStorage

        fun init(context: Context) {
            var masterKey: MasterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
//            sharedPref = EncryptedSharedPreferences.create(
//                context, "encryptStorage", masterKey, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
//            )
            sharedPref = context.getSharedPreferences("Storage", Context.MODE_PRIVATE)
            instance = EncryptedLocalStorage()
        }

        fun getInstance() = instance
    }

    var uid: String
        get() = sharedPref.get("uid", "")
        set(value) = sharedPref.save("uid", value)

    var numberF: String
        get() = sharedPref.get("numberF", "")
        set(value) = sharedPref.save("numberF", value)

    var number: String
        get() = sharedPref.get("number", "")
        set(value) = sharedPref.save("number", value)

    var settings: Boolean
        get() = sharedPref.get("password", false)
        set(value) = sharedPref.save("password", value)
    var fullName: String
        get() = sharedPref.get("fullName", "")
        set(value) = sharedPref.save("fullName", value)
    var address: String
        get() = sharedPref.get("address", "")
        set(value) = sharedPref.save("address", value)
    var profile: String
        get() = sharedPref.get("profile", "")
        set(value) = sharedPref.save("profile", value)
}
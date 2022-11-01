package uz.gita.kvartarena.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uz.gita.kvartarena.BuildConfig
import uz.gita.kvartarena.R
import uz.gita.kvartarena.app.App
import uz.gita.kvartarena.data.local.EncryptedLocalStorage
import uz.gita.kvartarena.data.remote.FirebaseRemote

class SplashActivity : AppCompatActivity() {
    private val storage = EncryptedLocalStorage.getInstance()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        CoroutineScope(Dispatchers.IO).launch {
            if (storage.uid == "") {
                delay(3000)
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                finish()
            } else {
                if (storage.settings) {
                    delay(3000)
                    FirebaseRemote.getInstance().getUser(storage.uid) { user ->
                        App.user = user
                        if (user.kid != "") {
                            FirebaseRemote.getInstance().getApart(user.kid.toString()){
                                App.apart=it
                                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            val intent = Intent(this@SplashActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                } else {
                    delay(3000)
                    val intent = Intent(this@SplashActivity, SettingsActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}
package uz.gita.kvartarena.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import uz.gita.kvartarena.R
import uz.gita.kvartarena.app.App
import uz.gita.kvartarena.data.local.EncryptedLocalStorage
import uz.gita.kvartarena.data.remote.FirebaseRemote

class SplashActivity : AppCompatActivity() {
    private val storage = EncryptedLocalStorage.getInstance()
    private val remote = FirebaseRemote.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        if (storage.uid == "") {
            openLogin()
        } else {
            if (!storage.settings) {
                openSettings()
            } else {
                remote.getUser(storage.uid) { user ->
                    App.user = user
                    if (user.kid != "") {
                        remote.getApart(user.kid!!) { apart ->
                            App.apart = apart
                            openMainActivity()
                        }
                    } else {
                        openMainActivity()
                    }
                }
            }
        }
    }

    private fun openLogin() {
        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
        finish()
    }

    private fun openSettings() {
        startActivity(Intent(this@SplashActivity, SettingsActivity::class.java))
        finish()
    }

    private fun openMainActivity() {
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()
    }
}
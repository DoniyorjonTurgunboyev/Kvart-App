package uz.gita.kvartarena.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import uz.gita.kvartarena.R
import uz.gita.kvartarena.data.local.EncryptedLocalStorage
import uz.gita.kvartarena.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val storage = EncryptedLocalStorage.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.enter.setOnClickListener {
            val intent = Intent(this, VerifyActivity::class.java)
            storage.number = "+998" + binding.number.rawText
            storage.numberF = binding.number.text.toString()
            startActivity(intent)
        }
        binding.accept.setOnClickListener { checkEnter() }
        binding.number.addTextChangedListener {
            checkEnter()
            if (binding.number.rawText.length == 9) {
                val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.number.windowToken, 0)
                if (!checkNumber()) {
                    Toast.makeText(this, "Noto'g'ri raqam kiritdingiz", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkNumber(): Boolean {
        val number = binding.number.rawText
        return number.startsWith("91")
                || number.startsWith("90")
                || number.startsWith("33")
                || number.startsWith("94")
                || number.startsWith("99")
                || number.startsWith("93")
                || number.startsWith("77")
                || number.startsWith("88")
                || number.startsWith("75")
                || number.startsWith("95")
                || number.startsWith("97")
    }

    private fun checkEnter() {
        if (checkNumber()) {
            if (binding.accept.isChecked && binding.number.rawText.length == 9) {
                binding.enter.isClickable = true
                binding.enter.setBackgroundResource(R.drawable.controls_bu)
                binding.text.setTextColor(resources.getColor(R.color.white))
                return
            }
        }
        binding.enter.isClickable = false
        binding.enter.setBackgroundResource(R.drawable.login_back)
        binding.text.setTextColor(resources.getColor(R.color.seriy))
    }
}
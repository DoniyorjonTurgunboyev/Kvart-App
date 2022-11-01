package uz.gita.kvartarena.ui.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import uz.gita.kvartarena.R
import uz.gita.kvartarena.app.App
import uz.gita.kvartarena.data.local.SpinnersData
import uz.gita.kvartarena.data.remote.FirebaseRemote
import uz.gita.kvartarena.databinding.ActivityCostBinding
import uz.gita.kvartarena.model.Expense
import uz.gita.kvartarena.model.Member
import uz.gita.kvartarena.ui.adapters.TypesAdapter
import uz.gita.kvartarena.ui.adapters.UserAdapter
import java.text.SimpleDateFormat
import java.util.*

class CostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCostBinding
    private lateinit var adapter: UserAdapter
    private val remote = FirebaseRemote.getInstance()
    private val user = App.user
    private var summa = 0
    private lateinit var progressDialog: ProgressDialog
    private var count = 0
    private var save = false
    private val time = Calendar.getInstance().time
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadRv()
        loadTime()
        costListener()
        binding.spinner.setOnClickListener { loadTypeSpinner() }
        binding.saqla.setOnClickListener {
            if (save)
                save()
        }
        binding.back.setOnClickListener { finish() }
    }

    private fun save() {
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Ma'lumotlar yuklanmoqda....")
        progressDialog.show()
        progressDialog.setCancelable(false)
        val type = binding.text.text.toString()
        val list = ArrayList<Member>()
        adapter.currentList.filter { it.checked }.forEach {
            list.add(Member(it.uid, it.name))
        }
        val expense = Expense(time.time.toString(), binding.comment.text.toString().trim(), summa, remote.auth.uid!!, user.name!!, type)
        remote.addExpense(user.kid.toString(), expense, list) {
            progressDialog.dismiss()
            Toast.makeText(this, "Accept", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun generate() {
        if (count == 0 || summa == 0) {
            binding.saqla.isClickable = false
            save = false
            binding.saqla.setBackgroundResource(R.drawable.login_back)
            binding.image.setImageResource(R.drawable.ic_save2)
            binding.saqlash.setTextColor(resources.getColor(R.color.seriy))
        } else {
            save = true
            binding.saqla.isClickable = true
            binding.saqla.setBackgroundResource(R.drawable.controls_bu)
            binding.image.setImageResource(R.drawable.ic_save)
            binding.saqlash.setTextColor(resources.getColor(R.color.white))
        }
        if (count == 0) {
            binding.generate.text = "Avval a'zolarni tanlang"
        } else if (summa == 0) {
            binding.generate.text = "Avval xarajatlar summasini kiriting"
        } else {
            val sum = String.format(Locale.US, "%,d", summa / count).replace(",", " ")
            binding.generate.text = Html.fromHtml("Tanlangan $count ta kvartira a'zosining har biriga <b>$sum UZS</b> dan")
        }
    }

    private fun loadRv() {
        binding.rv.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(this)
        remote.getMembersByApartId {
            adapter.submitList(it)
        }
        binding.rv.adapter = adapter
        adapter.setOnCheckListener {
            count++
            generate()
        }
        adapter.setUnCheckListener {
            count--
            generate()
        }
    }

    private fun loadTypeSpinner() {
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.bottomsheetdialog)
        bottomSheetDialog.findViewById<TextView>(R.id.textView2)!!.text = "Xarajat turini tanlang"
        val rv = bottomSheetDialog.findViewById<RecyclerView>(R.id.rvLocation)!!
        val adapter = TypesAdapter()
        rv.layoutManager = LinearLayoutManager(this)
        adapter.submitList(SpinnersData.getAll())
        rv.adapter = adapter
        adapter.setListener {
            binding.text.text = it.type
            binding.imageView.setImageResource(it.icon)
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    private fun costListener() {
        val b = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                binding.xarajat.removeTextChangedListener(this)
                var originalString = s.toString()
                if (originalString.contains(" ")) {
                    originalString = originalString.replace(" ".toRegex(), "")
                }

                val intVal = if (originalString != "") originalString.toInt() else 0
                summa = intVal
                binding.xarajat.setText(String.format(Locale.US, "%,d", intVal).replace(",", " "))
                binding.xarajat.setSelection(binding.xarajat.text.toString().length)
                generate()
                binding.xarajat.addTextChangedListener(this)
            }
        }
        binding.xarajat.addTextChangedListener(b)
    }

    private fun loadTime() {
        val calendar = time
        val locale = Locale("uz", "UZ")
        val year = SimpleDateFormat("yyyy", locale).format(calendar).toString()
        val monthName = SimpleDateFormat("MMMM", locale).format(calendar).toString()
        val day = SimpleDateFormat("dd", locale).format(calendar).toString()
        val weekDayName = SimpleDateFormat("EEEE", locale).format(calendar).toString()
        val time = SimpleDateFormat("HH:mm:ss", locale).format(calendar).toString()
        val timeText = "$year-yil $day-$monthName, $weekDayName, $time"
        binding.time.setText(timeText)
    }
}
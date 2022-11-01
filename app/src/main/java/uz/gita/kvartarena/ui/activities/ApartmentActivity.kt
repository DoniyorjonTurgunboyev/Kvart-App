package uz.gita.kvartarena.ui.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import uz.gita.kvartarena.R
import uz.gita.kvartarena.app.App
import uz.gita.kvartarena.data.remote.FirebaseRemote
import uz.gita.kvartarena.databinding.ActivityApartmentBinding
import uz.gita.kvartarena.model.Apartment
import uz.gita.kvartarena.ui.adapters.GenerateTypeAdapter
import uz.gita.kvartarena.ui.adapters.GenerateUserAdapter
import java.util.*

class ApartmentActivity : AppCompatActivity() {
    private lateinit var apartment: Apartment
    private val remote = FirebaseRemote.getInstance()
    private lateinit var progressDialog: ProgressDialog
    private lateinit var adapter: GenerateTypeAdapter
    private lateinit var adapterUser: GenerateUserAdapter
    private lateinit var binding: ActivityApartmentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApartmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Ma'lumotlar yuklanmoqda....")
        progressDialog.setCancelable(false)
        progressDialog.show()
        onResume()
        apartment = App.apart
        binding.name.text = apartment.name + " kvartirasi"
        binding.add.setOnClickListener {
            startActivity(Intent(this, CostActivity::class.java))
        }
        binding.swipe.setOnRefreshListener {
            onResume()
        }
        binding.expandX.setOnClickListener {
            binding.rvType.apply {
                if (this.visibility == View.VISIBLE) {
                    visibility = View.GONE
                    TransitionManager.beginDelayedTransition(binding.cardView, AutoTransition().setInterpolator(null).setStartDelay(500).setDuration(500))
                    TransitionManager.beginDelayedTransition(binding.cardView2, AutoTransition().setDuration(500).setInterpolator(null).setStartDelay(300))
                    binding.m.setImageResource(R.drawable.ic_more)
                } else {
                    TransitionManager.beginDelayedTransition(binding.cardView, AutoTransition().setDuration(500))
                    TransitionManager.beginDelayedTransition(binding.cardView2, AutoTransition().setInterpolator(null).setDuration(500))
                    visibility = View.VISIBLE
                    binding.m.setImageResource(R.drawable.ic_less)
                }
            }
        }
        binding.expandU.setOnClickListener {
            binding.rvUser.apply {
                if (this.visibility == View.VISIBLE) {
                    TransitionManager.beginDelayedTransition(binding.cardView2, AutoTransition().setInterpolator(null).setStartDelay(500).setDuration(500))
                    visibility = View.GONE
                    binding.u.setImageResource(R.drawable.ic_more)
                } else {
                    TransitionManager.beginDelayedTransition(binding.cardView2, AutoTransition().setInterpolator(null).setStartDelay(500).setDuration(500))
                    visibility = View.VISIBLE
                    binding.u.setImageResource(R.drawable.ic_less)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
//        progressDialog.show()
        remote.getExpenses {
            val list = it
            var all = 0
            list.forEach {
                all += it.amount
            }
            binding.rvType.layoutManager = LinearLayoutManager(this)
            adapter = GenerateTypeAdapter(all)
            val sum = String.format(Locale.US, "%,d", all).replace(",", " ")
            binding.all.text = "$sum UZS"
            binding.rvType.adapter = adapter
            adapter.submitList(list.sortedBy { -it.amount })
            binding.swipe.isRefreshing = false
        }
        remote.getMemberExpanses {
            binding.rvUser.layoutManager = LinearLayoutManager(this)
            adapterUser = GenerateUserAdapter()
            adapterUser.setSmooth {
                Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
                binding.rvUser.smoothScrollToPosition(adapterUser.currentList.size-1)
            }
            adapterUser.setListener { uid ->
                progressDialog.setTitle("Ma'lumotlar yuklanmoqda....")
                progressDialog.setCancelable(false)
                progressDialog.show()
                FirebaseRemote.getInstance().getUser(uid) {
                    progressDialog.dismiss()
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("user", it)
                    intent.putExtra("uid", uid)
                    startActivity(intent)
                }
            }
            progressDialog.dismiss()
            binding.rvUser.adapter = adapterUser
            adapterUser.submitList(it.sortedBy { -it.amount })
        }
    }
}
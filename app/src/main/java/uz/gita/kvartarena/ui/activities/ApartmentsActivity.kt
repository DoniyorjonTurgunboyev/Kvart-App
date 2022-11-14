package uz.gita.kvartarena.ui.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import uz.gita.kvartarena.app.App
import uz.gita.kvartarena.data.remote.FirebaseRemote
import uz.gita.kvartarena.databinding.ActivityApartmentsBinding
import uz.gita.kvartarena.model.Apartment
import uz.gita.kvartarena.ui.adapters.ApartAdapter

class ApartmentsActivity : AppCompatActivity() {
    private lateinit var progressDialog: ProgressDialog
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var binding: ActivityApartmentsBinding
    private lateinit var adapter: ApartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApartmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Ma'lumotlar yuklanmoqda....")
        progressDialog.show()
        progressDialog.setCancelable(false)
        binding.rv.addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                binding.rv.smoothScrollToPosition(newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

            }
        })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.rv.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                Log.d("RRR", "onScrollStateChanged: $scrollX")
                Log.d("RRR", "onScrollStateChanged: $oldScrollX")
                Log.d("RRR", "onScrollStateChanged: $scrollY")
                Log.d("RRR", "onScrollStateChanged: $oldScrollY")
            }
        }
        binding.rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = ApartAdapter()
        val user = App.user
        binding.back.setOnClickListener { finish() }
        binding.rv.adapter = adapter
        refresh()
        binding.back.setOnClickListener {
            finish()
        }
        binding.swip.setOnRefreshListener {
            refresh()
            adapter.notifyDataSetChanged()
        }
        adapter.setListener {
            val builder = AlertDialog.Builder(this)
            val exist = user.kid == ""
            if (!exist) {
                Toast.makeText(this, "Sizda avvaldan qo'shilgan\nkvartira mavjud", Toast.LENGTH_SHORT).show()
                return@setListener
            }
            builder.setTitle("Tasdiqlang")
            builder.setCancelable(false)
            builder.setMessage("" + it.address + " da joylashgan " + it.owner + " ga qarashli " + it.name + " kvartirasiga qo'shiasizmi?")
            builder.setPositiveButton("Ha albatta") { _, _ ->
                FirebaseRemote.getInstance().addMemberToApartment(it.uid!!)
                App.user.kid = it.uid
                App.initUser()
                App.initApart()
            }
            builder.setNegativeButton("Yo'q") { dialog, _ -> dialog.dismiss() }
            builder.create().show()
        }
    }

    private fun refresh() {
        firebaseDatabase.getReference("Apartments").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<Apartment>()
                snapshot.children.forEach { apart ->
                    val uid = apart.key!!
                    val name = apart.child("name").value.toString()
                    val address = apart.child("address").value.toString()
                    val owner = apart.child("owner").value.toString()
                    val bio = apart.child("bio").value.toString()
                    val ownername = apart.child("ownername").value.toString()
                    list.add(Apartment(uid, name, address, owner, ownername, bio))
                    list.add(Apartment(uid, name, address, owner, ownername, bio))
                    list.add(Apartment(uid, name, address, owner, ownername, bio))
                    list.add(Apartment(uid, name, address, owner, ownername, bio))
                    list.add(Apartment(uid, name, address, owner, ownername, bio))
                    list.add(Apartment(uid, name, address, owner, ownername, bio))
                    list.add(Apartment(uid, name, address, owner, ownername, bio))
                    list.add(Apartment(uid, name, address, owner, ownername, bio))
                }
                adapter.submitList(list)
                adapter.notifyDataSetChanged()
                progressDialog.dismiss()
                binding.swip.isRefreshing = false
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}
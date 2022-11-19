package uz.gita.kvartarena.ui.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.graphics.PointF
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.*
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import uz.gita.kvartarena.R
import uz.gita.kvartarena.app.App
import uz.gita.kvartarena.data.remote.FirebaseRemote
import uz.gita.kvartarena.databinding.ActivityApartmentsBinding
import uz.gita.kvartarena.model.Apartment
import uz.gita.kvartarena.ui.adapters.ApartAdapter


class ApartmentsActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var mapKit: MapKit
    private lateinit var progressDialog: ProgressDialog
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var mapObjectCollection: MapObjectCollection
    private lateinit var binding: ActivityApartmentsBinding
    private var position = Integer.MIN_VALUE
    private lateinit var adapter: ApartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApartmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Ma'lumotlar yuklanmoqda....")
        progressDialog.show()
        progressDialog.setCancelable(false)
        loadMap()
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rv)
        binding.rv.smoothScrollBy(5, 0)
        binding.rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val centerView: View = snapHelper.findSnapView(binding.rv.layoutManager)!!
                    val pos: Int = (binding.rv.layoutManager as LinearLayoutManager).getPosition(centerView)
                    val size = adapter.currentList.size
                    if (size > pos % size && position != pos % size) {
                        position = pos % size
                        mapView.map.move(CameraPosition(Point(adapter.currentList[position].lat!!, adapter.currentList[position].long!!), 16f, 0f, 0f), Animation(Animation.Type.SMOOTH, 2f), null)
                    }
                }
            }
        })
        adapter = ApartAdapter()
        val user = App.user
        binding.back.setOnClickListener { finish() }
        binding.rv.adapter = adapter
        refresh()
        binding.back.setOnClickListener {
            finish()
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
                FirebaseRemote.getInstance().updateUser(App.user)
                App.initUser()
                App.initApart()
            }
            builder.setNegativeButton("Yo'q") { dialog, _ -> dialog.dismiss() }
            builder.create().show()
        }
        binding.rv.scrollToPosition(Integer.MAX_VALUE / 2)
    }

    private fun loadMap() {
        mapKit = MapKitFactory.getInstance()
        mapView = binding.map
        mapKit.onStart()
        mapView.onStart()
        mapObjectCollection = mapView.map.mapObjects.addCollection()
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
                    val lat = apart.child("lat").value.toString().toDouble()
                    val long = apart.child("long").value.toString().toDouble()
                    mapView.map.mapObjects.addPlacemark(Point(lat, long))
                    val mark: PlacemarkMapObject = mapView.map.mapObjects.addCollection().addPlacemark(Point(lat, long))
                    mark.opacity = 0.5f
                    val icon = mark.useCompositeIcon()
                    icon.setIcon(
                        "icon", ImageProvider.fromResource(this@ApartmentsActivity, R.drawable.apartment), IconStyle().setAnchor(PointF(0.5f, 0.5f))
                            .setRotationType(RotationType.ROTATE)
                            .setZIndex(1f)
                            .setScale(0.5f)
                    )
                    list.add(Apartment(uid, name, address, lat, long, owner, ownername, bio))
                }
                adapter.submitList(list)
                adapter.notifyDataSetChanged()
                progressDialog.dismiss()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}
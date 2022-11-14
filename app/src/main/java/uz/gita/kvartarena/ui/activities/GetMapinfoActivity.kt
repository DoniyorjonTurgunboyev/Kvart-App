package uz.gita.kvartarena.ui.activities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.animation.Transformation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import retrofit2.Response
import uz.gita.kvartarena.data.remote.GeoCoderRetrofit
import uz.gita.kvartarena.databinding.ActivityGetMapinfoBinding
import uz.gita.kvartarena.model.geocoder.GeoApi


class GetMapinfoActivity : AppCompatActivity(), CameraListener {
    private val PERMISSIONS_REQUEST_FINE_LOCATION = 1
    private lateinit var userLocationLayer: UserLocationLayer
    private lateinit var mapView: MapView
    private var margin = 4
    private lateinit var mapKit: MapKit
    private var s = true
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var lat: Double = 0.0
    private var long: Double = 0.0
    private var locationByGps: Location? = null
    private var locationByNetwork: Location? = null
    private lateinit var currentLocation: Location
    lateinit var locationManager: LocationManager
    private val geoService = GeoCoderRetrofit.getRetrofit()
    private lateinit var binding: ActivityGetMapinfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetMapinfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadMap()
        binding.myLocation.setOnClickListener { myLocation() }
    }

    private fun loadMap() {
        mapKit = MapKitFactory.getInstance()
        mapView = binding.map
        val param = binding.imageView3.layoutParams as ViewGroup.MarginLayoutParams
        margin = param.bottomMargin
        requestLocationPermission()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        val gpsLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationByGps = location
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        val networkLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationByNetwork = location
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        mapView.map.addCameraListener(this)
        userLocationLayer = mapKit.createUserLocationLayer(mapView.mapWindow)
        userLocationLayer.isVisible = true
        if (hasGps) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0F, gpsLocationListener)
        }

        if (hasNetwork) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0F, networkLocationListener)
        }
        val lastKnownLocationByGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        lastKnownLocationByGps?.let { locationByGps = lastKnownLocationByGps }

        val lastKnownLocationByNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        lastKnownLocationByNetwork?.let { locationByNetwork = lastKnownLocationByNetwork }
        myLocation()
    }

    private fun myLocation() {
        if (locationByGps != null && locationByNetwork != null) {
            if (locationByGps!!.accuracy > locationByNetwork!!.accuracy) {
                currentLocation = locationByGps!!
                latitude = currentLocation.latitude
                longitude = currentLocation.longitude
            } else {
                currentLocation = locationByNetwork!!
                latitude = currentLocation.latitude
                longitude = currentLocation.longitude
            }
            mapView.map.move(CameraPosition(Point(latitude, longitude), 16f, 0.45f, 0f), Animation(Animation.Type.SMOOTH, 2f), null)
        }
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    private fun requestLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf("android.permission.ACCESS_FINE_LOCATION"), PERMISSIONS_REQUEST_FINE_LOCATION)
        }
        return true
    }

    override fun onCameraPositionChanged(p0: Map, p1: CameraPosition, p2: CameraUpdateReason, p3: Boolean) {
        Log.d("TTT", "onCameraPositionChanged: ")
        val dpRatio: Float = this.resources.displayMetrics.density
        if (p3) {
            s = true
            val animation = object : android.view.animation.Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    val param = binding.imageView3.layoutParams as ViewGroup.MarginLayoutParams
                    param.bottomMargin = (12 * dpRatio - 8 * interpolatedTime * dpRatio).toInt()
                    Log.d("FFF", "applyTransformation: $interpolatedTime")
                    binding.imageView3.layoutParams = param
                }
            }
            animation.duration = 200
            binding.imageView3.startAnimation(animation)
            val animation2 = object : android.view.animation.Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    val param = binding.imageView4.layoutParams
                    param.width = ((16 + 16 * interpolatedTime) * dpRatio).toInt()
                    param.height = ((4 + 4 * interpolatedTime) * dpRatio).toInt()
                    binding.imageView4.alpha = 100 - 50 * interpolatedTime
                }
            }
            animation2.duration = 200
            binding.imageView4.startAnimation(animation2)
        } else
            if (s) {
                s = false
                val animation = object : android.view.animation.Animation() {
                    override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                        val param = binding.imageView3.layoutParams as ViewGroup.MarginLayoutParams
                        param.bottomMargin = (4 * dpRatio + 8 * interpolatedTime * dpRatio).toInt()
                        binding.imageView3.layoutParams = param
                        Log.d("FFF", "applyTransformation: $interpolatedTime")
                    }
                }
                animation.duration = 200
                binding.imageView3.startAnimation(animation)
                val animation2 = object : android.view.animation.Animation() {
                    override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                        val param = binding.imageView4.layoutParams
                        param.width = ((32 - 16 * interpolatedTime) * dpRatio).toInt()
                        param.height = ((8 - 4 * interpolatedTime) * dpRatio).toInt()
                        binding.imageView4.alpha = 50 + 50 * interpolatedTime
                    }
                }
                animation2.duration = 200
                binding.imageView4.startAnimation(animation2)
            }
        if (p3) {
            lat = p1.target.latitude
            long = p1.target.longitude
        }
    }

    private fun geocoder() {
        val geo = "$long,$lat"
        val call: retrofit2.Call<GeoApi> = geoService.get("json", "5ab3428d-3078-4c73-ae82-e66ee5f1cd74", "uz", geo)
        call.enqueue(object : retrofit2.Callback<GeoApi> {
            override fun onResponse(call: retrofit2.Call<GeoApi>, response: Response<GeoApi>) {
                val t = response.body().toString()
            }

            override fun onFailure(call: retrofit2.Call<GeoApi>, t: Throwable) {

            }
        })
    }
}
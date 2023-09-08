package com.furkanharmanci.kotlintravelbook.view

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.furkanharmanci.kotlintravelbook.R
import com.furkanharmanci.kotlintravelbook.databinding.ActivityMapsBinding
import com.furkanharmanci.kotlintravelbook.model.Place
import com.furkanharmanci.kotlintravelbook.roomdb.PlaceDao
import com.furkanharmanci.kotlintravelbook.roomdb.PlaceDatabase
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var sharedPreferences: SharedPreferences
    private val projectPackageName = "com.furkanharmanci.kotlintravelbook"
    private var trackBoolean : Boolean? = null
    private val sharedData : String = "trackBoolean"
    private var selectedLatitude : Double? = null
    private var selectedLongitude : Double? = null
    private lateinit var db : PlaceDatabase
    private lateinit var placeDao : PlaceDao
    private val compositeDisposable = CompositeDisposable()
    private lateinit var placeFromMain: Place

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        registerLauncher()
        sharedPreferences = getSharedPreferences(projectPackageName, MODE_PRIVATE)
        trackBoolean = false
        selectedLatitude = 0.0
        selectedLongitude = 0.0
        db = Room.databaseBuilder(applicationContext,PlaceDatabase::class.java, "Places").build()
        placeDao = db.placeDao()
        binding.saveButton.isEnabled = false
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this) // harita ile longClicklistener arasındaki bağlantı

        val intent = intent
        val info = intent.getStringExtra("info")

        if (info.equals("newPosition")) {
            binding.saveButton.visibility = View.VISIBLE
            binding.deleteButton.visibility = View.GONE

            locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

            locationListener = LocationListener { location ->
                trackBoolean = sharedPreferences.getBoolean(sharedData, false)
                if (trackBoolean == false) {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                    sharedPreferences.edit().putBoolean(sharedData, true).apply()
                }
            }

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this@MapsActivity, Manifest.permission.ACCESS_FINE_LOCATION) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this@MapsActivity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Snackbar.make(binding.root, "Permission needed for location",Snackbar.LENGTH_INDEFINITE)
                        .setAction("Give Permission") {
                            // request permission
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                        }.show()
                } else {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                }
            } else {
                // permission granted!
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000, 10f,locationListener)
                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastLocation != null) {
                    val lastUserKnownLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserKnownLocation, 15f))
                }
                mMap.isMyLocationEnabled = true
            }
        } else {
            mMap.clear()
            placeFromMain = intent.getSerializableExtra("selectedPlace") as Place
            placeFromMain.let {
                val latLang = LatLng(it.latitude,it.longitude)
                mMap.addMarker(MarkerOptions().position(latLang).title(it.name))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLang, 15f))
                binding.place.setText(it.name)
                binding.saveButton.visibility = View.GONE
                binding.deleteButton.visibility = View.VISIBLE
            }
        }

    }

    private fun registerLauncher() {
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {result ->
            if (result) {
                //permission granted!
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000, 10f,locationListener)
                    val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (lastLocation != null) {
                        val lastUserKnownLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserKnownLocation, 15f))
                    }
                }
            } else {
                //permission denied :(
                Toast.makeText(this@MapsActivity, "Permission Needed!", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onMapLongClick(longClick: LatLng) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(longClick))

        selectedLatitude = longClick.latitude
        selectedLongitude = longClick.longitude

        binding.saveButton.isEnabled = true
    }

    fun save(view: View) {
        val placeName = binding.place.text.toString()

        if (selectedLatitude != null && selectedLongitude != null) {
            val place = Place(placeName, selectedLatitude!!, selectedLongitude!!)
            compositeDisposable.add(
                placeDao.insert(place)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse)
            )
        }
    }
    private fun handleResponse() {
        val intent = Intent(this@MapsActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
    fun delete(view: View) {
        placeFromMain?.let {
            compositeDisposable.add(
                placeDao.delete(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse)
            )
        }
    }
}
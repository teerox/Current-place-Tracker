package com.example.maplocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.maplocation.model.Distance
import com.example.maplocation.model.LocationDatabase
import com.example.maplocation.model.StartLocation
import com.example.maplocation.model.StopLocation
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.math.*


class MapLocation : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var longitude:String
    private lateinit var latitude:String
    private var mMap: GoogleMap? = null
    private lateinit var database:LocationDatabase
    private var mCameraPosition: CameraPosition? = null
    // The entry point to the Places API.
    private var mPlacesClient: PlacesClient? = null
    // The entry point to the Fused Location Provider.

    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private val mDefaultLocation = LatLng(-33.8523341, 151.2106085)
    private var mLocationPermissionGranted = false
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var mLastKnownLocation: Location? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = LocationDatabase.getInstance(applicationContext)!!
        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_main)
        // Construct a PlacesClient
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        mPlacesClient = Places.createClient(this)
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        // Build the map.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        //BUTTONS
        btn_start_location_updates.setOnClickListener {
            deviceLocation
            btn_start_location_updates.visibility = View.GONE
            btn_stop_location_updates.visibility = View.VISIBLE
            GlobalScope.async(Dispatchers.IO) {
                val startlocation = StartLocation(longitude,latitude)
                database.StartLocationDao().insertAllstart(startlocation)
                startlocation
            }
        }

        btn_stop_location_updates.setOnClickListener {
            deviceLocation
            btn_stop_location_updates.visibility = View.GONE
            btn_get_distance_covered.visibility = View.VISIBLE
            GlobalScope.async(Dispatchers.IO) {

                val stoplocation = StopLocation(longitude,latitude)
                database.StopLocationDao().insertAllstop(stoplocation)
                stoplocation
            }
        }

        btn_get_distance_covered.setOnClickListener {
            btn_get_distance_covered.visibility = View.GONE
            btn_start_location_updates.visibility = View.VISIBLE
            GlobalScope.async(Dispatchers.IO) {
                val startlocation = getStartLocation()
                val stoplocation = getStopLocation()
                if (startlocation.size > 0 && stoplocation.size > 0){
                    var srtloc =  startlocation[0]
                    var latitude1 =  srtloc.latitude.toDouble()
                    var longitude1 =  srtloc.longitude.toDouble()
                    var stoploc = stoplocation[0]
                    var latitude2 =  stoploc.latitude.toDouble()
                    var longitude2 =  stoploc.longitude.toDouble()
                    var result = getDistanceFromLatLonInKm(latitude1,longitude1,latitude2,longitude2)
                    val resultFinale= String.format("%.3f", result).toDouble()
                    Log.e("Distance", resultFinale.toString())
                    var distance = Distance(resultFinale.toString())
                    database.DistanceDao().insertDistance(distance)
                }
            }
            GlobalScope.launch(Dispatchers.Main) {
                var allDistance = getdistance()
                if(allDistance.size > 0){
                    var allDis = allDistance[0]
                    var finalResult = allDis.distance
                    Log.e("Dist", finalResult)
                    distance_result.text = "$finalResult KM"
                }
            }
        }

        btn_get_last_location.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                var lastloc =  getStopLocation()
                var lastlocation = lastloc[0]
                var lastlocationLongitude = lastlocation.longitude
                var lastlocationLatitude = lastlocation.latitude
                Toast.makeText(this@MapLocation,"latitude $lastlocationLatitude \nlongitude $lastlocationLongitude",Toast.LENGTH_LONG).show()

            }

        }


    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap!!.cameraPosition)
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation)
            super.onSaveInstanceState(outState)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map
        // Prompt the user for permission.
        getLocationPermission()
        updateLocationUI()
       // deviceLocation
    }

    //DEVICE LOCATION
    private val deviceLocation: Unit
        get() {
            try {
                if (mLocationPermissionGranted) {
                    val locationResult = mFusedLocationProviderClient!!.lastLocation
                    locationResult.addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) { // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.result
                            if (mLastKnownLocation != null) {
                                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        LatLng(mLastKnownLocation!!.latitude,
                                                mLastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))

                                Log.e("Latitude", mLastKnownLocation!!.latitude.toString())
                                Log.e("Longitude",mLastKnownLocation!!.longitude.toString())

                                latitude = mLastKnownLocation!!.latitude.toString()
                               longitude = mLastKnownLocation!!.longitude.toString()

                                //SUPPOSED TO ADD TO DB HERE FOR START AND STOP
                                location_result.text = latitude
                                updated_on.text = longitude

                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.")
                            Log.e(TAG, "Exception: %s", task.exception)
                            mMap!!.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM.toFloat()))
                            mMap!!.uiSettings.isMyLocationButtonEnabled = false
                        }
                    }
                }
            } catch (e: SecurityException) {
                Log.e("Exception: %s", e.message.toString())
            }
        }

    private fun getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.applicationContext,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }

    private fun updateLocationUI() {
        if (mMap == null) {
            return
        }
        try {
            if (mLocationPermissionGranted) {
                mMap!!.isMyLocationEnabled = true
                mMap!!.uiSettings.isMyLocationButtonEnabled = true
            } else {
                mMap!!.isMyLocationEnabled = false
                mMap!!.uiSettings.isMyLocationButtonEnabled = false
                mLastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message.toString())
        }
    }

    companion object {
        private val TAG = MapLocation::class.java.simpleName
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"

    }

    private suspend fun getStartLocation():ArrayList<StartLocation>{
        return GlobalScope.async(Dispatchers.IO){
            val dbstartLocation  = database.StartLocationDao().getAllstartlonglat() as ArrayList<StartLocation>
            dbstartLocation
        }.await()
    }

    private suspend fun getStopLocation():ArrayList<StopLocation> {
        return GlobalScope.async(Dispatchers.IO) {
            val dbstopLocation = database.StopLocationDao().getAllstoplonglat() as ArrayList<StopLocation>
            dbstopLocation
        }.await()
    }

    private suspend fun getdistance():ArrayList<Distance>{
        return GlobalScope.async(Dispatchers.IO) {
            val alldistance = database.DistanceDao().getDistance() as ArrayList<Distance>
            Log.e("check", alldistance.toString())
            alldistance
        }.await()
    }



    fun getDistanceFromLatLonInKm(lat1:Double,lon1:Double,lat2:Double,lon2:Double):Double {
        var radius = 6371 // Radius of the earth in km
        var dLat = deg2rad(lat2-lat1) // deg2rad below
        var dLon = deg2rad(lon2-lon1)
        var a =
                sin(dLat/2) * sin(dLat/2) +
                        cos(deg2rad(lat1)) * cos(deg2rad(lat2)) *
                        sin(dLon/2) * sin(dLon/2)
        ;
        var c = 2 * atan2(sqrt(a), sqrt(1-a))
        return radius* c
    }

    fun deg2rad(deg:Double):Double {
        return deg * (Math.PI/180)
    }

}
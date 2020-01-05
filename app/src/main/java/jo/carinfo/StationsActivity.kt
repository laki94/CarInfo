package jo.carinfo

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.AutoCompleteTextView
import androidx.constraintlayout.widget.Placeholder
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.Marker
import com.google.android.libraries.places.compat.*
import com.google.android.libraries.places.compat.ui.PlaceAutocomplete
import com.google.android.material.floatingactionbutton.FloatingActionButton

class StationsActivity : FragmentActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var mFusedLocationProvider: FusedLocationProviderClient
    private lateinit var mLastLocation: Location
    private lateinit var mLocationCallback: LocationCallback
    private lateinit var mLocationRequest: LocationRequest
    private var mLocationUpdateState = false
    private lateinit var mStations: StationList
    private lateinit var mCore: Core

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stations)

        val extras = intent.extras
        if (extras != null)
            if (extras.containsKey("core"))
                mCore = extras.getSerializable("Core") as Core

        val mapFragment = supportFragmentManager.findFragmentById(R.id.fMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mFusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                mLastLocation = p0.lastLocation
                placeMarkerOnMap(LatLng(mLastLocation.latitude, mLastLocation.longitude))
            }
        }
        createLocationRequest()

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            loadPlacePicker()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CHECK_SETTING) {
            if (resultCode == Activity.RESULT_OK) {
                mLocationUpdateState = true
                startLocationUpdates()
            }
        } else if (requestCode == PLACE_PICKER_REQ) {
            if (resultCode == RESULT_OK) {
                val place = PlaceAutocomplete.getPlace(this, data)
                var addressText = place.name.toString()
                addressText += "\n" + place.address.toString()
                placeMarkerOnMap(place.latLng)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mFusedLocationProvider.removeLocationUpdates(mLocationCallback)
    }

    override fun onResume() {
        super.onResume()
        if (!mLocationUpdateState)
            startLocationUpdates()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (permissions.indexOf(android.Manifest.permission.ACCESS_FINE_LOCATION) != -1)
            if ((requestCode == LOCATION_PERMISSION_REQ_CODE) && (grantResults[permissions.indexOf(android.Manifest.permission.ACCESS_FINE_LOCATION)] == PackageManager.PERMISSION_GRANTED))
                setUpMap()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        return false
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(this)
        setUpMap()
    }

    override fun onMapClick(p0: LatLng) {
        mMap.clear()
        placeMarkerOnMap(p0)
    }

    private fun placeMarkerOnMap(aLocation: LatLng) {
        val markerOptions = MarkerOptions().position(aLocation)
        mMap.addMarker(markerOptions)
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 10000
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest)

        val client = LocationServices.getSettingsClient(this)

        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            mLocationUpdateState = true
            startLocationUpdates()
        }

        task.addOnFailureListener {e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(this@StationsActivity, REQ_CHECK_SETTING)
                } catch (ex: IntentSender.SendIntentException) { }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQ_CODE)
            return
        }
        else
            mFusedLocationProvider.requestLocationUpdates(mLocationRequest, mLocationCallback, null)
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQ_CODE)
            return
        } else {
            mMap.isMyLocationEnabled = true
            mFusedLocationProvider.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) {
                    mLastLocation = location
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    placeMarkerOnMap(currentLatLng)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                }
            }
        }
    }

    private fun loadPlacePicker() { // NIE POKAZYWAC MIEJSC TYLKO NA PRZYCISKU DODAC ZAPIS PUNKTU PO KLIKNIECIU I ZAKRES

        val builder = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
            .setFilter(AutocompleteFilter.Builder().setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                .setCountry("PL")
                .build())
            .setInitialQuery("orlen")
            .build(this)
//        val filter = AutocompleteFilter.Builder().
//        builder.setFilter() //PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(this)
        try {
            startActivityForResult(builder, PLACE_PICKER_REQ)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQ_CODE = 1
        private const val REQ_CHECK_SETTING = 2
        private const val PLACE_PICKER_REQ = 3
    }
}

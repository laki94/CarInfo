package jo.carinfo

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.opengl.Visibility
import android.os.Bundle
import android.text.Editable
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.Placeholder
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.compat.*
import com.google.android.libraries.places.compat.ui.PlaceAutocomplete
import com.google.android.material.floatingactionbutton.FloatingActionButton
import yuku.ambilwarna.AmbilWarnaDialog

class StationsActivity : FragmentActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var mFusedLocationProvider: FusedLocationProviderClient
    private lateinit var mLastLocation: Location
    private lateinit var mLocationCallback: LocationCallback
    private lateinit var mLocationRequest: LocationRequest
    private var mLocationUpdateState = false
    private val mStations = StationList()
    private lateinit var mLastClickedPlace: LatLng
    private var mTmpStation = Station()
    private var mLastClickedStation: Station? = null
//    private var mLastClickedStation: Station? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stations)

        val extras = intent.extras
        if (extras != null)
            if (extras.containsKey("stations"))
                for (station in extras.getSerializable("stations") as ArrayList<Station>) {
                    mStations.add(station)
                }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.fMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mFusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                mLastLocation = p0.lastLocation
//                placeMarkerOnMap(LatLng(mLastLocation.latitude, mLastLocation.longitude))
            }
        }
        createLocationRequest()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    fun onRemoveStationClick(view: View) {
        if (mLastClickedStation != null) {
            val cfgManager = ConfigManager(this)
            if (cfgManager.removeStation(mLastClickedStation!!)) {
                mLastClickedStation!!.disconnectFromMap()
                mStations.remove(mLastClickedStation!!)

                val bSavePoint = findViewById<Button>(R.id.bAddEditStation)
                val bRemovePoint = findViewById<Button>(R.id.bRemoveStation)

                bSavePoint.visibility = View.INVISIBLE
                bRemovePoint.visibility = View.INVISIBLE
            }
        }
    }

    fun onSaveStationClick(view: View) {
        val editing = mLastClickedStation != null
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dlg_add_station, null)
        val etStationName = dialogLayout.findViewById<EditText>(R.id.etStationName)
        val etRadius = dialogLayout.findViewById<EditText>(R.id.etRadius)
        builder.setPositiveButton(R.string.save) { _, _ -> }
        val tvLatLng = dialogLayout.findViewById<TextView>(R.id.tvLatLon)

        val title = TextView(this)
        if (editing) {
            if (mLastClickedStation != null) {
                title.text = resources.getString(R.string.editingStation)
                etRadius.setText(mLastClickedStation?.mRadius.toString())
                etStationName.setText(mLastClickedStation?.mName)
                val latDir = if (mLastClickedStation!!.mLat > 0) "N" else "S"
                val lonDir = if (mLastClickedStation!!.mLon > 0) "E" else "W"
                tvLatLng.text = String.format(
                    "%.6f%s %.6f%s",
                    Math.abs(mLastClickedStation!!.mLat),
                    latDir,
                    Math.abs(mLastClickedStation!!.mLon),
                    lonDir
                )
            }
        }
        else {
            title.text = resources.getString(R.string.addingStation)
            val latDir = if (mLastClickedPlace.latitude > 0) "N" else "S"
            val lonDir = if (mLastClickedPlace.longitude > 0) "E" else "W"
            tvLatLng.text = String.format("%.6f%s %.6f%s", Math.abs(mLastClickedPlace.latitude), latDir, Math.abs(mLastClickedPlace.longitude), lonDir)
        }
        title.gravity = Gravity.CENTER
        title.textSize = 20f
        builder.setCustomTitle(title)
        builder.setView(dialogLayout)
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val cfgManager = ConfigManager(this)
            if (editing) {
                mLastClickedStation!!.mName = etStationName.text.toString()
                mLastClickedStation!!.mRadius = Integer.parseInt(etRadius.text.toString())
                if (cfgManager.editStation(mLastClickedStation!!)) {
                    mLastClickedStation!!.disconnectFromMap()
                    mLastClickedStation!!.connectToMap(mMap)
                    dialog.dismiss()
                } else
                    Toast.makeText(
                        this,
                        resources.getString(R.string.savingPointError),
                        Toast.LENGTH_SHORT
                    ).show()
            } else {
                mTmpStation.mName = etStationName.text.toString()
                mTmpStation.mRadius = Integer.parseInt(etRadius.text.toString())
                if (cfgManager.saveStation(mTmpStation)) {
                    mStations.add(mTmpStation)
                    mTmpStation.disconnectFromMap()
                    mTmpStation.connectToMap(mMap)
                    mTmpStation = Station()
                    dialog.dismiss()
                } else
                    Toast.makeText(
                        this,
                        resources.getString(R.string.savingPointError),
                        Toast.LENGTH_SHORT
                    ).show()
            }

            val bSavePoint = findViewById<Button>(R.id.bAddEditStation)
            val bRemovePoint = findViewById<Button>(R.id.bRemoveStation)
            bSavePoint.visibility = View.INVISIBLE
            bRemovePoint.visibility = View.INVISIBLE
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
//                placeMarkerOnMap(place.latLng)
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

    private fun clearSelection() {
        mLastClickedStation = null
        for (station in mStations)
            station.setNormal()
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        val bSavePoint = findViewById<Button>(R.id.bAddEditStation)
        val bRemovePoint = findViewById<Button>(R.id.bRemoveStation)
        val stationId = p0.tag as Int
        clearSelection()
        if (stationId != -1) {
            bSavePoint.visibility = View.VISIBLE
            bSavePoint.text = resources.getString(R.string.editPoint)
            bRemovePoint.visibility = View.VISIBLE
            mLastClickedStation = mStations.getStationWithId(stationId)
            mLastClickedStation?.setSelected()
        } else if (p0.tag == -1) {
            mLastClickedStation = null
            bSavePoint.text = resources.getString(R.string.addPoint)
        }
        return false
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(this)
        setUpMap()
        setUpMarkers()
    }

    override fun onMapClick(p0: LatLng) {
        val bSavePoint = findViewById<Button>(R.id.bAddEditStation)
        val bRemovePoint = findViewById<Button>(R.id.bRemoveStation)
        bSavePoint.text = resources.getString(R.string.addPoint)
        bSavePoint.visibility = View.VISIBLE
        bRemovePoint.visibility = View.INVISIBLE
        clearSelection()
        mTmpStation.disconnectFromMap()
        mTmpStation.mLat = p0.latitude
        mTmpStation.mLon = p0.longitude
        mTmpStation.connectToMap(mMap)
        mLastClickedPlace = p0
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

    private fun setUpMarkers() {
        for (station in mStations) {
            station.connectToMap(mMap)
        }
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
//                    placeMarkerOnMap(currentLatLng)
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

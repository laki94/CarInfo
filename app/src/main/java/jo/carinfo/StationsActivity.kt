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

class StationsActivity : FragmentActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, Workable<Location> {

    private lateinit var mMap: GoogleMap
    private lateinit var mLastLocation: Location
    private val mStations = StationList()
    private lateinit var mLastClickedPlace: LatLng
    private var mTmpStation = Station()
    private var mLastClickedStation: Station? = null
    private var mAnimateToLocation = true
    private val mPermissionsManager = PermissionsManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stations)

        val extras = intent.extras
        if (extras != null)
            if (extras.containsKey("stations"))
                for (station in extras.getSerializable("stations") as ArrayList<Station>) {
                    mStations.add(station)
                }

        if (mPermissionsManager.haveInternetPermission(this))
            startMap()
        else
            mPermissionsManager.askForInternetPermission(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PermissionsManager.INTERNET_PERMISSION_REQ_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startMap()
                } else
                    finish()
            }
            PermissionsManager.LOCATION_PERMISSION_REQ_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    setUpMap()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun startMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.fMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun work(aLocation: Location) {
        mMap.isMyLocationEnabled = true
        mLastLocation = aLocation
        if (mAnimateToLocation) {
            mAnimateToLocation = false
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(mLastLocation.latitude, mLastLocation.longitude), 12f))
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    fun onRemoveStationClick(view: View) {
        if (mLastClickedStation != null) {
            ConfigManager(this).use {
                if (it.removeStation(mLastClickedStation!!)) {
                    mLastClickedStation!!.disconnectFromMap()
                    mStations.remove(mLastClickedStation!!)

                    val bSavePoint = findViewById<Button>(R.id.bAddEditStation)
                    val bRemovePoint = findViewById<Button>(R.id.bRemoveStation)

                    bSavePoint.visibility = View.INVISIBLE
                    bRemovePoint.visibility = View.INVISIBLE
                }
            }
        }
    }

    fun onAddMarkerOnMyLocationClick(view: View) {
        if (this::mLastLocation.isInitialized)
            onMapClick(LatLng(mLastLocation.latitude, mLastLocation.longitude))
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
            if (etStationName.text.isEmpty())
                Toast.makeText(this, resources.getString(R.string.pointNameCannotBeEmpty), Toast.LENGTH_SHORT).show()
            else if (etRadius.text.isEmpty())
                Toast.makeText(this, resources.getString(R.string.radiusCannotBeEmpty), Toast.LENGTH_SHORT).show()
            else {
                ConfigManager(this).use {
                    if (editing) {
                        mLastClickedStation!!.mName = etStationName.text.toString()
                        mLastClickedStation!!.mRadius = Integer.parseInt(etRadius.text.toString())
                        if (it.editStation(mLastClickedStation!!)) {
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
                        if (it.saveStation(mTmpStation)) {
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
                }

                val bSavePoint = findViewById<Button>(R.id.bAddEditStation)
                val bRemovePoint = findViewById<Button>(R.id.bRemoveStation)
                bSavePoint.visibility = View.INVISIBLE
                bRemovePoint.visibility = View.INVISIBLE
            }
        }
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
            mTmpStation.disconnectFromMap()
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

    private fun setUpMarkers() {
        for (station in mStations) {
            station.connectToMap(mMap)
        }
    }

    private fun setUpMap() {
        if (!mPermissionsManager.haveLocationPermission(this)) {
            mPermissionsManager.askForLocationPermission(this)
        } else
            LocationUpd.instance.addCallback(this)
    }
}

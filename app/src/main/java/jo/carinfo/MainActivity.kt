package jo.carinfo

import android.app.Activity
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.android.gms.maps.model.LatLng

const val SETTINGS_CLICK = 1
const val FUEL_ENTRY = 2
const val STATIONS_CLICK = 3

class MainActivity : AppCompatActivity(), ServiceConnection {

    private val CHANNEL_ID = 22222222
    private val TAG = "MAIN"

    private lateinit var mCars: CarsList
    private lateinit var mStations: StationList
    private var mNotificationVisible: Boolean = false
    private lateinit var mStationCheck: StationCheck
    private val mPermissionsManager = PermissionsManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ConfigManager(this).use {
            mCars = it.getAllCars()
            mStations = it.getAllStations()
        }

        val intent = Intent(application, LocationUpd::class.java)
        Notifications.instance = Notifications(this, application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        application.startService(intent)
        application.bindService(intent, this, Context.BIND_AUTO_CREATE)
        mStationCheck = StationCheck(this)
        checkCarInspectionDates()
    }

    private fun checkCarInspectionDates() {
        for (car in mCars) {
            if (car.isInspectionComing())
                Notifications.instance.showInspectionIsComingNotification(car)
        }
    }

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        Log.i(TAG, "service connected")
        val name = p0?.className
        if (name!!.endsWith("LocationUpd")) {
            LocationUpd.instance = (p1 as LocationUpd.LocationServiceBinder).service
            LocationUpd.instance.mContext = this@MainActivity

            if (mPermissionsManager.haveLocationPermission(this))
                mStationCheck.start()
            else
                mPermissionsManager.askForLocationPermission(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PermissionsManager.LOCATION_PERMISSION_REQ_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    LocationUpd.instance.requestLocationUpdates()
                    mStationCheck.start()
            }

            PermissionsManager.FOREGROUND_SERVICE_PERMISSION_REQ_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    LocationUpd.instance.startForeground()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        Log.i(TAG, "service disconnected")
        LocationUpd.instance.stopTracking()
    }

    fun onSettingsClick(view : View)
    {
        val intent = Intent(this, SettingsActivity::class.java)
        intent.putExtra("cars", mCars)
        startActivityForResult(intent, SETTINGS_CLICK)
    }

    fun onStationsClick(view: View) {
        val intent = Intent(this, StationsActivity::class.java)
        intent.putExtra("stations", mStations)
        startActivityForResult(intent, STATIONS_CLICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTINGS_CLICK)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                Log.d("Main", "got ok result for settings click")
                val extras = data?.extras
                if (extras != null)
                    if (extras.containsKey("cars"))
                    {
                        mCars.clear()
                        for (car in extras.getSerializable("cars") as ArrayList<Car>)
                        {
                            Log.d("Main", String.format("adding car %s to list", car.mName))
                            mCars.add(car)
                        }
                    }
            }
        }
        else if (requestCode == FUEL_ENTRY) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("Main", "got ok result for fuel entry")
                val extras = data?.extras
                if (extras != null) {
                    var name = ""
                    var entry: FuelEntry? = null

                    if (extras.containsKey("name")) {
                        name = extras.getSerializable("name") as String
                        Log.d("Main", String.format("got entry for %s", name))
                    }
                    if (extras.containsKey("entry")) {
                        entry = extras.getSerializable("entry") as FuelEntry
                        Log.d("Main", String.format("got entry info %s", entry.getObjectString(this)))
                    }

                    if ((name.isNotEmpty()) && (entry != null))
                        if (mCars.getCarWithName(name) != null) {
                            mCars.getCarWithName(name).let { it?.addEntry(entry) }
                            mCars.let {
                                ConfigManager(this).use { cfgMgr ->
                                    if (cfgMgr.saveCars(it))
                                        Toast.makeText(this, R.string.carsSavedAfterEntry, Toast.LENGTH_SHORT).show()
                                    else
                                        Toast.makeText(this, R.string.carsNotSavedAfterEntry, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                }
            }
        }
        else if (requestCode == STATIONS_CLICK) {
            if (resultCode == Activity.RESULT_OK) {
                ConfigManager(this).use {
                    mStations = it.getAllStations()
                }
            }
        }
    }

    fun onFuelUsageClick(view: View)
    {
        val intent = Intent(this, FuelUsageGraph::class.java)
        intent.putExtra("cars", mCars)
        startActivity(intent)
    }
}

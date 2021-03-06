package jo.carinfo

import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.android.gms.location.*


interface Workable<T> {

    fun work(t: T) {

    }
}

class LocationUpd : Service() {
    private val binder = LocationServiceBinder()

    private lateinit var mFusedLocationProvider: FusedLocationProviderClient
    private lateinit var mLocationCallback: LocationCallback
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationSettingsRequest: LocationSettingsRequest
    lateinit var mContext: Context
    private val mPermissionsManager = PermissionsManager()

    private val mCallbacksOnPosition = ArrayList<Workable<Location>>()
    private val UPDATE_INTERVAL_MSEC: Long = 10000
    private val FASTEST_UPDATE_INTERVAL_MSEC: Long = 5000

    init {
        createLocationRequest()
        createLocationCallback()
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    private fun setUp() {
        if (mPermissionsManager.haveForegroundServicePermission(mContext))
            startForeground()
        else
            mPermissionsManager.askForForegroundServicePermission(mContext)

        if (mPermissionsManager.haveLocationPermission(mContext))
            requestLocationUpdates()
        else
            mPermissionsManager.askForLocationPermission(mContext)
    }

    private fun createFusedLocationClient() {
        mFusedLocationProvider = LocationServices.getFusedLocationProviderClient(mContext)
    }

    private fun createLocationCallback() {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                for (callback in mCallbacksOnPosition)
                    callback.work(p0.lastLocation)
            }
        }
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = UPDATE_INTERVAL_MSEC
        mLocationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL_MSEC
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest)
        mLocationSettingsRequest = builder.build()
    }

    fun addCallback(aCallback: Workable<Location>) {
        if (mCallbacksOnPosition.count() == 0)
            startTracking()
        mCallbacksOnPosition.add(aCallback)
    }

    override fun onStartCommand(
        intent: Intent,
        flags: Int,
        startId: Int
    ): Int {
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onCreate() {
        Log.i("LOCUPD", "onCreate")
    }

    fun requestLocationUpdates() {
        mFusedLocationProvider.requestLocationUpdates(mLocationRequest, mLocationCallback, null)
    }

    fun startForeground() {
        startForeground(12312312, Notifications.instance.locationNotification)
    }

    override fun onDestroy() {
        super.onDestroy()
        mFusedLocationProvider.removeLocationUpdates(mLocationCallback)
    }

    private fun startTracking() {
        createFusedLocationClient()
        setUp()
    }

    fun stopTracking() {
        onDestroy()
    }

    inner class LocationServiceBinder : Binder() {
        val service: LocationUpd
            get() = this@LocationUpd
    }

    companion object {
        lateinit var instance: LocationUpd
    }
}

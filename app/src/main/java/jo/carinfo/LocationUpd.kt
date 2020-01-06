package jo.carinfo

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import java.io.Serializable


interface Workable<T> {

    fun work(t: T) {

    }
}

class LocationUpd : Service() {
    private val binder =
        LocationServiceBinder()

    private lateinit var mFusedLocationProvider: FusedLocationProviderClient
    private lateinit var mLocationCallback: LocationCallback
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationSettingsRequest: LocationSettingsRequest
    lateinit var mContext: Context

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
        if (ActivityCompat.checkSelfPermission(mContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mContext as Activity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQ_CODE)
            return
        }
        else
            mFusedLocationProvider.requestLocationUpdates(mLocationRequest, mLocationCallback, null)
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
        startForeground(12312312, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        mFusedLocationProvider.removeLocationUpdates(mLocationCallback)

    }

    private fun startTracking() {
        createFusedLocationClient()
        setUp()
    }

    private fun stopTracking() {
        onDestroy()
    }

    private val notification: Notification
        get() {
            val channel = NotificationChannel(
                "channel_01",
                "My Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                getSystemService(
                    NotificationManager::class.java
                )
            notificationManager?.createNotificationChannel(channel)
            val builder =
                Notification.Builder(applicationContext, "channel_01")
                    .setAutoCancel(true)
            return builder.build()
        }

    inner class LocationServiceBinder : Binder() {
        val service: LocationUpd
            get() = this@LocationUpd
    }

    companion object {
        const val LOCATION_PERMISSION_REQ_CODE = 1

        lateinit var instance: LocationUpd
    }
}

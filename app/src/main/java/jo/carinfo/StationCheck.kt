package jo.carinfo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

class StationCheck(aCtx: Context): Workable<Location> {
    private val TAG = "STATIONCHECK"
    private val CHANNEL_ID = "notify_channel"

    private val mContext = aCtx
    private val mCfgMgr = ConfigManager(mContext)

    fun start() {
        LocationUpd.instance.addCallback(this)
    }

    override fun work(t: Location) {
        val stations = mCfgMgr.getAllStations()
        val (isNear, nearStations) = stations.getNearestStation(t)
        for (station in nearStations)
            mCfgMgr.editStation(station)
        if (isNear) {
            Log.i(TAG, "is near station" + nearStations[0].mName)
            Notifications.instance.showNearStationsNotification(nearStations[0].mName)
        } else {
            Log.i(TAG, "is not near any station")
        }
    }
}
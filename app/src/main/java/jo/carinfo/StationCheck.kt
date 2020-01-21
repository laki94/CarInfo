package jo.carinfo

import android.content.Context
import android.location.Location
import android.util.Log

class StationCheck(aCtx: Context): Workable<Location> {
    private val TAG = "STATIONCHECK"

    private val mContext = aCtx

    fun start() {
        LocationUpd.instance.addCallback(this)
    }

    override fun work(t: Location) {
        ConfigManager(mContext).use {
            val stations = it.getAllStations()
            val (isNear, nearStations) = stations.getNearestStation(t)
            for (station in nearStations)
                it.editStation(station)
            if (isNear) {
                Log.i(TAG, "is near station" + nearStations[0].mName)
                Notifications.instance.showNearStationsNotification(nearStations[0].mName)
            } else {
                Log.i(TAG, "is not near any station")
            }
        }
    }
}
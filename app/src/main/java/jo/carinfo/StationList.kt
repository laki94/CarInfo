package jo.carinfo

import android.location.Location
import android.util.Log
import java.io.Serializable

class StationList: ArrayList<Station>(), Serializable {

    private val TAG = "STATIONS"

    fun getStationWithId(aId: Int): Station? {
        for (station in this) {
            if (station.mId == aId)
                return station
        }
        return null
    }

    fun getNearestStation(aLocation: Location): Pair<Boolean, StationList> {
        var nearestDist = Int.MAX_VALUE
        var tmpDist: Int
        val resStation = StationList()
        var isNearStation = false
        for (station in this) {
            tmpDist = Math.round(aLocation.distanceTo(station.location()))
            if (tmpDist <= station.mRadius) {
                if (!station.mInRange) {
                    Log.i(TAG, "station ${station.mName} in range $tmpDist")
                    station.mInRange = true
                    if (tmpDist < nearestDist) {
                        nearestDist = tmpDist
                        isNearStation = true
                        resStation.add(0, station)
                    } else
                        resStation.add(station)
                }
            } else if (station.mInRange) {
                Log.i(TAG, "station ${station.mName} no longer in range")
                station.mInRange = false
                resStation.add(resStation.count(), station)
            }
        }
        return Pair(isNearStation, resStation)
    }
}
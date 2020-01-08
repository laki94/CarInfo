package jo.carinfo

import android.location.Location
import java.io.Serializable

class StationList: ArrayList<Station>(), Serializable {

    fun getStationWithId(aId: Int): Station? {
        for (station in this) {
            if (station.mId == aId)
                return station
        }
        return null
    }

    fun getNearestStation(aLocation: Location): Pair<Boolean, String> {
        var nearestDist = Int.MAX_VALUE
        var tmpDist: Int
        var stationName = ""
        var isNearStation = false
        for (station in this) {
            tmpDist = Math.round(aLocation.distanceTo(station.location()))
            if ((tmpDist < nearestDist) && (tmpDist <= station.mRadius)) {
                nearestDist = tmpDist
                isNearStation = true
                stationName = station.mName
            }
        }
        return Pair(isNearStation, stationName)
    }
}
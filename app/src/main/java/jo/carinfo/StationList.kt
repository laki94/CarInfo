package jo.carinfo

import java.io.Serializable

class StationList: ArrayList<Station>(), Serializable {

    fun getStationWithId(aId: Int): Station? {
        for (station in this) {
            if (station.mId == aId)
                return station
        }
        return null
    }

}
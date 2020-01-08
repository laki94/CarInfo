package jo.carinfo

import android.graphics.Color
import android.location.Location
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import java.io.Serializable

class Station: Serializable {

    var mId = -1
    var mName = ""
    var mLat = 0.0
    var mLon = 0.0
    var mRadius = 0
    lateinit var mMarker: Marker
    lateinit var mCircle: Circle

    fun latLng(): LatLng {
        return LatLng(mLat, mLon)
    }

    fun location(): Location {
        val loc = Location("")
        loc.latitude = mLat
        loc.longitude = mLon
        return loc
    }

    fun setSelected() {
        if (this::mCircle.isInitialized)
            mCircle.strokeColor = Color.YELLOW
    }

    fun setNormal() {
        if (this::mCircle.isInitialized)
            mCircle.strokeColor = Color.RED
    }

    fun connectToMap(aMap: GoogleMap) {
        mMarker = aMap.addMarker(MarkerOptions().position(latLng()).title(mName))
        mMarker.tag = mId
        mCircle = aMap.addCircle(CircleOptions()
            .center(latLng())
            .radius(mRadius.toDouble())
            .strokeWidth(10f)
            .strokeColor(Color.RED))
    }

    fun disconnectFromMap() {
        if (this::mMarker.isInitialized)
            mMarker.remove()
        if (this::mCircle.isInitialized)
            mCircle.remove()
    }
}
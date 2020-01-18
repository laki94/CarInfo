package jo.carinfo

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class PermissionsManager {

    fun haveInternetPermission(aCtx: Context): Boolean {
        return (ActivityCompat.checkSelfPermission(aCtx, android.Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED)
    }

    fun haveLocationPermission(aCtx: Context): Boolean {
        return (ActivityCompat.checkSelfPermission(aCtx, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    fun askForLocationPermission(aCtx: Context) {
        ActivityCompat.requestPermissions(aCtx as Activity,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQ_CODE)
    }

    fun askForInternetPermission(aCtx: Context) {
        ActivityCompat.requestPermissions(aCtx as Activity, arrayOf(android.Manifest.permission.INTERNET),
            INTERNET_PERMISSION_REQ_CODE)
    }

    companion object {
        const val LOCATION_PERMISSION_REQ_CODE = 1
        const val INTERNET_PERMISSION_REQ_CODE = 2
    }
}
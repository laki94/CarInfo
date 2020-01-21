package jo.carinfo

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat

class PermissionsManager {

    fun haveInternetPermission(aCtx: Context): Boolean {
        return (ActivityCompat.checkSelfPermission(aCtx, android.Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED)
    }

    fun haveLocationPermission(aCtx: Context): Boolean {
        return (ActivityCompat.checkSelfPermission(aCtx, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    fun haveForegroundServicePermission(aCtx: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
            return true
        else
            return (ActivityCompat.checkSelfPermission(aCtx, android.Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED)
    }

    fun askForForegroundServicePermission(aCtx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ActivityCompat.requestPermissions(
                aCtx as Activity,
                arrayOf(android.Manifest.permission.FOREGROUND_SERVICE),
                FOREGROUND_SERVICE_PERMISSION_REQ_CODE)
        }
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
        const val FOREGROUND_SERVICE_PERMISSION_REQ_CODE = 3
    }
}
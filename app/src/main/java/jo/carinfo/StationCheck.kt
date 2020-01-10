package jo.carinfo

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class StationCheck(aCtx: Context, aNotificationManager: NotificationManager): Workable<Location> {
    private val TAG = "STATIONCHECK"
    private val CHANNEL_ID = "notify_channel"

    private val mContext = aCtx
    private val mCfgMgr = ConfigManager(mContext)
    private var mNotificationShown = false
    private val mNotificationManager = aNotificationManager

    fun start() {
        LocationUpd.instance.addCallback(this)
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //Create channel only if it is not already created
            if (mNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                mNotificationManager.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        TAG,
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                )
            }
        }
    }

    override fun work(t: Location) {
        val stations = mCfgMgr.getAllStations()
        val (isNear, stationName) = stations.getNearestStation(t)
        Log.i(TAG, "is near station = $isNear (\"$stationName\")")
        if ((isNear) && (!mNotificationShown))
            showNotification(stationName)
    }

    private fun showNotification(aStationName: String) {
        mNotificationShown = true
        createNotificationChannel()

        val intent = Intent(mContext, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(mContext, 69, intent, 0)

        val builder = NotificationCompat.Builder(mContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.common_google_signin_btn_icon_light)
            .setContentTitle("Jestes blisko stacji $aStationName")
            .setContentText("Nie zapomnij dodać wpisu")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)

        mNotificationManager.notify(1, builder.build())
    }
}
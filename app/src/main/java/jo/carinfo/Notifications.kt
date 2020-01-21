package jo.carinfo

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService

class Notifications(aContext: Context, aNotificationManager: NotificationManager) {

    private val TAG = "NOTIFICATION"
    private val CHANNEL_NEAR_STATION = "notify_channel_near_station"
    private val STATION_ID = 1
    private var mNotificationId = 2


    private val mNotificationManager = aNotificationManager
    private val mContext = aContext

    private fun createNotificationChannel(aChannelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mNotificationManager.getNotificationChannel(aChannelId) == null) {
                mNotificationManager.createNotificationChannel(
                    NotificationChannel(
                        aChannelId,
                        TAG,
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                )
            }
        }
    }

    fun showNearStationsNotification(aStationName: String) {
        createNotificationChannel(CHANNEL_NEAR_STATION)

        val builder = NotificationCompat.Builder(mContext, CHANNEL_NEAR_STATION)
            .setSmallIcon(R.drawable.common_google_signin_btn_icon_light)
            .setContentTitle(mContext.resources.getString(R.string.YouAreNearStation) + " " + aStationName)
            .setContentText(mContext.resources.getString(R.string.DontForgetToAddEntry))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)

        mNotificationManager.notify(STATION_ID, builder.build())
    }

    val locationNotification: Notification
        get() {
            createNotificationChannel(CHANNEL_NEAR_STATION)
            val builder =
                Notification.Builder(mContext, CHANNEL_NEAR_STATION)
                    .setAutoCancel(true)
            return builder.build()
        }

    fun showInspectionIsComingNotification(aCar: Car) {
        createNotificationChannel(CHANNEL_NEAR_STATION)

        val builder = NotificationCompat.Builder(mContext, CHANNEL_NEAR_STATION)
            .setSmallIcon(R.drawable.common_google_signin_btn_icon_light)
            .setContentTitle(aCar.mName)
            .setContentText(mContext.resources.getString(R.string.DaysToInspection) + ": " + aCar.mInspection!!.daysLeftToInspection())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)

        mNotificationManager.notify(mNotificationId++, builder.build())
    }

    companion object {
        lateinit var instance: Notifications
    }
}
package jo.carinfo

import android.content.Context
import android.content.SharedPreferences

const val PREFS_FILENAME = "SharedExample"
const val FTIME = "first_time"

class SharedSettingsClass(aContext: Context)
{
    private var mPrefs : SharedPreferences = aContext.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

    fun isLaunchedForFirstTime() : Boolean
    {
        return mPrefs.getBoolean(FTIME, true)
    }

    fun setWasLaunchedForFirstTime()
    {
        mPrefs.edit().putBoolean(FTIME, false).apply()
    }

}

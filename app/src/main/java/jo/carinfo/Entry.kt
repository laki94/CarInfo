package jo.carinfo

import android.content.Context
import java.io.Serializable
import java.util.*

abstract class Entry(date: Date = Date(0)): Serializable {
    var mDate = date

    abstract fun getObjectString(context: Context): String

    abstract fun getRawData(): String
}
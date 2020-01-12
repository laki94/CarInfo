package jo.carinfo

import android.content.Context
import org.joda.time.DateTime
import java.io.Serializable
import java.util.*

abstract class Entry(aDate: DateTime): Serializable {
    var mId = -1
    var mDate = aDate

    abstract fun getObjectString(context: Context): String

    abstract fun getRawData(): String
}
package jo.carinfo

import android.content.Context
import org.joda.time.DateTime
import org.joda.time.Days

class CarInspectionEntry(aDate: DateTime, aInspectionDate: DateTime): Entry(aDate) {
    var mDateToInspection = aInspectionDate

    override fun getObjectString(context: Context): String {
        return String.format(context.resources.getString(R.string.DaysToInspection) +  " ${daysLeftToInspection()}")
    }

    override fun getRawData(): String {
        return mDateToInspection.toString()
    }

    fun daysLeftToInspection(): Int {
        return Days.daysBetween(DateTime.now(), mDateToInspection).days // TODO
    }
}
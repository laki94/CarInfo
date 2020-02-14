package jo.carinfo

import android.content.Context
import org.joda.time.DateTime
import org.joda.time.Days

class CarInspectionEntry(aDate: DateTime = DateTime(0), aLastInspectionDate: DateTime = DateTime(0), aRemindAfter: InspectionRemindAfter = InspectionRemindAfter.UNKNOWN): Entry(aDate) {
    var mLastInspectionDate = aLastInspectionDate
    var mRemindAfter = aRemindAfter

    override fun getObjectString(context: Context): String {
        return String.format(context.resources.getString(R.string.DaysToInspection) +  ": ${daysLeftToInspection()}")
    }

    override fun getRawData(): String {
        return mLastInspectionDate.toString()
    }

    fun daysLeftToInspection(): Int {
        return Days.daysBetween(DateTime.now(), getNextInspectionDate()).days
    }

    private fun getNextInspectionDate(): DateTime {
        when (mRemindAfter) {
            InspectionRemindAfter.YEAR -> return mLastInspectionDate.plusYears(1)
            InspectionRemindAfter.TWO_YEARS -> return mLastInspectionDate.plusYears(2)
            InspectionRemindAfter.THREE_YEARS -> return mLastInspectionDate.plusYears(3)
            else ->
                throw NotImplementedError("trying to get next inspection date for unknown reminder $mRemindAfter")
        }
    }
}
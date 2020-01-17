package jo.carinfo

import android.graphics.Color
import android.util.Log
import java.io.Serializable

class Car(aName: String = ""): Serializable
{
    private val TAG = "CAR"

    var mName : String = aName
    val mFuelEntries = FuelEntriesList()
    var mChartColor = Color.argb(255, 0, 0, 0)
    var mInspection: CarInspectionEntry? = null

    fun isInspectionComing(): Boolean {
        if (mInspection != null) {
            return (mInspection!!.daysLeftToInspection() <= 7) && (mInspection!!.daysLeftToInspection() >= 0)
        } else return false
    }

    fun addEntry(aEntry: Entry)
    {
        if (aEntry is FuelEntry)
        {
            Log.d(TAG, String.format("adding new fuel entry to %s", mName))
            mFuelEntries.add(aEntry)
        } else if (aEntry is CarInspectionEntry) {
            Log.d(TAG, "adding new car inspection entry to $mName")
            mInspection = aEntry
        } else
            Log.e(TAG, String.format("trying to add unknown entry to %s", mName))
    }

    fun editEntry(aEntry: Entry) {
        if (aEntry is FuelEntry) {
            for (entry in mFuelEntries) {
                if (entry.mId == aEntry.mId) {
                    entry.mOdometer = aEntry.mOdometer
                    entry.mPerLiter = aEntry.mPerLiter
                    entry.mFuelAmount = aEntry.mFuelAmount
                    break
                }
            }
        } else if (aEntry is CarInspectionEntry) {
            mInspection?.mLastInspectionDate = aEntry.mLastInspectionDate
            mInspection?.mRemindAfter = aEntry.mRemindAfter
        }
    }
}
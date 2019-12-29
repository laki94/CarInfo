package jo.carinfo

import android.util.Log
import java.io.Serializable

class Car(aName: String = ""): Serializable
{

    var mName : String = aName
    val mFuelEntries = FuelEntriesList()
    val mOilEntries = OilEntriesList()
    
    fun addEntry(aEntry: Entry)
    {
        if (aEntry is FuelEntry)
        {
            Log.d("Car", String.format("adding new fuel entry to %s", mName))
            mFuelEntries.add(aEntry)
        }
        else if (aEntry is OilEntry)
        {
            Log.d("Car", String.format("adding new oil entry to %s", mName))
            mOilEntries.add(aEntry)
        }
        else
            Log.e("Car", String.format("trying to add unknown entry to %s", mName))
    }

    fun editEntry(aEntry: Entry) {
        if (aEntry is FuelEntry) {
            for (entry in mFuelEntries) {
                if (entry.mId == aEntry.mId) {
                    entry.mOdometer = aEntry.mOdometer
                    entry.mPerLiter = aEntry.mPerLiter
                    entry.mFuelAmount = aEntry.mFuelAmount
                    entry.mMileage = entry.mMileage
                    break
                }
            }
        }
        else if (aEntry is OilEntry) {
            for (entry in mOilEntries) {
                if (entry.mId == aEntry.mId) {
                    entry.mOrgMileage = aEntry.mOrgMileage
                    entry.mRemindAfter = aEntry.mRemindAfter
                    break
                }
            }
        }
    }
}
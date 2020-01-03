package jo.carinfo

import android.graphics.Color
import android.util.Log
import java.io.Serializable
import java.util.*

class Car(aName: String = ""): Serializable
{

    var mName : String = aName
    val mFuelEntries = FuelEntriesList()
    var mChartColor = Color.argb(255, 0, 0, 0)
    
    fun addEntry(aEntry: Entry)
    {
        if (aEntry is FuelEntry)
        {
            Log.d("Car", String.format("adding new fuel entry to %s", mName))
            mFuelEntries.add(aEntry)
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
                    break
                }
            }
        }
    }
}
package jo.carinfo

import android.util.Log
import java.io.Serializable

class Car(aName: String = ""): Serializable
{

    var mName : String = aName
    var mFuelEntries = FuelEntriesList()

    fun addEntry(aEntry: Entry)
    {
        if (aEntry is FuelEntry)
        {
            Log.d("Car", String.format("adding new fuel entry to %s", mName))
            mFuelEntries.add(aEntry)
        }
    }
}
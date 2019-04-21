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
}
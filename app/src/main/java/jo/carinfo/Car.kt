package jo.carinfo

import java.io.Serializable

class Car(aName: String = ""): Serializable
{

    var mName : String = aName
    var mEntries = EntriesList()

    fun addEntry(aEntry: Entry)
    {
        if (aEntry is FuelEntry)
            mEntries.add(aEntry)
    }
}
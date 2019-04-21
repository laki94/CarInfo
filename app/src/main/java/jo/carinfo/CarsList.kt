package jo.carinfo

import java.io.Serializable

class CarsList: ArrayList<Car>(), Serializable
{
    fun getCarWithName(aName: String): Car?
    {
        val idxOfCar = indexOf(aName)
        val result: Car?
        if (idxOfCar == -1)
            result = null
        else
            result = this[idxOfCar]

        return result
    }

    fun indexOf(aName: String): Int
    {
        for (i in 0 until this.count())
        {
            if (this[i].mName.equals(aName, true))
                return i
        }
        return -1
    }

    override fun indexOf(aCar: Car): Int
    {
        if (indexOf(aCar.mName) != -1)
        {
            val orgFuelEntries = FuelEntriesList()
            val newFuelEntries = FuelEntriesList()

            orgFuelEntries.addAll(this[indexOf(aCar.mName)].mFuelEntries)
            newFuelEntries.addAll(aCar.mFuelEntries)

            if (newFuelEntries.count() == orgFuelEntries.count())
            {
                for (entry in newFuelEntries)
                    if (orgFuelEntries.indexOf(entry) == -1)
                        return -1
            }
            else
                return -1

            val orgOilEntries = OilEntriesList()
            val newOilEntries = OilEntriesList()
            orgOilEntries.addAll(this[indexOf(aCar.mName)].mOilEntries)
            newOilEntries.addAll(aCar.mOilEntries)
            if (newOilEntries.count() == orgOilEntries.count())
            {
                for (entry in newOilEntries)
                    if (orgOilEntries.indexOf(entry) == -1)
                        return -1
            }
            else
                return -1
            
            return indexOf(aCar.mName)
        }
        return -1
    }
}

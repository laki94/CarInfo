package jo.carinfo

import java.io.Serializable

open class CarsList: ArrayList<Car>(), Serializable
{
    fun changeName(aOldCar: Car, aNewCar: Car)
    {
        if (indexOf(aOldCar) != -1) {
            this[indexOf(aOldCar)].mName = aNewCar.mName
        }
    }

    fun changeColor(aOldCar: Car, aNewCar: Car) {
        if (indexOf(aOldCar) != -1) {
            this[indexOf(aOldCar)].mChartColor = aNewCar.mChartColor
        }
    }

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
            
            return indexOf(aCar.mName)
        }
        return -1
    }
}

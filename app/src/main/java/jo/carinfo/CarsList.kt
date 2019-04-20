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
            val orgEntries = this[indexOf(aCar.mName)].mEntries
            val newEntries = aCar.mEntries
            if (newEntries.count() == orgEntries.count())
            {
                for (entry in newEntries)
                    if (orgEntries.indexOf(entry) == -1)
                        return -1
                return indexOf(aCar.mName)
            }
        }
        return -1
    }
}

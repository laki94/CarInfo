package jo.carinfo

class FuelEntriesList: ArrayList<FuelEntry>()
{
    override fun indexOf(aEntry: FuelEntry): Int
    {
        for (i in 0 until this.count())
        {
            if (this[i].getRawData().equals(aEntry.getRawData()))
                return i
        }
        return -1
    }
}
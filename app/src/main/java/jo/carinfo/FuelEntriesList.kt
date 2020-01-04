package jo.carinfo

class FuelEntriesList: ArrayList<FuelEntry>()
{
    override fun indexOf(aEntry: FuelEntry): Int
    {
        for (i in 0 until this.count())
        {
            if (aEntry.mId == this[i].mId)
                return i
        }
        return -1
    }

    fun sortByDate() {
        this.sortWith(compareBy({ it.mDate }))
    }
}
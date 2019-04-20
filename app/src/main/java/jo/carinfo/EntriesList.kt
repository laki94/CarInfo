package jo.carinfo

class EntriesList: ArrayList<Entry>()
{
    override fun indexOf(aEntry: Entry): Int
    {
        for (i in 0 until this.count())
        {
            if (this[i].getRawData().equals(aEntry.getRawData()))
                return i
        }
        return -1
    }
}
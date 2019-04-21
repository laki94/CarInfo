package jo.carinfo

class OilEntriesList: ArrayList<OilEntry>() {

    override fun indexOf(aEntry: OilEntry): Int
    {
        for (i in 0 until this.count())
        {
            if (this[i].getRawData().equals(aEntry.getRawData()))
                return i
        }
        return -1
    }
}
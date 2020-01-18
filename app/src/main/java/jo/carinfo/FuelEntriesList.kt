package jo.carinfo

import org.joda.time.DateTime
import java.util.*
import kotlin.collections.ArrayList

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

    fun getAvgOdometer(aDateFrom: DateTime, aDateTo: DateTime): Int {
        var result = 0
        var entries = 0
        for (entry in this) {
            if ((entry.mDate >= aDateFrom) && (entry.mDate <= aDateTo)) {
                result += entry.mOdometer
                entries++
            }
        }
        if (result != 0)
            return String.format(Locale.ENGLISH,"%d", result / entries).toInt()
        else
            return 0
    }

    fun getAvgPricePerLiter(aDateFrom: DateTime, aDateTo: DateTime): Double {
        var result = 0.0
        var entries = 0
        for (entry in this) {
            if ((entry.mDate >= aDateFrom) && (entry.mDate <= aDateTo)) {
                result += entry.mPerLiter
                entries++
            }
        }
        if (result != 0.0)
            return String.format(Locale.ENGLISH,"%.2f", result / entries).toDouble()
        else
            return 0.0
    }

    fun getAvgTotalPrice(aDateFrom: DateTime, aDateTo: DateTime): Double {
        var result = 0.0
        var entries = 0
        for (entry in this) {
            if ((entry.mDate >= aDateFrom) && (entry.mDate <= aDateTo)) {
                result += entry.getTotalCost()
                entries++
            }
        }
        if (result != 0.0)
            return String.format(Locale.ENGLISH,"%.2f", result / entries).toDouble()
        else
            return 0.0
    }

    fun getAvgFuelAmount(aDateFrom: DateTime, aDateTo: DateTime): Double {
        var result = 0.0
        var entries = 0
        for (entry in this) {
            if ((entry.mDate >= aDateFrom) && (entry.mDate <= aDateTo)) {
                result += entry.mFuelAmount
                entries++
            }
        }
        if (result != 0.0)
            return String.format(Locale.ENGLISH,"%.2f", result / entries).toDouble()
        else
            return 0.0
    }

    fun getBestConsumption(aDateFrom: DateTime, aDateTo: DateTime): Double {
        var result = Double.MAX_VALUE
        for (entry in this) {
            if ((entry.mDate >= aDateFrom) && (entry.mDate <= aDateTo)) {
                if (result > entry.getFuelConsumption())
                    result = entry.getFuelConsumption()
            }
        }
        if (result != Double.MAX_VALUE)
            return String.format(Locale.ENGLISH,"%.2f", result).toDouble()
        else
            return 0.0
    }

    fun getWorstConsumption(aDateFrom: DateTime, aDateTo: DateTime): Double {
        var result = Double.MIN_VALUE
        for (entry in this) {
            if ((entry.mDate >= aDateFrom) && (entry.mDate <= aDateTo)) {
                if (result < entry.getFuelConsumption())
                    result = entry.getFuelConsumption()
            }
        }
        if (result != Double.MIN_VALUE)
            return String.format(Locale.ENGLISH,"%.2f", result).toDouble()
        else
            return 0.0
    }
}
package jo.carinfo

import android.content.Context
import org.joda.time.DateTime
import java.util.*
import kotlin.math.round

class FuelEntry(aDate: DateTime = DateTime(0), odo: Int = 0, fuelAm: Double = 0.0, perLiter: Double = 0.0): Entry(aDate) {

    var mOdometer = odo
    var mFuelAmount = fuelAm
    var mPerLiter = perLiter

    override fun getObjectString(context: Context): String
    {
        return String.format("%s: %d\n%s: %.2f\n%s: %.2f", context.getString(R.string.odometer),
            mOdometer, context.getString(R.string.fuelAmount), mFuelAmount,
            context.getString(R.string.perLiter), mPerLiter)
    }

    override fun getRawData(): String
    {
        return String.format("%d,%.2f,%.2f", mOdometer, mFuelAmount, mPerLiter)
    }

    fun getFuelConsumption(): Double {
        return "%.2f".format(Locale.ENGLISH, mOdometer / mFuelAmount).toDouble()
    }

    fun getTotalCost(): Double {
        return "%.2f".format(Locale.ENGLISH, mPerLiter * mFuelAmount).toDouble()
    }
}
package jo.carinfo

import android.content.Context
import java.util.*

class FuelEntry(date: Date = Date(0), odo: Int = 0, mile: Int = 0, fuelAm: Double = 0.0, perLiter: Double = 0.0): Entry(date) {

    var mOdometer = odo
    var mMileage = mile
    var mFuelAmount = fuelAm
    var mPerLiter = perLiter

    override fun getObjectString(context: Context): String
    {
        return String.format("%s: %d\n%s: %d\n%s: %.2f\n%s: %.2f", context.getString(R.string.odometer),
            mOdometer, context.getString(R.string.mileage), mMileage, context.getString(R.string.fuelAmount), mFuelAmount,
            context.getString(R.string.perLiter), mPerLiter)
    }

    override fun getRawData(): String
    {
        return String.format("%d,%d,%.2f,%.2f", mOdometer, mMileage, mFuelAmount, mPerLiter)
    }
}
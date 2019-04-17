package jo.carinfo

import java.util.*

class FuelEntry(date: Date = Date(0), odo: Int = 0, mile: Int = 0, fuelAm: Double = 0.0, perLiter: Double = 0.0): Entry(date) {

    var mOdometer = odo
    var mMileage = mile
    var mFuelAmount = fuelAm
    var mPerLiter = perLiter

//    var odometer: Int
//        get() = this.mOdometer
//        set(value) { mOdometer = value }
}
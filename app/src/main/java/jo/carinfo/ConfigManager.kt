package jo.carinfo

import android.content.Context

class ConfigManager(ctx: Context) {

    private val dbHandle = CarsDBHelper(ctx)

    fun saveCars(aCars: CarsList): Boolean {
        return dbHandle.saveCars(aCars)
    }

    fun getAllCars(): CarsList {
        return dbHandle.getAllCars()
    }

    fun addCar(aCar: Car): Boolean {
        return dbHandle.addCar(aCar)
    }

    fun removeCar(aCarName: String): Boolean {
        return dbHandle.removeCar(aCarName)
    }

    fun editCar(aOldCar: Car, aNewCar: Car): Boolean {
        return dbHandle.editCar(aOldCar, aNewCar)
    }

    fun addFuelEntry(aCarName: String, aEntry: FuelEntry): Boolean {
        val carId = dbHandle.getCarId(aCarName)
        return dbHandle.addFuelEntry(carId, aEntry)
    }

    fun addOilEntry(aCarName: String, aEntry: OilEntry): Boolean {
        val carId = dbHandle.getCarId(aCarName)
        return dbHandle.addOilEntry(carId, aEntry)
    }

    fun editFuelEntry(aEntry: FuelEntry): Boolean {
        return dbHandle.editFuelEntry(aEntry)
    }

    fun editOilEntry(aEntry: OilEntry): Boolean {
        return dbHandle.editOilEntry(aEntry)
    }

    fun removeEntry(aEntryId: Int): Boolean {
        return dbHandle.removeEntry(aEntryId)
    }
}
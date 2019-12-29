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

    fun addCar(aCarName: String): Boolean {
        return dbHandle.addCar(aCarName)
    }

    fun removeCar(aCarName: String): Boolean {
        return dbHandle.removeCar(aCarName)
    }

    fun editCarName(aOldCarName: String, aNewCarName: String): Boolean {
        return dbHandle.editCarName(aOldCarName, aNewCarName)
    }

    fun addFuelEntry(aCarName: String, aEntry: FuelEntry): Boolean {
        val carId = dbHandle.getCarId(aCarName)
        return dbHandle.addFuelEntry(carId, aEntry)
    }

    fun editFuelEntry(aEntry: FuelEntry): Boolean {
        return dbHandle.editFuelEntry(aEntry)
    }
}
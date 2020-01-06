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

    fun editFuelEntry(aEntry: FuelEntry): Boolean {
        return dbHandle.editFuelEntry(aEntry)
    }

    fun removeEntry(aEntryId: Int): Boolean {
        return dbHandle.removeEntry(aEntryId)
    }

    fun getAllStations(): StationList {
        return dbHandle.getAllStations()
    }

    fun saveStation(aStation: Station): Boolean {
        return dbHandle.saveStation(aStation)
    }

    fun editStation(aStation: Station): Boolean {
        return dbHandle.editStation(aStation)
    }

    fun removeStation(aStation: Station): Boolean {
        return dbHandle.removeStation(aStation)
    }
}
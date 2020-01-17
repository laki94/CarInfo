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

    fun removeFuelEntry(aEntry: Entry): Boolean {
        return dbHandle.removeEntry(aEntry)
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

    fun addInspectionEntry(aCarName: String, aEntry: CarInspectionEntry): Boolean {
        return dbHandle.addInspectionEntry(aCarName, aEntry)
    }

    fun editInspectionEntry(aEntry: CarInspectionEntry): Boolean {
        return dbHandle.editInspectionEntry(aEntry)
    }

    fun removeInspectionEntry(aEntry: CarInspectionEntry): Boolean {
        return dbHandle.removeEntry(aEntry)
    }
}
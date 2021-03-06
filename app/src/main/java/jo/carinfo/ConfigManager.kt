package jo.carinfo

import android.content.Context
import android.database.sqlite.SQLiteClosable

class ConfigManager(ctx: Context): SQLiteClosable() {

    private val dbHandle = CarsDBHelper(ctx)

    override fun onAllReferencesReleased() {
        dbHandle.close()
    }

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

    private fun addFuelEntry(aCarName: String, aEntry: FuelEntry): Boolean {
        val carId = dbHandle.getCarId(aCarName)
        return dbHandle.addFuelEntry(carId, aEntry)
    }

    private fun editFuelEntry(aEntry: FuelEntry): Boolean {
        return dbHandle.editFuelEntry(aEntry)
    }

    fun removeEntry(aEntry: Entry): Boolean {
        if (aEntry is FuelEntry)
            return removeFuelEntry(aEntry)
        else if (aEntry is CarInspectionEntry)
            return removeInspectionEntry(aEntry)
        else
            throw NotImplementedError("cannot remove unknown entry ${aEntry.javaClass}")
    }

    fun addEntry(aCarName: String, aEntry: Entry): Boolean {
        if (aEntry is FuelEntry)
            return addFuelEntry(aCarName, aEntry)
        else if (aEntry is CarInspectionEntry)
            return addInspectionEntry(aCarName, aEntry)
        else
            throw NotImplementedError("cannot add unknown entry ${aEntry.javaClass}")
    }

    fun editEntry(aEntry: Entry): Boolean {
        if (aEntry is FuelEntry)
            return editFuelEntry(aEntry)
        else if (aEntry is CarInspectionEntry)
            return editInspectionEntry(aEntry)
        else
            throw NotImplementedError("cannot edit unknown entry ${aEntry.javaClass}")
    }

    private fun removeFuelEntry(aEntry: Entry): Boolean {
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

    private fun addInspectionEntry(aCarName: String, aEntry: CarInspectionEntry): Boolean {
        return dbHandle.addInspectionEntry(aCarName, aEntry)
    }

    private fun editInspectionEntry(aEntry: CarInspectionEntry): Boolean {
        return dbHandle.editInspectionEntry(aEntry)
    }

    private fun removeInspectionEntry(aEntry: CarInspectionEntry): Boolean {
        return dbHandle.removeEntry(aEntry)
    }
}
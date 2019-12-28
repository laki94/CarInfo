package jo.carinfo

import android.content.Context

class ConfigManager(ctx: Context) {

    private val carsDbHandle = CarsDB(ctx)

    fun saveCars(aCars: CarsList): Boolean {
        return carsDbHandle.saveCars(aCars)
    }

    fun getAllCars(): CarsList {
        return carsDbHandle.getAllCars()
    }

    fun addCar(aCarName: String): Boolean {
        return carsDbHandle.addCar(aCarName)
    }

    fun removeCar(aCarName: String): Boolean {
        return carsDbHandle.removeCar(aCarName)
    }

    fun editCarName(aOldCarName: String, aNewCarName: String): Boolean {
        return carsDbHandle.editCarName(aOldCarName, aNewCarName)
    }
}
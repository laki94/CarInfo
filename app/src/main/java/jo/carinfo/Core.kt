package jo.carinfo

import android.content.Context

class Core(context: Context) {

    private var mAllCars = CarsList()
    private var mCarsInitialized = false

    private val cfgManager = ConfigManager(context)

    fun getAllCars(): CarsList
    {
        if (!mCarsInitialized)
            initializeCars()

        return mAllCars
    }

    fun saveCars(aCarsList: CarsList)
    {
        cfgManager.saveCars(aCarsList)
    }

    private fun initializeCars()
    {
        mAllCars = cfgManager.getCars()
        mCarsInitialized = true
    }
}
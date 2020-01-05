package jo.carinfo

import android.content.Context
import android.util.Log

class Core(context: Context) {

    private lateinit var mAllCars: CarsList
    private var mCarsInitialized = false

    private lateinit var mAllStations: StationList
    private var mStationsInitialized = false

    private val cfgManager = ConfigManager(context)

    fun getAllCars(): CarsList
    {
        if (!mCarsInitialized)
            initializeCars()

        return mAllCars
    }

    fun getAllStations(): StationList {
        if (!mStationsInitialized)
            initializeStations()

        return mAllStations
    }

    fun saveCars(aCarsList: CarsList): Boolean
    {
        Log.d("Core", "saving cars to file")
        return cfgManager.saveCars(aCarsList)
    }

    private fun initializeCars()
    {
        Log.d("Core", "initializing cars")
        mAllCars = cfgManager.getAllCars()
        mCarsInitialized = true
    }

    private fun initializeStations() {
        Log.d("Core", "initializing stations")
        mAllStations = cfgManager.getAllStations()
        mStationsInitialized = true
    }
}
package jo.carinfo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

const val SETTINGS_CLICK = 1
const val FUEL_ENTRY = 2
const val STATIONS_CLICK = 3

class MainActivity : AppCompatActivity() {

    private val mCore = Core(this)

    private lateinit var mCars: CarsList
    private lateinit var mStations: StationList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mCars = mCore.getAllCars()
        mStations = mCore.getAllStations()
    }

    fun onSettingsClick(view : View)
    {
        val intent = Intent(this, SettingsActivity::class.java)
        intent.putExtra("cars", mCars)
        startActivityForResult(intent, SETTINGS_CLICK)
    }

    fun onStationsClick(view: View) {
        val intent = Intent(this, StationsActivity::class.java)
        intent.putExtra("stations", mStations)
        startActivityForResult(intent, STATIONS_CLICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTINGS_CLICK)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                Log.d("Main", "got ok result for settings click")
                val extras = data?.extras
                if (extras != null)
                    if (extras.containsKey("cars"))
                    {
                        mCars.clear()
                        for (car in extras.getSerializable("cars") as ArrayList<Car>)
                        {
                            Log.d("Main", String.format("adding car %s to list", car.mName))
                            mCars.add(car)
                        }
                    }
            }
        }
        else if (requestCode == FUEL_ENTRY)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                Log.d("Main", "got ok result for fuel entry")
                val extras = data?.extras
                if (extras != null)
                {
                    var name = ""
                    var entry: FuelEntry? = null

                    if (extras.containsKey("name"))
                    {
                        name = extras.getSerializable("name") as String
                        Log.d("Main", String.format("got entry for %s", name))
                    }
                    if (extras.containsKey("entry"))
                    {
                        entry = extras.getSerializable("entry") as FuelEntry
                        Log.d("Main", String.format("got entry info %s", entry.getObjectString(this)))
                    }

                    if ((name.isNotEmpty()) && (entry != null))
                        if (mCars.getCarWithName(name) != null) {
                            mCars.getCarWithName(name).let { it?.addEntry(entry) }
                            mCars.let {
                                if (mCore.saveCars(it))
                                    Toast.makeText(this, R.string.carsSavedAfterEntry, Toast.LENGTH_SHORT).show()
                                else
                                    Toast.makeText(this, R.string.carsNotSavedAfterEntry, Toast.LENGTH_SHORT).show()
                            }
                        }
                }

            }
        }
    }

    fun onFuelUsageClick(view: View)
    {
        val intent = Intent(this, FuelUsageGraph::class.java)
        intent.putExtra("cars", mCars)
        startActivity(intent)
    }
}

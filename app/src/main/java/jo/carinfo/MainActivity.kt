package jo.carinfo

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlin.system.exitProcess

const val SETTINGS_CLICK = 1

class MainActivity : AppCompatActivity() {

    private val mCore = Core(this)

    private var mCars: CarsList? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val settings = SharedSettingsClass(this)

        if (settings.isLaunchedForFirstTime()) {
            Log.d("Main", "opened for first time")
            settings.setWasLaunchedForFirstTime()
        }
        else
            Log.d("Main", "not first time")

        mCars = mCore.getAllCars()
    }

    fun onSettingsClick(view : View)
    {
        val intent = Intent(this, SettingsActivity::class.java)
        intent.putExtra("cars", mCars)
        startActivityForResult(intent, SETTINGS_CLICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        if (requestCode == SETTINGS_CLICK)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                val extras = data?.extras
                if (extras != null)
                    if (extras.containsKey("cars"))
                    {
                        mCars?.clear()
                        for (car in extras.getSerializable("cars") as ArrayList<Car>)
                            mCars?.add(car)
                    }
            }
        }
    }

    fun onEntriesClick(view: View)
    {

    }
}

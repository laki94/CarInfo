package jo.carinfo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.util.Log
import android.view.MenuItem

class FuelUsageGraph : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fuel_usage_graph)

//        val drawer = findViewById<DrawerLayout>(R.id.dlFuelUsage)
//        val toggle = ActionBarDrawerToggle(this, drawer, R.string.open, R.string.close)
//
//        drawer.addDrawerListener(toggle)
//        toggle.syncState()

        val nv = findViewById<NavigationView>(R.id.nvFuelDrawer)
        nv.setNavigationItemSelectedListener{
//            drawer.closeDrawers()
//            true
            when (it.itemId) {
                R.id.miCars -> Log.d("aaa", "aaa")
                R.id.miSelectDate -> Log.d("asd", "asd")
                else -> Log.d("zxc", "zzz")
            }
            true
        }
    }
}
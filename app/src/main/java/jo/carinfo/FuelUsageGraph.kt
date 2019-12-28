package jo.carinfo

import android.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View

class FuelUsageGraph : AppCompatActivity() {

    private val mCars = CarsList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fuel_usage_graph)

        val extras = intent.extras
        if (extras != null)
            if (extras.containsKey("cars")) {
                for (car in extras.getSerializable("cars") as ArrayList<Car>)
                    mCars.add(car)
            }

        val nv = findViewById<NavigationView>(R.id.nvFuelDrawer)
        nv.setNavigationItemSelectedListener{
            when (it.itemId) {
                R.id.miCars -> showCars()
                R.id.miSelectDate -> Log.d("asd", "asd")
                else -> Log.d("zxc", "zzz")
            }
            true
        }
    }

    private fun showCars()
    {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater // CHYBA PRZYCISK CARS TO WYJEBANIA, POKAZAC AUTA NA DRAWERZE A NA SAMEJ GORZE WYBOR DATY
        builder.setTitle(R.string.cars)
        val dialogLayout = inflater.inflate(R.layout.dlg_cars_graph, null)
//        val etCarName = dialogLayout.findViewById<EditText>(R.id.etCarName)
        val rv = dialogLayout.findViewById<RecyclerView>(R.id.rvCarsGraph)
        rv.adapter = CarAdapter(this, mCars)
        rv.layoutManager = LinearLayoutManager(this)
//        rv.adapter.notifyDataSetChanged()
        builder.setView(dialogLayout)
        builder.setPositiveButton(R.string.save) { _, _ -> } //addAndRefreshList(etCarName.text.toString()) }
        builder.show()
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.dlFuelUsage)
        if (drawer.isDrawerOpen(Gravity.START))
            drawer.closeDrawers()
        else
            super.onBackPressed()
    }
}
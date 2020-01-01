package jo.carinfo

import android.app.AlertDialog
import android.graphics.Color
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
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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

        showGraphPoints()
//        val nv = findViewById<NavigationView>(R.id.nvFuelDrawer)
//        nv.setNavigationItemSelectedListener{
//            when (it.itemId) {
//                R.id.miCars -> showCars()
//                R.id.miSelectDate -> Log.d("asd", "asd")
//                else -> Log.d("zxc", "zzz")
//            }
//            true
//        }
    }

    private fun showGraphPoints() {
        var series = LineGraphSeries<DataPoint>()

        val fuel_graph = findViewById<GraphView>(R.id.gvFuel)

        fuel_graph.gridLabelRenderer.horizontalAxisTitle = "Data"
        fuel_graph.gridLabelRenderer.verticalAxisTitle = "Spalanie"

        fuel_graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(this, SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()))
        fuel_graph.viewport.setMaxX(100.0)
          val rnd = Random()
        for (car in mCars) {
            series.color = car.mChartColor
            for (single_entry in car.mFuelEntries) {
                series.appendData(DataPoint(single_entry.mDate, single_entry.getAvgFuelConsumption()), true, 40)
            }
            fuel_graph.addSeries(series)
        }
        fuel_graph.viewport.isXAxisBoundsManual = true
        fuel_graph.viewport.isScalable = true
        fuel_graph.viewport.isScrollable = true
//
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
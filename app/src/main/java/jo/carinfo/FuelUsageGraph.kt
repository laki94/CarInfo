package jo.carinfo

import android.app.AlertDialog
import android.content.Intent
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
import com.jjoe64.graphview.series.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.round

class FuelUsageGraph : AppCompatActivity() {

    private val mCars = CarsListGraph()
    private var mAdapter: CarGraphAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fuel_usage_graph)

        val extras = intent.extras
        if (extras != null)
            if (extras.containsKey("cars")) {
                for (car in extras.getSerializable("cars") as ArrayList<Car>)
                    mCars.add(car)
            }

        showCars()
    }

    private fun clearGraphPoints() {
        val fuel_graph = findViewById<GraphView>(R.id.gvFuel)
        fuel_graph.removeAllSeries()
    }

    private fun showGraphPoints() {
        var series: LineGraphSeries<DataPoint>
        val fuel_graph = findViewById<GraphView>(R.id.gvFuel)
        var maxY = 10.0

        fuel_graph.gridLabelRenderer.horizontalAxisTitle = "Data"
        fuel_graph.gridLabelRenderer.verticalAxisTitle = "Spalanie"
        fuel_graph.gridLabelRenderer.numHorizontalLabels = 4
        fuel_graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(this, SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()))

        for (car in mCars) {
            if (!mCars.isCarHidden(car)) {
                for (entry in car.mFuelEntries)
                if (maxY < entry.getAvgFuelConsumption())
                    maxY = entry.getAvgFuelConsumption()
            }
        }
        fuel_graph.viewport.setMaxY(maxY)

        for (car in mCars) {
            if (!mCars.isCarHidden(car)) {
                series = LineGraphSeries()
                series.isDrawDataPoints = true
                series.color = car.mChartColor
                series.setOnDataPointTapListener(object : OnDataPointTapListener {
                    override fun onTap(series: Series<*>?, dataPoint: DataPointInterface?) {
                        Log.d("series", String.format("%f, %.2f", dataPoint?.x, dataPoint?.y))
                    }
                })
                for (single_entry in car.mFuelEntries)
                    series.appendData(DataPoint(single_entry.mDate, single_entry.getAvgFuelConsumption()), true, 1000)

                fuel_graph.addSeries(series)
            }
        }
        fuel_graph.viewport.isXAxisBoundsManual = true
        fuel_graph.viewport.isScalable = true
        fuel_graph.viewport.isScrollable = true
    }


    private fun showCars()
    {
        val listView = findViewById<RecyclerView>(R.id.rvCarsGraph)

        mAdapter = CarGraphAdapter(this, mCars)

        mAdapter.let {
            it?.onItemClick = { it, isChecked ->
                mAdapter?.changeItemVisibility(it, isChecked)
                clearGraphPoints()
                showGraphPoints()
            }
        }

        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = mAdapter
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.dlFuelUsage)
        if (drawer.isDrawerOpen(Gravity.START))
            drawer.closeDrawers()
        else
            super.onBackPressed()
    }
}
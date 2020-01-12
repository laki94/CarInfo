package jo.carinfo

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.*
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FuelUsageGraph : AppCompatActivity() {

    private val TAG = "GRAPH"
    private val mCars = CarsListGraph()
    private var mChartType = ChartType.FUEL_USAGE
    private lateinit var mAdapter: CarGraphAdapter
    private lateinit var mFuelGraph: GraphView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fuel_usage_graph)

        val extras = intent.extras
        if (extras != null)
            if (extras.containsKey("cars")) {
                for (car in extras.getSerializable("cars") as ArrayList<Car>)
                    mCars.add(car)
            }

        mFuelGraph = findViewById(R.id.gvFuel)
        initGraph()
        val spinner = findViewById<Spinner>(R.id.sChartTypes)

        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                return
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                Log.i(TAG, "selected $p2 item, refreshing series")
                when (p2) {
                    0 -> refreshSeries(ChartType.FUEL_USAGE)
                    1 -> refreshSeries(ChartType.COST_PER_LITER)
                    2 -> refreshSeries(ChartType.TOTAL_COST)
                    else -> throw NotImplementedError("chart type $p2 not implemented")
                }
            }
        }
        spinner.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, resources.getStringArray(R.array.ChartTypes))

        showCars()
    }

    private fun initGraph() {
        mFuelGraph.gridLabelRenderer.numHorizontalLabels = 4
        mFuelGraph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(this, SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()))
    }

    private fun clearGraphPoints() {
        mFuelGraph.removeAllSeries()
    }

    private fun showFuelUsageOnGraph() {
        var series: LineGraphSeries<DataPoint>
        mFuelGraph.gridLabelRenderer.verticalAxisTitle = resources.getString(R.string.FuelConsumptionChart)

        for (car in mCars) {
            if (!mCars.isCarHidden(car)) {
                car.mFuelEntries.sortByDate()
                series = LineGraphSeries()
                series.isDrawDataPoints = true
                series.color = car.mChartColor
                series.setOnDataPointTapListener(object : OnDataPointTapListener {
                    override fun onTap(series: Series<*>?, dataPoint: DataPointInterface?) {
                        Log.d("series", String.format("%f, %.2f", dataPoint?.x, dataPoint?.y))
                    }
                })
                for (single_entry in car.mFuelEntries)
                    series.appendData(DataPoint(single_entry.mDate.toDate(), single_entry.getAvgFuelConsumption()), true, 1000)

                mFuelGraph.addSeries(series)
            }
        }
    }

    private fun showTotalCostOnGraph() {
        var series: LineGraphSeries<DataPoint>
        mFuelGraph.gridLabelRenderer.verticalAxisTitle = resources.getString(R.string.TotalCostChart)

        for (car in mCars) {
            if (!mCars.isCarHidden(car)) {
                car.mFuelEntries.sortByDate()
                series = LineGraphSeries()
                series.isDrawDataPoints = true
                series.color = car.mChartColor
                series.setOnDataPointTapListener(object: OnDataPointTapListener {
                    override fun onTap(series: Series<*>?, dataPoint: DataPointInterface?) {
                        Log.d("series", String.format("%f, %.2f", dataPoint?.x, dataPoint?.y))
                    }
                })
                for (single_entry in car.mFuelEntries)
                    series.appendData(DataPoint(single_entry.mDate.toDate(), single_entry.mPerLiter * single_entry.mFuelAmount), false, 1000)

                mFuelGraph.addSeries(series)
            }
        }
    }

    private fun showCostPerLiterOnGraph() {
        var series: LineGraphSeries<DataPoint>
        mFuelGraph.gridLabelRenderer.verticalAxisTitle = resources.getString(R.string.CostPerLiterChart)

        for (car in mCars) {
            if (!mCars.isCarHidden(car)) {
                car.mFuelEntries.sortByDate()
                series = LineGraphSeries()
                series.isDrawDataPoints = true
                series.color = car.mChartColor
                series.setOnDataPointTapListener(object: OnDataPointTapListener {
                    override fun onTap(series: Series<*>?, dataPoint: DataPointInterface?) {
                        Log.d("series", String.format("%f, %.2f", dataPoint?.x, dataPoint?.y))
                    }
                })
                for (single_entry in car.mFuelEntries)
                    series.appendData(DataPoint(single_entry.mDate.toDate(), single_entry.mPerLiter), false, 1000)

                mFuelGraph.addSeries(series)
            }
        }
    }

    private fun addPointAtTheEnd() {

        var maxX = 0.0
        var maxY = 0.0
        for (simpleSeries in mFuelGraph.series) {
            if (maxX < simpleSeries.highestValueX)
                maxX = simpleSeries.highestValueX
            if (maxY < simpleSeries.highestValueY)
                maxY = simpleSeries.highestValueY
        }
        val newDate = DateTime(maxX.toLong()).plusWeeks(1)

        val series = LineGraphSeries<DataPoint>()
        series.isDrawDataPoints = false
        series.appendData(DataPoint(newDate.millis.toDouble(), maxY), false, 1)
        mFuelGraph.addSeries(series)
    }

    private fun showGraphPoints() {
        when (mChartType) {
            ChartType.FUEL_USAGE -> showFuelUsageOnGraph()
            ChartType.COST_PER_LITER -> showCostPerLiterOnGraph()
            ChartType.TOTAL_COST -> showTotalCostOnGraph()
            else -> throw NotImplementedError("cannot show graph points for $mChartType")
        }
        addPointAtTheEnd()
        mFuelGraph.viewport.isXAxisBoundsManual = true
        mFuelGraph.viewport.isScalable = true
        mFuelGraph.viewport.isScrollable = true
        mFuelGraph.viewport.scrollToEnd()
    }

    private fun showCars()
    {
        val listView = findViewById<RecyclerView>(R.id.rvCarsGraph)

        mAdapter = CarGraphAdapter(this, mCars)

        mAdapter.let {
            it.onItemClick = { it, isChecked ->
                mAdapter.changeItemVisibility(it, isChecked)
                refreshSeries(mChartType)
            }
        }

        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = mAdapter
    }

    private fun refreshSeries(aChartType: ChartType) {
        mChartType = aChartType
        clearGraphPoints()
        showGraphPoints()
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.dlFuelUsage)
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawers()
        else
            super.onBackPressed()
    }
}
package jo.carinfo

import android.app.AlertDialog
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

class SettingsActivity : AppCompatActivity() {

    class CarsAdapter(private val context: Context,
                      private val dataSource: ArrayList<Car>) : BaseAdapter()
    {
        private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {

            val rowView = inflater.inflate(R.layout.lvcars_item, p2, false)
            val carName = rowView.findViewById(R.id.lvCarsTv) as TextView

            val simpleCar = getItem(p0) as Car
            carName.text = simpleCar.mName

            return rowView
        }

        override fun getItem(p0: Int): Any {
            return dataSource[p0]
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        override fun getCount(): Int {
            return dataSource.count()
        }

    }

    private var carsList = CarsList()

    private var adapter: CarsAdapter? = null


    private fun addAndRefreshList(aCarName: String){
        if (aCarName.isNotEmpty()){
            carsList.add(Car(aCarName))

            adapter?.notifyDataSetChanged()
        }
    }

    fun onCarAddClick(view : View) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle(R.string.creatingCar)
        val dialogLayout = inflater.inflate(R.layout.activity_dlg_add_car, null)
        val etCarName = dialogLayout.findViewById<EditText>(R.id.etCarName)
        builder.setView(dialogLayout)
        builder.setPositiveButton(R.string.save) { _, _ -> addAndRefreshList(etCarName.text.toString()) }
        builder.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        adapter = CarsAdapter(this, carsList)
        var listview = findViewById<ListView>(R.id.lvCars)
        listview.adapter = adapter
    }


}

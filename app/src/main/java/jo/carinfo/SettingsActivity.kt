package jo.carinfo

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.ColorSpace
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

class SettingsActivity : AppCompatActivity() {

    private var carsList = CarsList()

    private var adapter: CarAdapter? = null

    private fun addAndRefreshList(aCarName: String){
        if (aCarName.isNotEmpty()){
            carsList.add(Car(aCarName))

            adapter?.notifyItemChanged(carsList.count() - 1)
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

        var listView = findViewById<RecyclerView>(R.id.rvCars)

        adapter = CarAdapter(this, carsList)
        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = adapter
    }
}

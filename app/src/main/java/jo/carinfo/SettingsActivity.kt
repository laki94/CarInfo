package jo.carinfo

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.*

class SettingsActivity : AppCompatActivity() {

    private val carsList = CarsList()

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

    override fun onBackPressed() {
        when (adapter?.isEditing())
        {
            true -> { adapter?.stopEditing() }
            else -> {
                val intent = Intent()
                intent.putExtra("cars", carsList)
                setResult(Activity.RESULT_OK, intent)
                super.onBackPressed()
            }
        }
    }

    fun onCarRemoveClick(view: View)
    {
        val checkedIds = adapter?.getCheckedIds()
        if (!checkedIds.isNullOrEmpty())
        {
            checkedIds.sort()
            for (id in checkedIds.asReversed())
            {
                carsList.removeAt(id)
            }

            when (carsList.isEmpty())
            {
                true -> { adapter?.stopEditing() }
                else -> { adapter?.notifyDataSetChanged() }
            }
        }
    }

    fun onSaveSettings(view: View)
    {
        val cfgManager = ConfigManager(this)
        if (!cfgManager.saveCars(carsList))
            Toast.makeText(this, R.string.couldNotSaveCars, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        var listView = findViewById<RecyclerView>(R.id.rvCars)
        val extras = intent.extras
        if (extras != null)
            if (extras.containsKey("cars"))
            {
                for (car in extras.getSerializable("cars") as ArrayList<Car>)
                    carsList.add(car)
            }

        adapter = CarAdapter(this, carsList)
        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = adapter
    }
}

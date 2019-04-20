package jo.carinfo

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.*

const val FUEL_ENTRY_CLICK = 0

class SettingsActivity : AppCompatActivity() {

    private val originalCarsList = CarsList()
    private var newCarsList = CarsList()
    private var adapter: CarAdapter? = null

    private fun addAndRefreshList(aCarName: String) {
        if (aCarName.isNotEmpty()) {
            newCarsList.add(Car(aCarName))
            adapter?.notifyItemChanged(newCarsList.count() - 1)
        }
    }

    fun onCarAddClick(view: View) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle(R.string.creatingCar)
        val dialogLayout = inflater.inflate(R.layout.dlg_add_car, null)
        val etCarName = dialogLayout.findViewById<EditText>(R.id.etCarName)
        builder.setView(dialogLayout)
        builder.setPositiveButton(R.string.save) { _, _ -> addAndRefreshList(etCarName.text.toString()) }
        builder.show()
    }

    override fun onBackPressed() {
        when (adapter?.isEditing()) {
            true -> {
                adapter?.stopEditing()
            }
            else -> {
                askForSaveCarsAndQuit()
            }
        }
    }

    private fun askForSaveCarsAndQuit() {
        var doAsk = newCarsList.count() != originalCarsList.count()

        if (!doAsk)
            for (car in newCarsList) {
                if (originalCarsList.indexOf(car) == -1) {
                    doAsk = true
                    break
                }
            }

        if (doAsk) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.dataChangedWantSave)
            builder.setPositiveButton(R.string.Yes) { _, _ ->
                saveCars()
                closeActivity(true)
            }

            builder.setNegativeButton(R.string.No) { _, _ ->
                closeActivity(false)
            }

            builder.setNeutralButton(R.string.Cancel) { _, _ -> }
            builder.show()
        } else
            closeActivity(false)
    }

    private fun closeActivity(aReturnCars: Boolean) {
        val intent = Intent()
        if (aReturnCars) {
            intent.putExtra("cars", newCarsList)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    fun onCarRemoveClick(view: View) {
        val checkedIds = adapter?.getCheckedIds()
        if (!checkedIds.isNullOrEmpty()) {
            checkedIds.sort()
            for (id in checkedIds.asReversed()) {
                newCarsList.removeAt(id)
            }

            when (newCarsList.isEmpty()) {
                true -> {
                    adapter?.stopEditing()
                }
                else -> {
                    adapter?.notifyDataSetChanged()
                }
            }
        }
    }

    private fun saveCars() {
        val cfgManager = ConfigManager(this)
        if (!cfgManager.saveCars(newCarsList))
            Toast.makeText(this, R.string.couldNotSaveCars, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val listView = findViewById<RecyclerView>(R.id.rvCars)
        val extras = intent.extras
        if (extras != null)
            if (extras.containsKey("cars")) {
                for (car in extras.getSerializable("cars") as ArrayList<Car>)
                    originalCarsList.add(car)
                newCarsList = originalCarsList.clone() as CarsList
            }

        adapter = CarAdapter(this, newCarsList)

        adapter.let {
            it?.onItemClick = { it ->
                val intent = Intent(this, CarEntries::class.java)
                intent.putExtra("car", it)
                startActivityForResult(intent, FUEL_ENTRY_CLICK)
            }
        }

        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FUEL_ENTRY_CLICK) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("CarsActivity", "got ok result for fuel entry click")
                val extras = data?.extras
                if (extras != null)
                    if (extras.containsKey("car"))
                    {
                        val car = extras.getSerializable("car") as Car
                        newCarsList[newCarsList.indexOf(car.mName)] = car
                    }

            }
        }
    }
}
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
    private var adapter: CarAdapter? = null

    private fun addAndRefreshList(aCarName: String) {
        val cfgManager = ConfigManager(this)
        if (cfgManager.addCar(aCarName)) {
            originalCarsList.add(Car(aCarName))
            adapter?.notifyItemChanged(originalCarsList.count() - 1)
        }
        else
            Toast.makeText(this, R.string.couldNotSaveCars, Toast.LENGTH_SHORT).show()
    }

    private fun editCarOnList(aOldCarName: String, aNewCarName: String) {
        val cfgManager = ConfigManager(this)
        if (cfgManager.editCarName(aOldCarName, aNewCarName)) {
            originalCarsList.changeName(aOldCarName, aNewCarName)
            adapter?.notifyItemChanged(originalCarsList.indexOf(aNewCarName))
        }
        else
            Toast.makeText(this, R.string.couldNotSaveCars, Toast.LENGTH_SHORT).show()
    }

    fun onEditCarClick(view: View) {
        if (view is TextView)
            createAddCarDialog(view.text.toString())
    }

    private fun createAddCarDialog(aStartText: String = "")
    {
        val editing = aStartText.isNotEmpty()
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dlg_add_car, null)
        val etCarName = dialogLayout.findViewById<EditText>(R.id.etCarName)
        if (editing) {
            builder.setTitle(R.string.editingCar)
            builder.setPositiveButton(R.string.save) { _, _ ->  }
        }
        else {
            builder.setTitle(R.string.creatingCar)
            builder.setPositiveButton(R.string.save) { _, _ ->  }
        }
        etCarName.setText(aStartText)
        etCarName.setSelection(etCarName.text.length)
        builder.setView(dialogLayout)
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (parseUserInput(etCarName.text.toString())) {
                if (editing) {
                    if (originalCarsList.indexOf(aStartText) == -1) {
                        Toast.makeText(this, R.string.unknownError, Toast.LENGTH_SHORT).show()
                    }
                    else {
                        editCarOnList(aStartText, etCarName.text.toString())
                    }
                } else {
                    addAndRefreshList(etCarName.text.toString())
                }
                dialog.dismiss()
            }
        }
    }

    private fun parseUserInput(aInput: String): Boolean {
        if (originalCarsList.indexOf(aInput) != -1) {
            Toast.makeText(this, R.string.carNameAlreadyExists, Toast.LENGTH_SHORT).show()
        }
        else if (aInput.length > 45) {
            Toast.makeText(this, R.string.carNameTooLong, Toast.LENGTH_SHORT).show()
        }
        else if (aInput.isEmpty()) {
            Toast.makeText(this, R.string.invalidCarName, Toast.LENGTH_SHORT).show()
        }
        else
            return true
        return false
    }

    fun onCarAddClick(view: View) {
        createAddCarDialog()
    }

    override fun onBackPressed() {
        when (adapter?.isEditing()) {
            true -> {
                adapter?.stopEditing()
            }
            else -> {
                closeActivity(true)
            }
        }
    }

    private fun closeActivity(aReturnCars: Boolean) {
        val intent = Intent()
        if (aReturnCars) {
            intent.putExtra("cars", originalCarsList)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    fun onCarRemoveClick(view: View) {
        val checkedIds = adapter?.getCheckedIds()
        var wasErr = false
        if (!checkedIds.isNullOrEmpty()) {
            checkedIds.sort()
            for (id in checkedIds.asReversed()) {
                val cfgManager = ConfigManager(this)
                if (cfgManager.removeCar(originalCarsList[id].mName)) {
                    originalCarsList.removeAt(id)
                }
                else {
                    wasErr = true
                }
            }
        if (wasErr)
            Toast.makeText(this, R.string.couldNotRemoveCars, Toast.LENGTH_SHORT).show()

            when (originalCarsList.isEmpty()) {
                true -> {
                    adapter?.stopEditing()
                }
                else -> {
                    adapter?.notifyDataSetChanged()
                }
            }
        }
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
            }

        adapter = CarAdapter(this, originalCarsList)

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
                        originalCarsList[originalCarsList.indexOf(car.mName)] = car
                    }

            }
        }
    }
}
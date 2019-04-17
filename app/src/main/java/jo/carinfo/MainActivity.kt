package jo.carinfo

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioGroup

const val SETTINGS_CLICK = 1
const val FUEL_ENTRY = 2
const val OIL_ENTRY = 3

class MainActivity : AppCompatActivity() {

    private val mCore = Core(this)

    private var mCars: CarsList? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mCars = mCore.getAllCars()
    }

    fun onSettingsClick(view : View)
    {
        val intent = Intent(this, SettingsActivity::class.java)
        intent.putExtra("cars", mCars)
        startActivityForResult(intent, SETTINGS_CLICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        if (requestCode == SETTINGS_CLICK)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                val extras = data?.extras
                if (extras != null)
                    if (extras.containsKey("cars"))
                    {
                        mCars?.clear()
                        for (car in extras.getSerializable("cars") as ArrayList<Car>)
                            mCars?.add(car)
                    }
            }
        }
        else if (requestCode == FUEL_ENTRY)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                val extras = data?.extras
                if (extras != null)
                {
                    var name = ""
                    var entry: FuelEntry? = null

                    if (extras.containsKey("name"))
                        name = extras.getSerializable("name") as String
                    if (extras.containsKey("entry"))
                        entry = extras.getSerializable("entry") as FuelEntry

                    if ((name.isNotEmpty()) && (entry != null))
                        if (mCars?.getCarWithName(name) != null) {
                            mCars?.getCarWithName(name).let { it?.addEntry(entry) }
                            mCars?.let { mCore.saveCars(it) }
                        }
                }

            }
        }
    }

    fun onEntriesClick(view: View)
    {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle(R.string.entryType)
        val dialogLayout = inflater.inflate(R.layout.dlg_select_entry_type, null)
        val rg = dialogLayout.findViewById<RadioGroup>(R.id.rgEntryTypes)
        builder.setView(dialogLayout)
        builder.setPositiveButton(R.string.save) { _, _ -> createNewEntry(getEntryType(rg.checkedRadioButtonId)) }
        builder.show()
    }

    private fun getEntryType(aRadioId: Int): EntryType
    {
        var type = EntryType.Unknown
        when (aRadioId)
        {
            R.id.rbFuel -> type = EntryType.Fuel
            R.id.rbOil -> type = EntryType.Oil
        }
        return type
    }

    private fun createNewEntry(aEntryType: EntryType)
    {
        when (aEntryType)
        {
            EntryType.Fuel -> { createFuelEntry() }
            EntryType.Oil -> { }
            else -> { }
        }
    }

    private fun createFuelEntry()
    {
        val intent = Intent(this, FuelEntryActivity::class.java)
        intent.putExtra("cars", mCars)
        startActivityForResult(intent, FUEL_ENTRY)
    }
}

package jo.carinfo

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import java.lang.String.format
import java.util.*

class CarEntries : AppCompatActivity() {

    private var mainCar: Car? = null
    private var entriesAdapter: EntriesAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_entries)

        val extras = intent.extras
        if (extras != null)
            if (extras.containsKey("car"))
                mainCar = extras.getSerializable("car") as Car

        if (mainCar != null)
        {
            val title = findViewById<TextView>(R.id.tvCarEntryTitle)
            title.text = format("%s %s", getString(R.string.entriesFor), mainCar?.mName)
        }

        val listView = findViewById<RecyclerView>(R.id.rvEntries)

        entriesAdapter = EntriesAdapter(this, mainCar?.mFuelEntries!!)

        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = entriesAdapter
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("car", mainCar)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    fun onEntryAddClick(view: View) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle(R.string.entryType)
        val dialogLayout = inflater.inflate(R.layout.dlg_select_entry_type, null)
        val rg = dialogLayout.findViewById<RadioGroup>(R.id.rgEntryTypes)
        builder.setView(dialogLayout)
        builder.setPositiveButton(R.string.Next) { _, _ -> createNewEntry(getEntryType(rg.checkedRadioButtonId)) }
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
            EntryType.Oil -> { createOilEntry() }
            else -> { }
        }
    }

    private fun createFuelEntry()
    {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.activity_fuel_entry, null)

        builder.setView(dialogLayout)
        builder.setPositiveButton(R.string.save) {_, _ ->}

        val dialog: AlertDialog = builder.create()
        dialog.show()

        val btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        btn.setOnClickListener {
            val odo = dialogLayout.findViewById<TextView>(R.id.etOdometer)
            val mil = dialogLayout.findViewById<TextView>(R.id.etMileage)
            val fuelAm = dialogLayout.findViewById<TextView>(R.id.etFuelAmount)
            val perLit = dialogLayout.findViewById<TextView>(R.id.etPerLiter)

            val date = Calendar.getInstance().time
            var odoVal = 0
            var milVal = 0
            var fuelAmVal = 0.0
            var perLitVal = 0.0
            var lastError = ""


            var haveMileage = false

            if (odo.text.toString().toIntOrNull() != null)
            {
                odoVal = odo.text.toString().toInt()
                haveMileage = true
            }

            if (mil.text.toString().toIntOrNull() != null)
            {
                milVal = mil.text.toString().toInt()
                haveMileage = true
            }

            if (haveMileage)
            {
                if (fuelAm.text.toString().toDoubleOrNull() == null)
                    lastError = getString(R.string.fuelAmountInputError)
                else
                {
                    fuelAmVal = String.format(Locale.ROOT, "%.2f", fuelAm.text.toString().toDouble()).toDouble()

                    if (perLit.text.toString().toDoubleOrNull() == null)
                        lastError = getString(R.string.perLiterInputError)
                    else
                        perLitVal = String.format(Locale.ROOT, "%.2f", perLit.text.toString().toDouble()).toDouble()
                }
            }
            else
                lastError = getString(R.string.mileageInputError)

            if (lastError.isEmpty()) {
                mainCar?.addEntry(FuelEntry(date, odoVal, milVal, fuelAmVal, perLitVal))
                dialog.dismiss()
                entriesAdapter?.notifyDataSetChanged()
            } else
                Toast.makeText(this, lastError, Toast.LENGTH_SHORT).show()
        }
    }

    private fun createOilEntry() {

    }
}

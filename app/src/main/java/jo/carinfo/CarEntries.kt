package jo.carinfo

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import java.lang.String.format
import java.util.*

class CarEntries : AppCompatActivity() {

    private var mainCar: Car? = null
    private var entriesAdapter: EntriesAdapter? = null

    private val mAllEntries = ArrayList<Entry>()

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

            mAllEntries.addAll(mainCar?.mFuelEntries!!.asIterable())
            mAllEntries.addAll(mainCar?.mOilEntries!!.asIterable())
            mAllEntries.sortBy { it.mDate }
        }

        val listView = findViewById<RecyclerView>(R.id.rvEntries)

        entriesAdapter = EntriesAdapter(this, mAllEntries)

        entriesAdapter.let {
            it?.onItemClick = {
                editEntry(it)
            }
        }

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

    private fun editEntry(aEntry: Entry): Entry {
        if (aEntry is FuelEntry)
            createFuelEntry(aEntry)
        return aEntry
    }

    private fun createNewEntry(aEntryType: EntryType)
    {
        when (aEntryType)
        {
            EntryType.Fuel -> { createFuelEntry(null) }
            EntryType.Oil -> { createOilEntry() }
            else -> { }
        }
    }

    private fun createFuelEntry(aEntry: FuelEntry?)
    {
        val editing = aEntry != null
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.activity_fuel_entry, null)

        builder.setView(dialogLayout)
        builder.setPositiveButton(R.string.save) {_, _ ->}

        val dialog: AlertDialog = builder.create()
        dialog.show()

        val btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

        val odo = dialogLayout.findViewById<TextView>(R.id.etOdometer)
        val mil = dialogLayout.findViewById<TextView>(R.id.etMileage)
        val fuelAm = dialogLayout.findViewById<TextView>(R.id.etFuelAmount)
        val perLit = dialogLayout.findViewById<TextView>(R.id.etPerLiter)

        if (editing)
        {
            if (aEntry?.mOdometer != 0)
                odo.text = aEntry?.mOdometer.toString()
            if (aEntry?.mMileage != 0)
                mil.text = aEntry?.mMileage.toString()
            fuelAm.text = aEntry?.mFuelAmount.toString()
            perLit.text = aEntry?.mPerLiter.toString()
        }

        btn.setOnClickListener {

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
                val cfgManager = ConfigManager(this)
                val entry = FuelEntry(date, odoVal, milVal, fuelAmVal, perLitVal)
                if (editing) {
                    entry.mId = aEntry!!.mId
                    if (cfgManager.editFuelEntry(entry)) {
                        mainCar?.editEntry(entry)
                        dialog.dismiss()

                        for (tmpentry in mAllEntries) {
                            if (tmpentry.mId == entry.mId) {
                                (tmpentry as FuelEntry).mMileage = entry.mMileage
                                tmpentry.mFuelAmount = entry.mFuelAmount
                                tmpentry.mPerLiter = entry.mPerLiter
                                tmpentry.mOdometer = entry.mOdometer
                                break
                            }
                        }
                        entriesAdapter?.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this, R.string.unknownError, Toast.LENGTH_SHORT).show()
                    }
                }
                else {
                    if (cfgManager.addFuelEntry(mainCar!!.mName, entry)) {
                        mainCar?.addEntry(entry)
                        dialog.dismiss()
                        mAllEntries.add(entry)
                        entriesAdapter?.notifyDataSetChanged()
                    }
                    else
                        Toast.makeText(this, R.string.unknownError, Toast.LENGTH_SHORT).show()
                }
            } else
                Toast.makeText(this, lastError, Toast.LENGTH_SHORT).show()
        }
    }

    private fun createOilEntry() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.activity_oil_entry, null)

        builder.setView(dialogLayout)
        builder.setPositiveButton(R.string.save) {_, _ ->}

        val dialog: AlertDialog = builder.create()
        dialog.show()

        val btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        btn.setOnClickListener {
            val mil = dialogLayout.findViewById<EditText>(R.id.etMileage)
            val remindAfter = dialogLayout.findViewById<EditText>(R.id.etRemindAfter)
            var lastError = ""
            val date = Calendar.getInstance().time
            var milVal = 0
            var remAfterVal = 0

            if (mil.text.toString().toIntOrNull() == null)
                lastError = getString(R.string.mileageInputError)
            else {
                milVal = mil.text.toString().toInt()
                if (remindAfter.text.toString().toIntOrNull() == null)
                    lastError = getString(R.string.remindAfterInputError)
                else
                    remAfterVal = remindAfter.text.toString().toInt()
            }

            if (lastError.isEmpty()) {
                val entry = OilEntry(date, milVal, remAfterVal)
                mainCar?.addEntry(entry)
                dialog.dismiss()
                mAllEntries.add(entry)
                entriesAdapter?.notifyDataSetChanged()
            } else
                Toast.makeText(this, lastError, Toast.LENGTH_SHORT).show()
        }
    }
}

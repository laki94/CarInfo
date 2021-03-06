package jo.carinfo

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AlertDialogLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Months
import java.lang.String.format
import java.util.*

class CarEntries : AppCompatActivity() {

    private lateinit var mainCar: Car
    private lateinit var entriesAdapter: EntriesAdapter
    private lateinit var mSummaryDateFrom: DateTime
    private lateinit var mSummaryDateTo: DateTime

    private val mAllEntries = ArrayList<Entry>()
    private val p = Paint()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_entries)

        val extras = intent.extras
        if (extras != null)
            if (extras.containsKey("car"))
                mainCar = extras.getSerializable("car") as Car

        val title = findViewById<TextView>(R.id.tvCarEntryTitle)
        title.text = format("%s %s", getString(R.string.entriesFor), mainCar.mName)
        mainCar.mFuelEntries.sortByDate()
        mAllEntries.addAll(mainCar.mFuelEntries.asIterable())
        if (mainCar.mInspection != null)
            mAllEntries.add(mainCar.mInspection!!)

        setSummaryButtonVisibility()

        val listView = findViewById<RecyclerView>(R.id.rvEntries)

        entriesAdapter = EntriesAdapter(this, mAllEntries)

        entriesAdapter.let {
            it.onItemClick = {
                editEntry(it)
            }
        }

        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = entriesAdapter

        enableSwipe()
    }

    private fun setSummaryButtonVisibility() {
        val bSummary = findViewById<Button>(R.id.bSummary)
        if (mainCar.mFuelEntries.count() == 0)
            bSummary.visibility = View.GONE
        else
            bSummary.visibility = View.VISIBLE
    }

    private fun enableSwipe() {
        val listView = findViewById<RecyclerView>(R.id.rvEntries)
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition

                if (direction == ItemTouchHelper.LEFT) {
                    val delEntry = mAllEntries[pos]
                    entriesAdapter.removeItem(pos)
                    if (delEntry is FuelEntry) {
                        mainCar.mFuelEntries.remove(delEntry)
                        setSummaryButtonVisibility()
                    }
                    else if (delEntry is CarInspectionEntry)
                        mainCar.mInspection = null
                    val snackbar = Snackbar.make(
                        findViewById(R.id.entries_layout),
                        R.string.entryRemoved,
                        Snackbar.LENGTH_INDEFINITE
                    )
                    snackbar.setAction("UNDO") {
                        entriesAdapter.restoreItem(delEntry, pos)
                        if (delEntry is FuelEntry) {
                            mainCar.mFuelEntries.add(delEntry)
                            setSummaryButtonVisibility()
                        }
                        else if (delEntry is CarInspectionEntry)
                            mainCar.mInspection = delEntry
                    }
                    snackbar.addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            if (event != DISMISS_EVENT_ACTION) {
                                removeEntryOnList(delEntry)
                            }
                            super.onDismissed(transientBottomBar, event)
                        }
                    })
                    snackbar.setActionTextColor(Color.YELLOW)
                    snackbar.show()
                } else if (direction == ItemTouchHelper.RIGHT) {
                    val edEntry = mAllEntries[pos]
                    editEntry(edEntry)
                    entriesAdapter.refreshItem(pos)
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val icon: Bitmap
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView
                    val height = itemView.bottom.toFloat() - itemView.top.toFloat()
                    val width = height / 3

                    if (dX > 0) {
                        p.color = Color.parseColor("#63f542")
                        val background = RectF(itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat())
                        c.drawRect(background, p)
                        icon = BitmapFactory.decodeResource(resources, R.drawable.edit)
                        val icon_dest = RectF(itemView.left.toFloat() + width, itemView.top.toFloat() + width, itemView.left.toFloat() + 2*width, itemView.bottom.toFloat() - width)
                        c.drawBitmap(icon, null, icon_dest, p)
                    } else {
                        p.color = Color.parseColor("#D32F2F")
                        val background = RectF(
                            itemView.right.toFloat() + dX,
                            itemView.top.toFloat(),
                            itemView.right.toFloat(),
                            itemView.bottom.toFloat()
                        )
                        c.drawRect(background, p)
                        icon = BitmapFactory.decodeResource(resources, R.drawable.delete)
                        val icon_dest = RectF(
                            itemView.right.toFloat() - 2 * width,
                            itemView.top.toFloat() + width,
                            itemView.right.toFloat() - width,
                            itemView.bottom.toFloat() - width
                        )
                        c.drawBitmap(icon, null, icon_dest, p)
                    }
                }

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(listView)
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

    private fun getEntryType(aButtonId: Int): EntryType {
        when (aButtonId) {
            R.id.rbFuel -> return EntryType.Fuel
            R.id.rbCarInspection -> return EntryType.Inspection
            else -> return EntryType.Unknown
        }
    }

    private fun createInspectionEntry(aEntry: CarInspectionEntry?) {
        val editing = aEntry != null
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.activity_car_inspection, null)

        builder.setView(dialogLayout)
        builder.setPositiveButton(R.string.save) {_, _ ->}

        val dialog: AlertDialog = builder.create()
        dialog.show()

        val btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

        val cvLastInspection = dialogLayout.findViewById<CalendarView>(R.id.cvLastInspection)
        val rgReminders = dialogLayout.findViewById<RadioGroup>(R.id.rgReminders)

        if (editing)
        {
            cvLastInspection.date = aEntry!!.mLastInspectionDate.toDate().time
            when (aEntry.mRemindAfter) {
                InspectionRemindAfter.YEAR -> rgReminders.check(R.id.rbRemindInOneYear)
                InspectionRemindAfter.TWO_YEARS -> rgReminders.check(R.id.rbRemindInTwoYears)
                InspectionRemindAfter.THREE_YEARS -> rgReminders.check(R.id.rbRemindInThreeYears)
                else -> throw NotImplementedError("could not select remind after radio button for unknown reminder ${aEntry.mRemindAfter}")
            }
        } else {
            cvLastInspection.date = DateTime.now(DateTimeZone.UTC).millis
            rgReminders.check(R.id.rbRemindInOneYear)
        }

        var inspectionDate = DateTime.now(DateTimeZone.UTC)
        cvLastInspection.setOnDateChangeListener { view, year, month, dayOfMonth ->
            inspectionDate = DateTime(year, month+1, dayOfMonth, 0, 0)
        }

        btn.setOnClickListener {
            var remindAfter = InspectionRemindAfter.UNKNOWN
            when (rgReminders.checkedRadioButtonId) {
                R.id.rbRemindInOneYear -> remindAfter = InspectionRemindAfter.YEAR
                R.id.rbRemindInTwoYears -> remindAfter = InspectionRemindAfter.TWO_YEARS
                R.id.rbRemindInThreeYears -> remindAfter = InspectionRemindAfter.THREE_YEARS
            }
            ConfigManager(this).use {
                val entry = CarInspectionEntry(DateTime.now(), inspectionDate, remindAfter)
                if (editing) {
                    entry.mId = aEntry!!.mId
                    if (it.editEntry(entry)) {
                        mainCar.editEntry(entry)
                        entriesAdapter.editItem(entry)
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, R.string.unknownError, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (it.addEntry(mainCar.mName, entry)) {
                        mainCar.addEntry(entry)
                        entriesAdapter.addNewItem(entry)
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, R.string.unknownError, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun removeEntryOnList(aEntry: Entry){
        ConfigManager(this).use {
            if (!it.removeEntry(aEntry))
                Toast.makeText(this, R.string.couldNotRemoveEntry, Toast.LENGTH_SHORT).show()
        }
    }

    private fun editEntry(aEntry: Entry) {
        if (aEntry is FuelEntry)
            createFuelEntry(aEntry)
        else if (aEntry is CarInspectionEntry)
            createInspectionEntry(aEntry)
        else
            throw NotImplementedError("Not implemented entry type to edit")
    }

    private fun createNewEntry(aEntryType: EntryType)
    {
        when (aEntryType)
        {
            EntryType.Fuel -> createFuelEntry(null)
            EntryType.Inspection -> createInspectionEntry(null)
            else -> throw NotImplementedError("cannot create entry for unknown entry $aEntryType")
        }
    }

    private fun parseUserInput(aDialogLayout: View): Pair<Boolean, String> {

        var lastError = ""

        val odo = aDialogLayout.findViewById<TextView>(R.id.etOdometer)
        val fuelAm = aDialogLayout.findViewById<TextView>(R.id.etFuelAmount)
        val perLit = aDialogLayout.findViewById<TextView>(R.id.etPerLiter)

        if (odo.text.toString().toIntOrNull() == null)
            lastError = getString(R.string.odometerInputError)
        else if (fuelAm.text.toString().toDoubleOrNull() == null)
            lastError = getString(R.string.fuelAmountInputError)
        else if (perLit.text.toString().toDoubleOrNull() == null)
            lastError = getString(R.string.perLiterInputError)

        return Pair(lastError.isEmpty(), lastError)
    }

    fun onSummaryClick(view: View) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.activity_entries_summary, null)
        builder.setView(dialogLayout)

        var minDate = DateTime.now()
        var maxDate = DateTime.now()

        for (entry in mainCar.mFuelEntries) {
            if (entry.mDate < minDate)
                minDate = entry.mDate
            if (entry.mDate > maxDate)
                maxDate = entry.mDate
        }

        mSummaryDateFrom = minDate
        mSummaryDateTo = maxDate

        fillSummaryDialog(dialogLayout)

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun fillSummaryDialog(aDialogLayout: View) {
        val tvSummary = aDialogLayout.findViewById<TextView>(R.id.tvSummaryFromTo)
        val tvAvgOdo = aDialogLayout.findViewById<TextView>(R.id.tvAvgOdo)
        val tvAvgPricePerLiter = aDialogLayout.findViewById<TextView>(R.id.tvAvgPricePerLiter)
        val tvAvgTotalPrice = aDialogLayout.findViewById<TextView>(R.id.tvAvgTotalPrice)
        val tvAvgFuelAmount = aDialogLayout.findViewById<TextView>(R.id.tvAvgFuelAmount)
        val tvBestConsumption = aDialogLayout.findViewById<TextView>(R.id.tvBestConsumption)
        val tvWorstConsumption = aDialogLayout.findViewById<TextView>(R.id.tvWorstConsumption)

        tvSummary.text = String.format("%s - %s", mSummaryDateFrom.toString(CarsDBHelper.dateTimeFormatter),
            mSummaryDateTo.toString(CarsDBHelper.dateTimeFormatter))
        tvAvgOdo.text = String.format("%s = %d km", resources.getString(R.string.averageOdometer),
            mainCar.mFuelEntries.getAvgOdometer(mSummaryDateFrom, mSummaryDateTo))
        tvAvgPricePerLiter.text = String.format("%s = %.2f zł", resources.getString(R.string.averagePricePerLiter),
            mainCar.mFuelEntries.getAvgPricePerLiter(mSummaryDateFrom, mSummaryDateTo))
        tvAvgTotalPrice.text = String.format("%s = %.2f zł", resources.getString(R.string.avgTotalPrice),
            mainCar.mFuelEntries.getAvgTotalPrice(mSummaryDateFrom, mSummaryDateTo))
        tvAvgFuelAmount.text = String.format("%s = %.2f l", resources.getString(R.string.avgFuelAmount),
            mainCar.mFuelEntries.getAvgFuelAmount(mSummaryDateFrom, mSummaryDateTo))
        tvBestConsumption.text = String.format("%s = %.2f l/100km", resources.getString(R.string.bestConsumption),
            mainCar.mFuelEntries.getBestConsumption(mSummaryDateFrom, mSummaryDateTo))
        tvWorstConsumption.text = String.format("%s = %.2f l/100km", resources.getString(R.string.worstConsumption),
            mainCar.mFuelEntries.getWorstConsumption(mSummaryDateFrom, mSummaryDateTo))
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
        val fuelAm = dialogLayout.findViewById<TextView>(R.id.etFuelAmount)
        val perLit = dialogLayout.findViewById<TextView>(R.id.etPerLiter)

        if (editing)
        {
            if (aEntry?.mOdometer != 0)
                odo.text = aEntry?.mOdometer.toString()
            fuelAm.text = aEntry?.mFuelAmount.toString()
            perLit.text = aEntry?.mPerLiter.toString()
        }

        btn.setOnClickListener {

            val (parsed, err) = parseUserInput(dialogLayout)
            if (parsed) {
                var odoVal = 0
                if (odo.text.toString().toIntOrNull() != null)
                    odoVal = odo.text.toString().toInt()
                val fuelAmVal = String.format(Locale.ROOT, "%.2f", fuelAm.text.toString().toDouble()).toDouble()
                val perLitVal = String.format(Locale.ROOT, "%.2f", perLit.text.toString().toDouble()).toDouble()

                ConfigManager(this).use {
                    val entry = FuelEntry(DateTime.now(), odoVal, fuelAmVal, perLitVal)
                    if (editing) {
                        entry.mId = aEntry!!.mId
                        if (it.editEntry(entry)) {
                            mainCar.editEntry(entry)
                            entriesAdapter.editItem(entry)
                            dialog.dismiss()
                        } else {
                            Toast.makeText(this, R.string.unknownError, Toast.LENGTH_SHORT).show()
                        }
                    }
                    else {
                        if (it.addEntry(mainCar.mName, entry)) {
                            mainCar.addEntry(entry)
                            entriesAdapter.addNewItem(entry)
                            dialog.dismiss()
                            setSummaryButtonVisibility()
                        } else
                            Toast.makeText(this, R.string.unknownError, Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (err.isNotEmpty())
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this, R.string.unknownError, Toast.LENGTH_SHORT).show()
        }
    }
}

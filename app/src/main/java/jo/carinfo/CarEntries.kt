package jo.carinfo

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import org.joda.time.DateTime
import java.lang.String.format
import java.util.*

class CarEntries : AppCompatActivity() {

    private lateinit var mainCar: Car
    private lateinit var entriesAdapter: EntriesAdapter

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
                    if (delEntry is FuelEntry)
                        mainCar.mFuelEntries.remove(delEntry)
                    val snackbar = Snackbar.make(
                        findViewById(R.id.entries_layout),
                        R.string.entryRemoved,
                        Snackbar.LENGTH_INDEFINITE
                    )
                    snackbar.setAction("UNDO") {
                        entriesAdapter.restoreItem(delEntry, pos)
                        if (delEntry is FuelEntry)
                            mainCar.mFuelEntries.add(delEntry)
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
        // TODO Add inspection entry
    }

    private fun removeEntryOnList(aEntry: Entry){
        val cfgManager = ConfigManager(this)
        if (!cfgManager.removeFuelEntry(aEntry))
            Toast.makeText(this, R.string.couldNotRemoveEntry, Toast.LENGTH_SHORT).show()
    }

    private fun editEntry(aEntry: Entry) {
        if (aEntry is FuelEntry)
            createFuelEntry(aEntry)
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
            lastError = getString(R.string.mileageInputError)
        else if (fuelAm.text.toString().toDoubleOrNull() == null)
            lastError = getString(R.string.fuelAmountInputError)
        else if (perLit.text.toString().toDoubleOrNull() == null)
            lastError = getString(R.string.perLiterInputError)

        return Pair(lastError.isEmpty(), lastError)
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
                var fuelAmVal = String.format(Locale.ROOT, "%.2f", fuelAm.text.toString().toDouble()).toDouble()
                var perLitVal = String.format(Locale.ROOT, "%.2f", perLit.text.toString().toDouble()).toDouble()

                val cfgManager = ConfigManager(this)
                val entry = FuelEntry(DateTime.now(), odoVal, fuelAmVal, perLitVal)
                if (editing) {
                    entry.mId = aEntry!!.mId
                    if (cfgManager.editFuelEntry(entry)) {
                        mainCar.editEntry(entry)
                        entriesAdapter.editItem(entry)
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, R.string.unknownError, Toast.LENGTH_SHORT).show()
                    }
                }
                else {
                    if (cfgManager.addFuelEntry(mainCar.mName, entry)) {
                        mainCar.addEntry(entry)
                        entriesAdapter.addNewItem(entry)
                        dialog.dismiss()
                    } else
                        Toast.makeText(this, R.string.unknownError, Toast.LENGTH_SHORT).show()
                }
            } else if (err.isNotEmpty())
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this, R.string.unknownError, Toast.LENGTH_SHORT).show()
        }
    }
}

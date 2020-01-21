package jo.carinfo

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import yuku.ambilwarna.AmbilWarnaDialog
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener

const val FUEL_ENTRY_CLICK = 0

class SettingsActivity : AppCompatActivity() {

    private val originalCarsList = CarsList()
    private lateinit var adapter: CarAdapter
    private val p = Paint()

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
            it.onItemClick = { it ->
                val intent = Intent(this, CarEntries::class.java)
                intent.putExtra("car", it)
                startActivityForResult(intent, FUEL_ENTRY_CLICK)
            }
        }

        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = adapter

        enableSwipe()
    }

    private fun addAndRefreshList(aCar: Car) {
        ConfigManager(this).use {
            if (it.addCar(aCar)) {
                adapter.addNewItem(aCar)
            } else
                Toast.makeText(this, R.string.couldNotSaveCars, Toast.LENGTH_SHORT).show()
        }
    }

    private fun editCarOnList(aOldCar: Car, aNewCar: Car) {
        ConfigManager(this).use {
            if (it.editCar(aOldCar, aNewCar)) {
                adapter.editItem(aOldCar, aNewCar)
            } else
                Toast.makeText(this, R.string.couldNotSaveCars, Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeCarOnList(aCarName: String) {
        ConfigManager(this).use {
            if (!it.removeCar(aCarName))
                Toast.makeText(this, R.string.couldNotRemoveCars, Toast.LENGTH_SHORT).show()
        }
    }

    fun onEditCarClick(aCar: Car) {
        createAddCarDialog(aCar)
    }

    private fun createAddCarDialog(aStartCar: Car?) {
        val editing = aStartCar != null
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dlg_add_car, null)
        val etCarName = dialogLayout.findViewById<EditText>(R.id.etCarName)
        val bColorPicker = dialogLayout.findViewById<Button>(R.id.bPickChartColor)
        var editColor = Color.BLACK
        if (editing)
            editColor = aStartCar!!.mChartColor
        bColorPicker.setOnClickListener {
            val dlg = AmbilWarnaDialog(this, editColor, false, object : OnAmbilWarnaListener {

                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    editColor = color
                    bColorPicker.setBackgroundColor(editColor)
                }

                override fun onCancel(dialog: AmbilWarnaDialog?) {
                }
            })
            dlg.show()
        }
        bColorPicker.setBackgroundColor(editColor)
        builder.setPositiveButton(R.string.save) { _, _ -> }
        if (editing)
            builder.setTitle(R.string.editingCar)
        else
            builder.setTitle(R.string.creatingCar)
        etCarName.setText(aStartCar?.mName)
        etCarName.setSelection(etCarName.text.length)
        builder.setView(dialogLayout)
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

            val carName = etCarName.text.toString()
            if ((editing) && (!aStartCar!!.mName.equals(carName)) && (originalCarsList.indexOf(carName) != -1))
                Toast.makeText(this, R.string.carNameAlreadyExists, Toast.LENGTH_SHORT).show()
            else if (carName.length > 45)
                Toast.makeText(this, R.string.carNameTooLong, Toast.LENGTH_SHORT).show()
            else if (carName.isEmpty())
                Toast.makeText(this, R.string.invalidCarName, Toast.LENGTH_SHORT).show()
            else {
                val newCar = Car(carName)
                newCar.mChartColor = editColor

                if (editing)
                    editCarOnList(aStartCar!!, newCar)
                else
                    addAndRefreshList(newCar)
                dialog.dismiss()
            }
        }
    }

    fun onCarAddClick(view: View) {
        createAddCarDialog(null)
    }

    override fun onBackPressed() {
        closeActivity(true)
    }

    private fun closeActivity(aReturnCars: Boolean) {
        val intent = Intent()
        if (aReturnCars) {
            intent.putExtra("cars", originalCarsList)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun enableSwipe() {
        val listView = findViewById<RecyclerView>(R.id.rvCars)
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
                    val delCar = originalCarsList[pos]
                    adapter.removeItem(pos)
                    val snackbar = Snackbar.make(
                        findViewById(R.id.cars_layout),
                        R.string.carRemoved,
                        Snackbar.LENGTH_INDEFINITE
                    )
                    snackbar.setAction("UNDO") {
                        adapter.restoreItem(delCar, pos)
                    }
                    snackbar.addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            if (event != DISMISS_EVENT_ACTION)
                                removeCarOnList(delCar.mName)
                            super.onDismissed(transientBottomBar, event)
                        }
                    })
                    snackbar.setActionTextColor(Color.YELLOW)
                    snackbar.show()
                } else if (direction == ItemTouchHelper.RIGHT) {
                    val edCar = originalCarsList[pos]
                    onEditCarClick(edCar)
                    adapter.refreshItem(pos)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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
package jo.carinfo

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.View
import android.widget.*

const val FUEL_ENTRY_CLICK = 0

class SettingsActivity : AppCompatActivity() {

    private val originalCarsList = CarsList()
    private var adapter: CarAdapter? = null
    private val p = Paint()

    private fun addAndRefreshList(aCarName: String) {
        val cfgManager = ConfigManager(this)
        if (cfgManager.addCar(aCarName)) {
            adapter?.addNewItem(aCarName)
        } else
            Toast.makeText(this, R.string.couldNotSaveCars, Toast.LENGTH_SHORT).show()
    }

    private fun editCarOnList(aOldCarName: String, aNewCarName: String) {
        val cfgManager = ConfigManager(this)
        if (cfgManager.editCarName(aOldCarName, aNewCarName)) {
            adapter?.editItem(aOldCarName, aNewCarName)
        } else
            Toast.makeText(this, R.string.couldNotSaveCars, Toast.LENGTH_SHORT).show()
    }

    private fun removeCarOnList(aCarName: String) {
        val cfgManager = ConfigManager(this)
        if (!cfgManager.removeCar(aCarName))
            Toast.makeText(this, R.string.couldNotRemoveCars, Toast.LENGTH_SHORT).show()
    }

    fun onEditCarClick(aCar: Car) {
        createAddCarDialog(aCar.mName)
    }

    private fun createAddCarDialog(aStartText: String = "") {
        val editing = aStartText.isNotEmpty()
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dlg_add_car, null)
        val etCarName = dialogLayout.findViewById<EditText>(R.id.etCarName)
        if (editing) {
            builder.setTitle(R.string.editingCar)
            builder.setPositiveButton(R.string.save) { _, _ -> }
        } else {
            builder.setTitle(R.string.creatingCar)
            builder.setPositiveButton(R.string.save) { _, _ -> }
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
                    } else {
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
        } else if (aInput.length > 45) {
            Toast.makeText(this, R.string.carNameTooLong, Toast.LENGTH_SHORT).show()
        } else if (aInput.isEmpty()) {
            Toast.makeText(this, R.string.invalidCarName, Toast.LENGTH_SHORT).show()
        } else
            return true
        return false
    }

    fun onCarAddClick(view: View) {
        createAddCarDialog()
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

        enableSwipe()
    }

    private fun enableSwipe() {
        val listView = findViewById<RecyclerView>(R.id.rvCars)
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView?,
                viewHolder: RecyclerView.ViewHolder?,
                target: RecyclerView.ViewHolder?
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
                val pos = viewHolder!!.adapterPosition

                if (direction == ItemTouchHelper.LEFT) {
                    val delCar = originalCarsList[pos]
                    adapter?.removeItem(pos)
                    val snackbar = Snackbar.make(
                        findViewById(R.id.cars_layout),
                        R.string.carRemoved,
                        Snackbar.LENGTH_INDEFINITE
                    )
                    snackbar.setAction("UNDO") {
                        adapter?.restoreItem(delCar, pos)
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
                    adapter?.refreshItem(pos)
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
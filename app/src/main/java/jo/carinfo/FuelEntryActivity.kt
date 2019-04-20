package jo.carinfo

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.*

class FuelEntryActivity : AppCompatActivity() {

    var mLastError = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fuel_entry)

        val sCars = findViewById<Spinner>(R.id.sCarNames)
        var namesArr = arrayListOf<String>()
        val extras = intent.extras
        if (extras != null)
            if (extras.containsKey("cars"))
            {
                for (car in extras.getSerializable("cars") as ArrayList<Car>)
                    namesArr.add(car.mName)
            }
        sCars.adapter = ArrayAdapter(this, R.layout.simple_name, namesArr)
        sCars.setSelection(0)
    }

    fun onFuelEntrySave(view: View)
    {
        mLastError = ""
        if (parseUserInput())
        {
            val date = Calendar.getInstance().time
            val odo = getOdometer()
            val mileage = getMileage()
            val fuelAm = getFuelAmount()
            val perLit = getPerLiter()

            val fuelEntry = FuelEntry(date, odo, mileage, fuelAm, perLit)
            val intent = Intent()
            intent.putExtra("name", findViewById<Spinner>(R.id.sCarNames).selectedItem.toString())
            intent.putExtra("entry", fuelEntry)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        else
            Toast.makeText(this, mLastError, Toast.LENGTH_LONG).show()
    }

    private fun getOdometer(): Int
    {
        val et = findViewById<TextView>(R.id.etOdometer)
        var result = 0
        when (et.text.toString().toIntOrNull() != null)
        {
            true -> result = et.text.toString().toInt()
        }
        return result
    }

    private fun getMileage(): Int
    {
        val et = findViewById<TextView>(R.id.etMileage)
        var result = 0
        when (et.text.toString().toIntOrNull() != null)
        {
            true -> result = et.text.toString().toInt()
        }
        return result
    }

    private fun getFuelAmount(): Double
    {
        val et = findViewById<TextView>(R.id.etFuelAmount)
        var result = 0.0
        when (et.text.toString().toDoubleOrNull() != null)
        {
            true -> result = String.format(Locale.ROOT, "%.2f", et.text.toString().toDouble()).toDouble()
        }
        return result
    }

    private fun getPerLiter(): Double
    {
        val et = findViewById<TextView>(R.id.etPerLiter)
        var result = 0.0
        when (et.text.toString().toDoubleOrNull() != null)
        {
            true -> result = String.format(Locale.ROOT, "%.2f", et.text.toString().toDouble()).toDouble()
        }
        return result
    }

    private fun parseUserInput(): Boolean
    {
        var haveMileage = false

        var et = findViewById<EditText>(R.id.etOdometer)

        if (et.text.toString().toIntOrNull() != null)
            haveMileage = true
        else
        {
            et = findViewById(R.id.etMileage)
            if (et.text.toString().toIntOrNull() != null)
                haveMileage = true
            else
                mLastError = getString(R.string.mileageInputError)
        }

        if (haveMileage)
        {
            et = findViewById(R.id.etFuelAmount)
            if (et.text.toString().toDoubleOrNull() == null)
            {
                mLastError = getString(R.string.fuelAmountInputError)
            }
            else
            {
                et = findViewById(R.id.etPerLiter)
                if (et.text.toString().toDoubleOrNull() == null)
                    mLastError = getString(R.string.perLiterInputError)
                else
                    return true
            }
        }
        return false
    }
}

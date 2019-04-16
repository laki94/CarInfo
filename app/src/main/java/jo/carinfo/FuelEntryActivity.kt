package jo.carinfo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.SpinnerAdapter

class FuelEntryActivity : AppCompatActivity() {

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
        sCars.adapter = ArrayAdapter(this, R.layout.activity_simple_name, namesArr)
    }
}

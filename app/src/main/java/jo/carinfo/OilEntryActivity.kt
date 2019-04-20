package jo.carinfo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.CalendarView
import java.util.*

class OilEntryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oil_entry)

        val c = Calendar.getInstance()
        val dpd = findViewById<CalendarView>(R.id.cvDate)
        dpd.date = c.timeInMillis
    }
}

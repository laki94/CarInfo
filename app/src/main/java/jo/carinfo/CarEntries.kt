package jo.carinfo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.TextView
import java.lang.String.format

class CarEntries : AppCompatActivity() {

    private var mainCar: Car? = null

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

        var adapter: EntriesAdapter? = null
        if (mainCar?.mEntries?.isNullOrEmpty() == false)
            adapter = EntriesAdapter(this, mainCar!!.mEntries)

//        adapter.let { it?.onItemClick = { it ->
//            val intent = Intent(this, CarEntries::class.java)
//            intent.putExtra("car", it)
//            startActivity(intent)
//        }
//        }

        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = adapter
    }
}

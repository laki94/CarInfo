package jo.carinfo

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.lvcars_item.view.*

class CarAdapter(private val context: Context, private val items : CarsList) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val myHolder = holder as MyViewHolder

        myHolder.tvCarName.text = items[position].mName

        myHolder.tvCarName.setOnClickListener {
            // Handle click to select items to edit/remove
        }

        myHolder.tvCarName.setOnLongClickListener {
            // Handle long click to edit/remove items
            false
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.lvcars_item, parent, false))
    }

    class MyViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        val tvCarName : TextView = view.tvSimpleCarName
    }
}
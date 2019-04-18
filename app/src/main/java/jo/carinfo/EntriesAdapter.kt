package jo.carinfo

import android.content.Context
import android.opengl.Visibility
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import kotlinx.android.synthetic.main.lvcars_item.view.*

class EntriesAdapter(private val context: Context, private val items : EntriesList) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClick: ((Car) -> Unit)? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val myHolder = holder as EntriesViewHolder

        myHolder.tvCarName.text = items[position].getObjectString(context)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return EntriesViewHolder(LayoutInflater.from(context).inflate(R.layout.lvcars_item, parent, false))
    }

    inner class EntriesViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        val tvCarName : TextView = view.tvSimpleCarName
        private val cbSelectCar: CheckBox = view.cbSelectCar

        init {
            cbSelectCar.visibility = View.GONE
        }
    }
}
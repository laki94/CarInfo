package jo.carinfo

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import kotlinx.android.synthetic.main.lvcars_item.view.*

class EntriesAdapter(private val context: Context, private val items : ArrayList<Entry>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClick: ((Entry) -> Unit)? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val myHolder = holder as EntriesViewHolder

        myHolder.tvCarName.text = items[position].getObjectString(context)
    }

    override fun getItemCount(): Int {
        var result = 0

        if (!items.isNullOrEmpty())
            result = items.size

        return result
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return EntriesViewHolder(LayoutInflater.from(context).inflate(R.layout.lvcars_item, parent, false))
    }

    inner class EntriesViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        val tvCarName : TextView = view.tvSimpleCarName
        private val cbSelectCar: CheckBox = view.cbSelectCar

        init {
            cbSelectCar.visibility = View.GONE
            tvCarName.setOnClickListener{
                onItemClick?.invoke(items[adapterPosition])
            }
        }
    }
}
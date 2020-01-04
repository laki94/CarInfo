package jo.carinfo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.lvcarsgraph_item.view.*

class CarGraphAdapter(private val context: Context, private val items : CarsListGraph) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClick: ((Car, Boolean) -> Unit)? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val myHolder = holder as MyViewHolder

        myHolder.tvCarName.text = items[position].mName
        myHolder.tvCarName.setTextColor(items[position].mChartColor)
        myHolder.cbCarGraph.isChecked = true
    }

    fun changeItemVisibility(aCar: Car, aIsChecked: Boolean) {
        items.processGraphLineVisibility(aCar, aIsChecked)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.lvcarsgraph_item, parent, false))
    }

    inner class MyViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        val tvCarName : TextView = view.tvSimpleCarNameGraph
        val cbCarGraph : CheckBox = view.cbCarGraph

        init {
            tvCarName.setOnClickListener {
                cbCarGraph.isChecked = !cbCarGraph.isChecked
            }

            cbCarGraph.setOnCheckedChangeListener { _, isChecked ->
                onItemClick?.invoke(items[adapterPosition], isChecked)
            }
        }
    }
}
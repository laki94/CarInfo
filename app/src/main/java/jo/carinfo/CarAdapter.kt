package jo.carinfo

import android.content.Context
import android.content.Intent
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_settings.view.*
import kotlinx.android.synthetic.main.lvcars_item.view.*

class CarAdapter(private val context: Context, private val items : CarsList) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClick: ((Car) -> Unit)? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val myHolder = holder as MyViewHolder

        myHolder.tvCarName.text = items[position].mName
    }

    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
    }

    fun addNewItem(aCarName: String) {
        items.add(Car(aCarName))
        notifyItemChanged(itemCount - 1)
    }

    fun editItem(aOldCarName: String, aNewCarName: String) {
        items.changeName(aOldCarName, aNewCarName)
        notifyItemChanged(items.indexOf(aNewCarName))
    }

    fun refreshItem(position: Int) {
        notifyItemChanged(position)
    }

    fun restoreItem(aCar: Car, position: Int) {
        items.add(position, aCar)
        notifyItemInserted(position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.lvcars_item, parent, false))
    }

    inner class MyViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        val tvCarName : TextView = view.tvSimpleCarName

        init {
            tvCarName.setOnClickListener{
                onItemClick?.invoke(items[adapterPosition])
            }
        }
    }
}
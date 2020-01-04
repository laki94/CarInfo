package jo.carinfo

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.lvcars_item.view.*

class CarAdapter(private val context: Context, private val items : CarsList) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClick: ((Car) -> Unit)? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val myHolder = holder as MyViewHolder

        myHolder.cardView.setBackgroundColor(Color.YELLOW)
        myHolder.tvCarName.text = items[position].mName
    }

    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
    }

    fun addNewItem(aCar: Car) {
        items.add(aCar)
        notifyItemChanged(itemCount - 1)
    }

    fun editItem(aOldCar: Car, aNewCar: Car) {
        items.changeName(aOldCar, aNewCar)
        items.changeColor(aOldCar, aNewCar)
        notifyItemChanged(items.indexOf(aNewCar))
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
        val cardView : CardView = view.card_view

        init {
            tvCarName.setOnClickListener{
                onItemClick?.invoke(items[adapterPosition])
            }
        }
    }
}
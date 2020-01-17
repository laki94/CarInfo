package jo.carinfo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.lvcars_item.view.*

class EntriesAdapter(private val context: Context, private val items : ArrayList<Entry>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClick: ((Entry) -> Unit)? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val myHolder = holder as EntriesViewHolder

        myHolder.cardView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark))
        myHolder.tvCarName.text = items[position].getObjectString(context)
    }

    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
    }

    fun addNewItem(aEntry: Entry) {
        items.add(aEntry)
        notifyItemChanged(itemCount - 1)
    }

    fun editItem(aEntry: Entry) {

        for (tmpentry in items) {
            if (tmpentry.mId == aEntry.mId) {
                if (aEntry is FuelEntry) {
                    (tmpentry as FuelEntry).mFuelAmount = aEntry.mFuelAmount
                    tmpentry.mPerLiter = aEntry.mPerLiter
                    tmpentry.mOdometer = aEntry.mOdometer
                } else if (aEntry is CarInspectionEntry) {
                    (tmpentry as CarInspectionEntry).mLastInspectionDate = aEntry.mLastInspectionDate
                    tmpentry.mRemindAfter = aEntry.mRemindAfter
                }
                break
            }
        }
        notifyItemChanged(getIndexOf(aEntry))
    }

    private fun getIndexOf(aEntry: Entry): Int {

        for (i in 0 until items.count())
        {
            if (items[i].mId == aEntry.mId)
                return i
        }
        return -1
    }

    fun refreshItem(position: Int) {
        notifyItemChanged(position)
    }

    fun restoreItem(aEntry: Entry, position: Int) {
        items.add(position, aEntry)
        notifyItemInserted(position)
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
        val cardView : CardView = view.card_view

        init {
            tvCarName.setOnClickListener{
                onItemClick?.invoke(items[adapterPosition])
            }
        }
    }
}
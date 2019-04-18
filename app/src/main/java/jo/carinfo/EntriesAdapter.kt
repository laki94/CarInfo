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

//    private var mEditing = false
//    private val mCheckedIds: ArrayList<Int> = arrayListOf()
//    private var mOnLongPos = -1
    var onItemClick: ((Car) -> Unit)? = null

//    fun isEditing(): Boolean
//    {
//        return mEditing
//    }

//    fun stopEditing()
//    {
//        (context as SettingsActivity).findViewById<Toolbar>(R.id.tbEdit).visibility = View.GONE
//        mEditing = false
//        notifyDataSetChanged()
//    }

//    fun getCheckedIds(): ArrayList<Int>
//    {
//        val result: ArrayList<Int> = (mCheckedIds.clone() as ArrayList<Int>)
//        mCheckedIds.clear()
//        mOnLongPos = -1
//        return result
//    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val myHolder = holder as EntriesViewHolder

        myHolder.tvCarName.text = items[position].getObjectString(context)
    }

//        if (mEditing)
//        {
//            myHolder.cbSelectCar.visibility = View.VISIBLE
//        }
//        else
//        {
//            myHolder.cbSelectCar.visibility = View.INVISIBLE
//        }

//        myHolder.tvCarName.text = items[position].mName

//        if (position != mOnLongPos)
//            myHolder.cbSelectCar.isChecked = false

//        myHolder.tvCarName.setOnLongClickListener {
//            if (!mEditing)
//            {
//                mEditing = true
//                mOnLongPos = position
//                (context as SettingsActivity).findViewById<Toolbar>(R.id.tbEdit).visibility = View.VISIBLE
//                notifyDataSetChanged()
//            }
//            myHolder.cbSelectCar.isChecked = !myHolder.cbSelectCar.isChecked
//            true
//        }

//        myHolder.cbSelectCar.setOnCheckedChangeListener {_, isChecked ->
//
//            if (isChecked)
//            {
//                if (!mCheckedIds.contains(position))
//                    mCheckedIds.add(position)
//            }
//            else
//                if (mCheckedIds.contains(position))
//                    mCheckedIds.remove(position)
//        }
//    }

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
//        init {
//            tvCarName.setOnClickListener{
//                if (mEditing)
//                    cbSelectCar.isChecked = !cbSelectCar.isChecked
//                else
//                    onItemClick?.invoke(items[adapterPosition])
//            }
//        }
    }
}
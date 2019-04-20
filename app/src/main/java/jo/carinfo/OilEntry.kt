package jo.carinfo

import android.content.Context
import java.util.*

class OilEntry(aDate: Date = Date(0), aMileage: Int, aRemindAfter: Int): Entry(aDate) {

    val mOrgMileage = aMileage
    val aRemindAfter = aRemindAfter

    override fun getObjectString(context: Context): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getRawData(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
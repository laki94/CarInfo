package jo.carinfo

import android.content.Context
import java.util.*

class OilEntry(aDate: Date = Date(0), aMileage: Int = 0, aRemindAfter: Int = 0): Entry(aDate) {

    var mOrgMileage = aMileage
    var mRemindAfter = aRemindAfter

    override fun getObjectString(context: Context): String {
        return String.format("%s: %d\n%s: %d", context.getString(R.string.mileage), mOrgMileage,
            context.getString(R.string.remindAfter), mRemindAfter)
    }

    override fun getRawData(): String {
        return String.format("%d,%d", mOrgMileage, mRemindAfter)
    }
}
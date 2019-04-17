package jo.carinfo

import java.io.Serializable
import java.util.*

open class Entry(date: Date = Date(0)): Serializable {
    var mDate = date
}
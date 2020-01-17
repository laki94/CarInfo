package jo.carinfo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.io.Serializable

class CarsDBHelper(ctx: Context): SQLiteOpenHelper(ctx, DATABASE_NAME, null, DATABASE_VERSION), Serializable {
    private val dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")

    override fun onCreate(p0: SQLiteDatabase) {
        p0.execSQL(
                "CREATE TABLE IF NOT EXISTS $TABLE_CARS (" +
                    "'id' INTEGER PRIMARY KEY," +
                    "'name' VARCHAR(45) NULL," +
                    "'chart_color' INTEGER NULL)")

        p0.execSQL(
                "CREATE TABLE IF NOT EXISTS $TABLE_FUEL_ENTRIES (" +
                "'entry_id' INTEGER PRIMARY KEY," +
                "'entry_date' VARCHAR(19) NOT NULL," +
                "'entry_odo' INTEGER NULL," +
                "'entry_fuel_amount' REAL NULL," +
                "'entry_fuel_price' REAL NULL," +
                "'carId' INTEGER NOT NULL," +
                "FOREIGN KEY(carId) REFERENCES cars(id))")

        p0.execSQL(
            "CREATE TABLE IF NOT EXISTS $TABLE_INSPECTION_ENTRIES (" +
                    "'entry_id' INTEGER PRIMARY KEY," +
                    "'entry_date' VARCHAR(19) NOT NULL," +
                    "'entry_inspection_date' VARCHAR(19) NOT NULL," +
                    "'entry_remind_after' INTEGER NOT NULL," +
                    "'carId' INTEGER NOT NULL," +
                    "FOREIGN KEY(carId) REFERENCES cars(id))")

        p0.execSQL(
            "CREATE TABLE IF NOT EXISTS $TABLE_STATIONS (" +
                "'station_id' INTEGER PRIMARY KEY," +
                "'station_lat' REAL NOT NULL," +
                "'station_lon' REAL NOT NULL," +
                "'station_name' VARCHAR(45) NOT NULL," +
                "'station_radius' INTEGER NOT NULL," +
                "'is_near' VARCHAR(5) NOT NULL)")

        p0.execSQL("INSERT INTO $TABLE_CARS (name, chart_color) VALUES ('test', 4278190080);")
        p0.execSQL("INSERT INTO $TABLE_CARS (name, chart_color) VALUES ('test2', 4278190335);")

        p0.execSQL("INSERT INTO $TABLE_FUEL_ENTRIES (entry_date, entry_odo, entry_fuel_amount, entry_fuel_price, carId) VALUES " +
                "('2019-12-11', 610, 52.4, 4.55, 1);")
        p0.execSQL("INSERT INTO $TABLE_FUEL_ENTRIES (entry_date, entry_odo, entry_fuel_amount, entry_fuel_price, carId) VALUES " +
                "('2020-01-01', 655, 55.4, 4.75, 1);")
        p0.execSQL("INSERT INTO $TABLE_FUEL_ENTRIES (entry_date, entry_odo, entry_fuel_amount, entry_fuel_price, carId) VALUES " +
                "('2020-02-13', 620, 50.0, 4.65, 1);")

        p0.execSQL("INSERT INTO $TABLE_FUEL_ENTRIES (entry_date, entry_odo, entry_fuel_amount, entry_fuel_price, carId) VALUES " +
                "('2019-11-22', 499, 33.4, 4.55, 2);")
        p0.execSQL("INSERT INTO $TABLE_FUEL_ENTRIES (entry_date, entry_odo, entry_fuel_amount, entry_fuel_price, carId) VALUES " +
                "('2020-01-01', 541, 37.4, 4.75, 2);")
        p0.execSQL("INSERT INTO $TABLE_FUEL_ENTRIES (entry_date, entry_odo, entry_fuel_amount, entry_fuel_price, carId) VALUES " +
                "('2020-02-13', 504, 36.0, 4.65, 2);")
    }

    override fun onUpgrade(p0: SQLiteDatabase, oldVer: Int, newVer: Int) {
        p0.execSQL("DROP TABLE IF EXISTS $TABLE_CARS")
        p0.execSQL("DROP TABLE IF EXISTS $TABLE_FUEL_ENTRIES")
        p0.execSQL("DROP TABLE IF EXISTS $TABLE_INSPECTION_ENTRIES")
        p0.execSQL("DROP TABLE IF EXISTS $TABLE_STATIONS")
        onCreate(p0)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun addCar(aCar: Car): Boolean {
        val values = ContentValues()
        val db = this.writableDatabase
        try {
            values.put("name", aCar.mName)
            values.put("chart_color", aCar.mChartColor)
            val _success = db.insert(TABLE_CARS, null, values)
            return Integer.parseInt("$_success") != -1
        } finally {
            db.close()
        }
    }

    fun editCar(aOldCar: Car, aNewCar: Car): Boolean {
        val values = ContentValues()
        val db = this.writableDatabase
        try {
            values.put("name", aNewCar.mName)
            values.put("chart_color", aNewCar.mChartColor)
            val _success = db.update(TABLE_CARS, values, "name=?", arrayOf(aOldCar.mName))
            return Integer.parseInt("$_success") != -1
        } finally {
            db.close()
        }
    }

    fun getCarId(aCarName: String): Int {
        var result = -1
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_CARS WHERE $NAME_PARAM=?", arrayOf(aCarName))
        try {
            if (cursor.moveToFirst())
            {
                result = cursor.getInt(cursor.getColumnIndex("id"))
            }
        } finally {
            cursor.close()
        }
        return result
    }

    fun removeCar(aCarName: String): Boolean {
        val db = this.writableDatabase
        try {
            var _success = db.delete(TABLE_FUEL_ENTRIES, "carId=?", arrayOf(getCarId(aCarName).toString()))
            if (Integer.parseInt("$_success") != -1)
                _success = db.delete(TABLE_INSPECTION_ENTRIES, "carId=?", arrayOf(getCarId(aCarName).toString()))
            if (Integer.parseInt("$_success") != -1)
                _success = db.delete(TABLE_CARS, "name=?",  arrayOf(aCarName))
            return Integer.parseInt("$_success") != -1
        } finally {
            db.close()
        }
    }

    fun saveCars(aCars: CarsList): Boolean {
        val values = ContentValues()
        var _success: Long
        val db = this.writableDatabase
        try {
            for (car in aCars)
            {
                values.clear()
                values.put("name", car.mName)
                values.put("chart_color", car.mChartColor)
                _success = db.insert(TABLE_CARS, null, values)
                if (Integer.parseInt("$_success") == -1)
                    return false
            }
            return true
        } finally {
            db.close()
        }
    }

    fun getCarInspectionEntry(aCarId: Int): CarInspectionEntry? {
        if (aCarId != -1) {
            val db = this.readableDatabase
            val cursor = db.rawQuery(
                "SELECT * FROM $TABLE_INSPECTION_ENTRIES WHERE $CARID_PARAM=?",
                arrayOf(aCarId.toString())
            )
            try {
                if (cursor.moveToFirst()) {
                    val entry = CarInspectionEntry()
                    entry.mId = cursor.getInt(cursor.getColumnIndex("entry_id"))
                    entry.mDate = DateTime.parse(
                        cursor.getString(cursor.getColumnIndex("entry_date")),
                        dateTimeFormatter
                    )
                    entry.mLastInspectionDate = DateTime.parse(
                        cursor.getString(cursor.getColumnIndex("entry_inspection_date")),
                        dateTimeFormatter
                    )
                    entry.mRemindAfter =
                        InspectionRemindAfter.valueOf(cursor.getString(cursor.getColumnIndex("entry_remind_after")))
                    return entry
                }
            } finally {
                cursor.close()
            }
        }
        return null
    }

    fun getAllCars(): CarsList {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_CARS", null)
        try {
            val result = CarsList()
            if (cursor.moveToFirst())
            {
                var newCar = Car(cursor.getString(cursor.getColumnIndex("name")))
                newCar.mChartColor = cursor.getInt(cursor.getColumnIndex("chart_color"))
                newCar.mFuelEntries.addAll(getAllFuelEntries(cursor.getInt(cursor.getColumnIndex("id"))))
                newCar.mInspection = getCarInspectionEntry(cursor.getInt(cursor.getColumnIndex("id")))
                result.add(newCar)
                while (cursor.moveToNext())
                {
                    newCar = Car(cursor.getString(cursor.getColumnIndex("name")))
                    newCar.mChartColor = cursor.getInt(cursor.getColumnIndex("chart_color"))
                    newCar.mFuelEntries.addAll(getAllFuelEntries(cursor.getInt(cursor.getColumnIndex("id"))))
                    newCar.mInspection = getCarInspectionEntry(cursor.getInt(cursor.getColumnIndex("id")))
                    result.add(newCar)
                }
            }
            return result
        } finally {
            cursor.close()
        }
    }

    fun addFuelEntry(aOwnerId: Int, aEntry: FuelEntry): Boolean {
        if (aOwnerId != -1) {
            val values = ContentValues()
            val db = this.writableDatabase
            try {
                values.put("carId", aOwnerId)
                values.put("entry_date", aEntry.mDate.toString(dateTimeFormatter))
                values.put("entry_odo", aEntry.mOdometer)
                values.put("entry_fuel_amount", aEntry.mFuelAmount)
                values.put("entry_fuel_price", aEntry.mPerLiter)
                val _success = db.insert(TABLE_FUEL_ENTRIES, null, values)
                aEntry.mId = Integer.parseInt("$_success")
                return Integer.parseInt("$_success") != -1
            } finally {
                db.close()
            }
        }
        return false
    }

    fun addInspectionEntry(aCarName: String, aEntry: CarInspectionEntry): Boolean {
        val carId = getCarId(aCarName)
        if (carId != -1) {
            val values = ContentValues()
            val db = this.writableDatabase
            try {
                db.delete(TABLE_INSPECTION_ENTRIES, "carId=?", arrayOf(carId.toString()))
                values.put("carId", carId)
                values.put("entry_date", aEntry.mDate.toString(dateTimeFormatter))
                values.put("entry_inspection_date", aEntry.mLastInspectionDate.toString(dateTimeFormatter))
                values.put("entry_remind_after", aEntry.mRemindAfter.name)
                val _success = db.insert(TABLE_INSPECTION_ENTRIES, null, values)
                aEntry.mId = Integer.parseInt("$_success")
                return Integer.parseInt("$_success") != -1
            } finally {
                db.close()
            }
        }
        return false
    }

    fun editFuelEntry(aEntry: FuelEntry): Boolean {
        val values = ContentValues()
        val db = this.writableDatabase
        try {
            values.put("entry_odo", aEntry.mOdometer)
            values.put("entry_fuel_amount", aEntry.mFuelAmount)
            values.put("entry_fuel_price", aEntry.mPerLiter)
            val _success = db.update(TABLE_FUEL_ENTRIES, values, "entry_id=?", arrayOf(aEntry.mId.toString()))
            return Integer.parseInt("$_success") != -1
        } finally {
            db.close()
        }
    }

    fun editInspectionEntry(aEntry: CarInspectionEntry): Boolean {
        val values = ContentValues()
        val db = this.writableDatabase
        try {
            values.put("entry_inspection_date", aEntry.mLastInspectionDate.toString(dateTimeFormatter))
            values.put("entry_remind_after", aEntry.mRemindAfter.name)
            val _success = db.update(TABLE_INSPECTION_ENTRIES, values, "entry_id=?", arrayOf(aEntry.mId.toString()))
            return Integer.parseInt("$_success") != -1
        } finally {
            db.close()
        }
    }

    fun removeEntry(aEntry: Entry): Boolean {
        val db = this.writableDatabase
        var _success = -1
        try {
            if (aEntry is FuelEntry)
                _success = db.delete(TABLE_FUEL_ENTRIES, "entry_id=?",  arrayOf(aEntry.mId.toString()))
            else if (aEntry is CarInspectionEntry)
                _success = db.delete(TABLE_INSPECTION_ENTRIES, "entry_id=?",  arrayOf(aEntry.mId.toString()))
            return Integer.parseInt("$_success") != -1
        } finally {
            db.close()
        }
    }

    private fun getAllFuelEntries(aCarId: Int): FuelEntriesList {
        val result = FuelEntriesList()
        if (aCarId != -1) {
            val db = this.readableDatabase
            val cursor = db.rawQuery("SELECT * FROM $TABLE_FUEL_ENTRIES WHERE $CARID_PARAM=?", arrayOf(aCarId.toString()))
            try {
                if (cursor.moveToFirst())
                {
                    var entry = FuelEntry()
                    entry.mId = cursor.getInt(cursor.getColumnIndex("entry_id"))
                    entry.mOdometer = cursor.getInt(cursor.getColumnIndex("entry_odo"))
                    entry.mDate = DateTime.parse(cursor.getString(cursor.getColumnIndex("entry_date")),dateTimeFormatter)
                    entry.mFuelAmount = cursor.getDouble(cursor.getColumnIndex("entry_fuel_amount"))
                    entry.mPerLiter = cursor.getDouble(cursor.getColumnIndex("entry_fuel_price"))
                    result.add(entry)
                    while (cursor.moveToNext())
                    {
                        entry = FuelEntry()
                        entry.mId = cursor.getInt(cursor.getColumnIndex("entry_id"))
                        entry.mOdometer = cursor.getInt(cursor.getColumnIndex("entry_odo"))
                        entry.mDate = DateTime.parse(cursor.getString(cursor.getColumnIndex("entry_date")),dateTimeFormatter)
                        entry.mFuelAmount = cursor.getDouble(cursor.getColumnIndex("entry_fuel_amount"))
                        entry.mPerLiter = cursor.getDouble(cursor.getColumnIndex("entry_fuel_price"))
                        result.add(entry)
                    }
                }
            } finally {
                cursor.close()
            }
        }
        result.sortByDate()
        return result
    }

    fun getAllStations(): StationList {
        val result = StationList()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_STATIONS", null)
        try {
            if (cursor.moveToFirst())
            {
                var simple_station = Station()
                simple_station.mId = cursor.getInt(cursor.getColumnIndex("station_id"))
                simple_station.mName = cursor.getString(cursor.getColumnIndex("station_name"))
                simple_station.mLat = cursor.getDouble(cursor.getColumnIndex("station_lat"))
                simple_station.mLon = cursor.getDouble(cursor.getColumnIndex("station_lon"))
                simple_station.mRadius = cursor.getInt(cursor.getColumnIndex("station_radius"))
                simple_station.mInRange = cursor.getString(cursor.getColumnIndex("is_near"))!!.toBoolean()
                result.add(simple_station)
                while (cursor.moveToNext())
                {
                    simple_station = Station()
                    simple_station.mId = cursor.getInt(cursor.getColumnIndex("station_id"))
                    simple_station.mName = cursor.getString(cursor.getColumnIndex("station_name"))
                    simple_station.mLat = cursor.getDouble(cursor.getColumnIndex("station_lat"))
                    simple_station.mLon = cursor.getDouble(cursor.getColumnIndex("station_lon"))
                    simple_station.mRadius = cursor.getInt(cursor.getColumnIndex("station_radius"))
                    simple_station.mInRange = cursor.getString(cursor.getColumnIndex("is_near"))!!.toBoolean()
                    result.add(simple_station)
                }
            }
        } finally {
            cursor.close()
        }
        return result
    }

    fun saveStation(aStation: Station): Boolean {
        val values = ContentValues()
        val db = this.writableDatabase
        try {
            values.put("station_name", aStation.mName)
            values.put("station_lat", aStation.mLat)
            values.put("station_lon", aStation.mLon)
            values.put("station_radius", aStation.mRadius)
            values.put("is_near", aStation.mInRange.toString())
            val _success = db.insert(TABLE_STATIONS, null, values)
            aStation.mId = Integer.parseInt("$_success")
            return Integer.parseInt("$_success") != -1
        } finally {
            db.close()
        }
    }

    fun removeStation(aStation: Station): Boolean {
        if (aStation.mId != -1) {
            val db = this.writableDatabase
            try {
                val _success = db.delete(TABLE_STATIONS, "station_id=?",  arrayOf(aStation.mId.toString()))
                return Integer.parseInt("$_success") != -1
            } finally {
                db.close()
            }
        } else
            return false
    }

    fun editStation(aStation: Station): Boolean {
        val values = ContentValues()
        val db = this.writableDatabase
        try {
            values.put("station_name", aStation.mName)
            values.put("station_lat", aStation.mLat)
            values.put("station_lon", aStation.mLon)
            values.put("station_radius", aStation.mRadius)
            values.put("is_near", aStation.mInRange.toString())
            val _success = db.update(TABLE_STATIONS, values, "station_id=?", arrayOf(aStation.mId.toString()))
            return Integer.parseInt("$_success") != -1
        } finally {
            db.close()
        }
    }

    companion object {
        const val DATABASE_NAME = "carsdb"
        const val DATABASE_VERSION = 10
        const val TABLE_CARS = "cars"
        const val TABLE_FUEL_ENTRIES = "fuel_entries"
        const val TABLE_INSPECTION_ENTRIES = "inspection_entries"
        const val TABLE_STATIONS = "stations"
        const val NAME_PARAM = "name"
        const val CARID_PARAM = "carId"
    }
}
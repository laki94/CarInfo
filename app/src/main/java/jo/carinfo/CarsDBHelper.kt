package jo.carinfo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class CarsDBHelper(ctx: Context): SQLiteOpenHelper(ctx, DATABASE_NAME, null, DATABASE_VERSION) {

    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(p0: SQLiteDatabase) {
        p0.execSQL(
                "CREATE TABLE IF NOT EXISTS $TABLE_CARS (" +
                    "'id' INTEGER PRIMARY KEY," +
                    "'name' VARCHAR(45) NULL," +
                    "'chart_color' INTEGER NULL)")

        p0.execSQL(
                "CREATE TABLE IF NOT EXISTS $TABLE_ENTRIES (" +
                "'entry_id' INTEGER PRIMARY KEY," +
                "'entry_type' INTEGER NOT NULL," +
                "'entry_date' VARCHAR(19) NOT NULL," +
                "'entry_odo' INTEGER NULL," +
                "'entry_mil' INTEGER NULL," +
                "'entry_fuel_amount' REAL NULL," +
                "'entry_fuel_price' REAL NULL," +
                "'entry_remind_after' INTEGER NULL," +
                "'carId' INTEGER NOT NULL," +
                "FOREIGN KEY(carId) REFERENCES cars(id))")

        p0.execSQL("INSERT INTO $TABLE_CARS (name, chart_color) VALUES ('test', 4278190080);")
        p0.execSQL("INSERT INTO $TABLE_CARS (name, chart_color) VALUES ('test2', 4278190335);")

        p0.execSQL("INSERT INTO $TABLE_ENTRIES (entry_type, entry_date, entry_odo, entry_fuel_amount, entry_fuel_price, carId) VALUES " +
                "(2, '2019-12-11', 610, 52.4, 4.55, 1);")
        p0.execSQL("INSERT INTO $TABLE_ENTRIES (entry_type, entry_date, entry_odo, entry_fuel_amount, entry_fuel_price, carId) VALUES " +
                "(2, '2020-01-01', 655, 55.4, 4.75, 1);")
        p0.execSQL("INSERT INTO $TABLE_ENTRIES (entry_type, entry_date, entry_odo, entry_fuel_amount, entry_fuel_price, carId) VALUES " +
                "(2, '2020-02-13', 620, 50.0, 4.65, 1);")

        p0.execSQL("INSERT INTO $TABLE_ENTRIES (entry_type, entry_date, entry_odo, entry_fuel_amount, entry_fuel_price, carId) VALUES " +
                "(2, '2019-11-22', 499, 33.4, 4.55, 2);")
        p0.execSQL("INSERT INTO $TABLE_ENTRIES (entry_type, entry_date, entry_odo, entry_fuel_amount, entry_fuel_price, carId) VALUES " +
                "(2, '2020-01-01', 541, 37.4, 4.75, 2);")
        p0.execSQL("INSERT INTO $TABLE_ENTRIES (entry_type, entry_date, entry_odo, entry_fuel_amount, entry_fuel_price, carId) VALUES " +
                "(2, '2020-02-13', 504, 36.0, 4.65, 2);")
    }

    override fun onUpgrade(p0: SQLiteDatabase, oldVer: Int, newVer: Int) {
        p0.execSQL("DROP TABLE IF EXISTS $TABLE_CARS")
        p0.execSQL("DROP TABLE IF EXISTS $TABLE_ENTRIES")
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
            var _success = db.delete(TABLE_ENTRIES, "carId=?", arrayOf(getCarId(aCarName).toString()))
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
                newCar.mOilEntries.addAll(getAllOilEntries(cursor.getInt(cursor.getColumnIndex("id"))))
                result.add(newCar)
                while (cursor.moveToNext())
                {
                    newCar = Car(cursor.getString(cursor.getColumnIndex("name")))
                    newCar.mChartColor = cursor.getInt(cursor.getColumnIndex("chart_color"))
                    newCar.mFuelEntries.addAll(getAllFuelEntries(cursor.getInt(cursor.getColumnIndex("id"))))
                    newCar.mOilEntries.addAll(getAllOilEntries(cursor.getInt(cursor.getColumnIndex("id"))))
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
                values.put("entry_type", FUEL_ENTRY)
                values.put("entry_date", dateTimeFormat.format(aEntry.mDate))
                values.put("entry_odo", aEntry.mOdometer)
                values.put("entry_fuel_amount", aEntry.mFuelAmount)
                values.put("entry_fuel_price", aEntry.mPerLiter)
                val _success = db.insert(TABLE_ENTRIES, null, values)
                aEntry.mId = Integer.parseInt("$_success")
                return Integer.parseInt("$_success") != -1
            } finally {
                db.close()
            }
        }
        return false
    }

    fun addOilEntry(aOwnerId: Int, aEntry: OilEntry): Boolean {
        if (aOwnerId != -1) {
            val values = ContentValues()
            val db = this.writableDatabase
            try {
                values.put("carId", aOwnerId)
                values.put("entry_type", OIL_ENTRY)
                values.put("entry_date", dateTimeFormat.format(aEntry.mDate))
                values.put("entry_mil", aEntry.mOrgMileage)
                values.put("entry_remind_after", aEntry.mRemindAfter)
                val _success = db.insert(TABLE_ENTRIES, null, values)
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
            val _success = db.update(TABLE_ENTRIES, values, "entry_id=?", arrayOf(aEntry.mId.toString()))
            return Integer.parseInt("$_success") != -1
        } finally {
            db.close()
        }
    }

    fun editOilEntry(aEntry: OilEntry): Boolean {
        val values = ContentValues()
        val db = this.writableDatabase
        try {
            values.put("entry_mil", aEntry.mOrgMileage)
            values.put("entry_remind_after", aEntry.mRemindAfter)
            val _success = db.update(TABLE_ENTRIES, values, "entry_id=?", arrayOf(aEntry.mId.toString()))
            return Integer.parseInt("$_success") != -1
        } finally {
            db.close()
        }
    }

    fun removeEntry(aEntryId: Int): Boolean {
        val db = this.writableDatabase
        try {
            val _success = db.delete(TABLE_ENTRIES, "entry_id=?",  arrayOf(aEntryId.toString()))
            return Integer.parseInt("$_success") != -1
        } finally {
            db.close()
        }
    }

    private fun getAllOilEntries(aCarId: Int): OilEntriesList {
        val result = OilEntriesList()
        if (aCarId != -1) {
            val db = this.readableDatabase
            val cursor = db.rawQuery("SELECT * FROM $TABLE_ENTRIES WHERE $CARID_PARAM=? AND $ENTRY_TYPE_PARAM=?", arrayOf(aCarId.toString(), OIL_ENTRY.toString()))
            try {
                if (cursor.moveToFirst()) {
                    var entry = OilEntry()
                    entry.mId = cursor.getInt(cursor.getColumnIndex("entry_id"))
                    entry.mDate = dateTimeFormat.parse(cursor.getString(cursor.getColumnIndex("entry_date")))
                    entry.mRemindAfter = cursor.getInt(cursor.getColumnIndex("entry_remind_after"))
                    entry.mOrgMileage = cursor.getInt(cursor.getColumnIndex("entry_mil"))
                    result.add(entry)
                    while (cursor.moveToNext()) {
                        entry = OilEntry()
                        entry.mId = cursor.getInt(cursor.getColumnIndex("entry_id"))
                        entry.mDate = dateTimeFormat.parse(cursor.getString(cursor.getColumnIndex("entry_date")))
                        entry.mRemindAfter = cursor.getInt(cursor.getColumnIndex("entry_remind_after"))
                        entry.mOrgMileage = cursor.getInt(cursor.getColumnIndex("entry_mil"))
                        result.add(entry)
                    }
                }
            } finally {
                cursor.close()
            }
        }
        return result
    }

    private fun getAllFuelEntries(aCarId: Int): FuelEntriesList {
        val result = FuelEntriesList()
        if (aCarId != -1) {
            val db = this.readableDatabase
            val cursor = db.rawQuery("SELECT * FROM $TABLE_ENTRIES WHERE $CARID_PARAM=? AND $ENTRY_TYPE_PARAM=?", arrayOf(aCarId.toString(), FUEL_ENTRY.toString()))
            try {
                if (cursor.moveToFirst())
                {
                    var entry = FuelEntry()
                    entry.mId = cursor.getInt(cursor.getColumnIndex("entry_id"))
                    entry.mOdometer = cursor.getInt(cursor.getColumnIndex("entry_odo"))
                    entry.mDate = dateTimeFormat.parse(cursor.getString(cursor.getColumnIndex("entry_date")))
                    entry.mFuelAmount = cursor.getDouble(cursor.getColumnIndex("entry_fuel_amount"))
                    entry.mPerLiter = cursor.getDouble(cursor.getColumnIndex("entry_fuel_price"))
                    result.add(entry)
                    while (cursor.moveToNext())
                    {
                        entry = FuelEntry()
                        entry.mId = cursor.getInt(cursor.getColumnIndex("entry_id"))
                        entry.mOdometer = cursor.getInt(cursor.getColumnIndex("entry_odo"))
                        entry.mDate = dateTimeFormat.parse(cursor.getString(cursor.getColumnIndex("entry_date")))
                        entry.mFuelAmount = cursor.getDouble(cursor.getColumnIndex("entry_fuel_amount"))
                        entry.mPerLiter = cursor.getDouble(cursor.getColumnIndex("entry_fuel_price"))
                        result.add(entry)
                    }
                }
            } finally {
                cursor.close()
            }
        }
        return result
    }

    companion object {
        const val DATABASE_NAME = "carsdb"
        const val DATABASE_VERSION = 7
        const val TABLE_CARS = "cars"
        const val TABLE_ENTRIES = "entries"
        const val NAME_PARAM = "name"
        const val CARID_PARAM = "carId"
        const val ENTRY_TYPE_PARAM = "entry_type"
    }
}
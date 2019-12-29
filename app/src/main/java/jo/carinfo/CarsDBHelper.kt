package jo.carinfo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class CarsDBHelper(ctx: Context): SQLiteOpenHelper(ctx, DATABASE_NAME, null, DATABASE_VERSION) {

    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreate(p0: SQLiteDatabase) {
        val create_table = (
                "CREATE TABLE IF NOT EXISTS $TABLE_CARS ('id' INTEGER PRIMARY KEY," +
                "'name' VARCHAR(45) NULL);" +

                "CREATE TABLE IF NOT EXISTS $TABLE_ENTRIES (" +
                "'entry_id' INTEGER PRIMARY KEY," +
                "'entry_date' VARCHAR(19) NOT NULL," +
                "'entry_odo' INTEGER NULL," +
                "'entry_mil' INTEGER NULL," +
                "'entry_fuel_amount' REAL NOT NULL," +
                "'entry_fuel_price' REAL NOT NULL," +
                "'carId' INTEGER NOT NULL," +
                "FOREIGN KEY(carId) REFERENCES cars(id));")
        p0.execSQL(create_table)
    }

    override fun onUpgrade(p0: SQLiteDatabase, oldVer: Int, newVer: Int) {
        p0.execSQL("DROP TABLE IF EXISTS $TABLE_CARS;" +
                        "DROP TABLE IF EXISTS $TABLE_ENTRIES;")
        onCreate(p0)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun addCar(aCarName: String): Boolean {
        val values = ContentValues()
        val db = this.writableDatabase
        try {
            values.put("name", aCarName)
            val _success = db.insert(TABLE_CARS, null, values)
            return Integer.parseInt("$_success") != -1
        } finally {
            db.close()
        }
    }

    fun editCarName(aOldCarName: String, aNewName: String): Boolean {
        val values = ContentValues()
        val db = this.writableDatabase
        try {
            values.put("name", aNewName)
            val _success = db.update(TABLE_CARS, values, "name=?", arrayOf(aOldCarName))
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
        val result = CarsList()
        if (cursor.moveToFirst())
        {
            var newCar = Car(cursor.getString(cursor.getColumnIndex("name")))
            newCar.mFuelEntries.addAll(getAllFuelEntries(cursor.getInt(cursor.getColumnIndex("id"))))
            result.add(newCar)
            while (cursor.moveToNext())
            {
                newCar = Car(cursor.getString(cursor.getColumnIndex("name")))
                newCar.mFuelEntries.addAll(getAllFuelEntries(cursor.getInt(cursor.getColumnIndex("id"))))
                result.add(newCar)
            }
        }
        cursor.close()
        return result
    }

    fun addFuelEntry(aOwnerId: Int, aEntry: FuelEntry): Boolean {
        if (aOwnerId != -1) {
            val values = ContentValues()
            val db = this.writableDatabase
            try {
                values.put("carId", aOwnerId)
                values.put("entry_date", dateTimeFormat.format(aEntry.mDate))
                values.put("entry_odo", aEntry.mOdometer)
                values.put("entry_mil", aEntry.mMileage)
                values.put("entry_fuel_amount", aEntry.mFuelAmount)
                values.put("entry_fuel_price", aEntry.mPerLiter)
                val _success = db.insert(TABLE_ENTRIES, null, values)
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
            values.put("entry_mil", aEntry.mMileage)
            values.put("entry_fuel_amount", aEntry.mFuelAmount)
            values.put("entry_fuel_price", aEntry.mPerLiter)
            val _success = db.update(TABLE_ENTRIES, values, "entry_id=?", arrayOf(aEntry.mId.toString()))
            return Integer.parseInt("$_success") != -1
        } finally {
            db.close()
        }
    }

    fun removeFuelEntry(aEntryId: Int): Boolean {
        val db = this.writableDatabase
        try {
            val _success = db.delete(TABLE_ENTRIES, "entry_id=?",  arrayOf(aEntryId.toString()))
            return Integer.parseInt("$_success") != -1
        } finally {
            db.close()
        }
    }

    private fun getAllFuelEntries(aCarId: Int): FuelEntriesList {
        var result = FuelEntriesList()
        if (aCarId != -1) {
            val db = this.readableDatabase
            val cursor = db.rawQuery("SELECT * FROM $TABLE_ENTRIES WHERE $CARID_PARAM=?", arrayOf(aCarId.toString()))
            try {
                if (cursor.moveToFirst())
                {
                    var entry = FuelEntry()
                    entry.mId = cursor.getInt(cursor.getColumnIndex("entry_id"))
                    entry.mMileage = cursor.getInt(cursor.getColumnIndex("entry_mil"))
                    entry.mOdometer = cursor.getInt(cursor.getColumnIndex("entry_odo"))
                    entry.mDate = dateTimeFormat.parse(cursor.getString(cursor.getColumnIndex("entry_date")))
                    entry.mFuelAmount = cursor.getDouble(cursor.getColumnIndex("entry_fuel_amount"))
                    entry.mPerLiter = cursor.getDouble(cursor.getColumnIndex("entry_fuel_price"))
                    result.add(entry)
                    while (cursor.moveToNext())
                    {
                        entry = FuelEntry()
                        entry.mId = cursor.getInt(cursor.getColumnIndex("entry_id"))
                        entry.mMileage = cursor.getInt(cursor.getColumnIndex("entry_mil"))
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
        const val DATABASE_VERSION = 3
        const val TABLE_CARS = "cars"
        const val TABLE_ENTRIES = "entries"
        const val NAME_PARAM = "name"
        const val CARID_PARAM = "carId"
    }
}
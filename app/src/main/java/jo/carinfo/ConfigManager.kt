package jo.carinfo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import java.io.*
import com.google.gson.GsonBuilder


class ConfigManager(private var ctx: Context): SQLiteOpenHelper(ctx, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(p0: SQLiteDatabase) {
        val create_table = ("CREATE TABLE 'cars' ('id' INTEGER PRIMARY KEY," +
                            "'name' VARCHAR(45) NULL);")
        p0.execSQL(create_table)
    }

    override fun onUpgrade(p0: SQLiteDatabase, oldVer: Int, newVer: Int) {
        p0.execSQL("DROP TABLE IF EXISTS 'cars'")
        onCreate(p0)
    }

    fun addCar(aCarName: String): Boolean {
        val values = ContentValues()
        val db = this.writableDatabase
        try {
            values.clear()
            values.put("name", aCarName)
            val _success = db.insert("cars", null, values)
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
            val _success = db.update("cars", values, "name=?", arrayOf(aOldCarName))
            return Integer.parseInt("$_success") != -1
        } finally {
            db.close()
        }
    }

    fun removeCar(aCarName: String): Boolean {
        val db = this.writableDatabase
        try {
            val _success = db.delete("cars", "name=?",  arrayOf(aCarName))
            return Integer.parseInt("$_success") != -1
        } finally {
            db.close()
        }
    }

    fun removeCars(aCars: CarsList): Boolean {
        val values = ContentValues()
        val db = this.writableDatabase
        try {
            for (car in aCars)
                db.delete("cars", "name=?",  arrayOf(car.mName))
            return true
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
                _success = db.insert("cars", null, values)
                if (Integer.parseInt("$_success") == -1)
                    return false
            }
            return true
        } finally {
            db.close()
        }
    }

    fun getAllCars(): CarsList {
        val db = this. readableDatabase
        val cursor = db.rawQuery("SELECT * FROM 'cars'", null)
        val result = CarsList()
        if (cursor.moveToFirst())
        {
            result.add(Car(cursor.getString(cursor.getColumnIndex("name"))))
            while (cursor.moveToNext())
                result.add(Car(cursor.getString(cursor.getColumnIndex("name"))))
        }
        cursor.close()
        return result
    }

    companion object {
        const val DATABASE_NAME = "carsdb"
        const val DATABASE_VERSION = 1
    }
}
package jo.carinfo

import android.content.Context
import android.util.Log
import android.widget.Toast
import java.io.*

const val CARS_FILE = "cars.txt"

class ConfigManager(private var context: Context) {

    fun getCars(): CarsList
    {
        Log.d("CFGMGR", "getting cars")
        var result = CarsList()
        val fileObj = File(context.filesDir, CARS_FILE)

        if (fileObj.exists())
        {
            val f = FileInputStream(fileObj)
            val o = ObjectInputStream(f)
            try
            {
                Log.d("CFGMGR", "cars file exists, reading...")
                result = o.readObject() as CarsList
                Log.d("CFGMGR", "got " + result.count().toString() + " cars from file")

            }catch(e: Exception)
            {
                val builder = StringBuilder()
                builder.append(R.string.couldNotGetCars)
                builder.append(" - ")
                builder.append(e.message)
                Toast.makeText(context, builder.toString(), Toast.LENGTH_SHORT).show()
            }
            finally
            {
                o.close()
                f.close()
            }
        }
        else
            Log.d("CFGMGR", "cars file does not exists")

        return result
    }

    fun saveCars(aCarsList: CarsList): Boolean {
        Log.d("CFGMGR", "saving cars")

        var fileObj = File(context.filesDir, CARS_FILE)
        try {
            if (!fileObj.parentFile.exists())
                fileObj.parentFile.mkdirs()

            if (!fileObj.exists())
                fileObj.createNewFile()

            Log.d("CFGMGR", "saving " + aCarsList.count().toString() + " cars to file")

            val f = FileOutputStream(fileObj)
            val o = ObjectOutputStream(f)

            try {
                o.writeObject(aCarsList)
                Log.d("CFGMGR", "cars saved")
            } catch (e: Exception)
            {
                return false
            } finally {
                o.close()
                f.close()
            }

            return true
        } catch (e: FileNotFoundException) {
            Log.e("CFGMGR", e.message)
            return false
        }
    }

}
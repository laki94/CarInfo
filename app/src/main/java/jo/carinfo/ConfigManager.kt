package jo.carinfo

import android.content.Context
import android.util.Log
import android.widget.Toast
import java.io.*
import com.google.gson.GsonBuilder

const val CARS_FILE = "cars.txt"

class ConfigManager(private var context: Context) {

    fun getCars(): CarsList
    {
        Log.d("CFGMGR", "getting cars")
        var result = CarsList()
        val fileObj = File(context.filesDir, CARS_FILE)

        if (fileObj.exists())
        {
            val gson = GsonBuilder().setPrettyPrinting().create()
            try
            {
                Log.d("CFGMGR", "cars file exists, reading...")
                result = gson.fromJson<CarsList>(fileObj.readText(), CarsList::class.java)
                Log.d("CFGMGR", "got " + result.count().toString() + " cars from file")
            }catch(e: Exception)
            {
                val builder = StringBuilder()
                builder.append(R.string.couldNotGetCars)
                builder.append(" - ")
                builder.append(e.message)
                Toast.makeText(context, builder.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        else
            Log.d("CFGMGR", "cars file does not exists")

        return result
    }

    fun saveCars(aCarsList: CarsList): Boolean {
        val fileObj = File(context.filesDir, CARS_FILE)
        try {
            if (!fileObj.parentFile.exists())
                fileObj.parentFile.mkdirs()

            if (!fileObj.exists())
                fileObj.createNewFile()

            Log.d("CFGMGR", "saving " + aCarsList.count().toString() + " cars to file")

            val f = FileOutputStream(fileObj)
            val sw = OutputStreamWriter(f)
            val gson = GsonBuilder().setPrettyPrinting().create()
            try {
                sw.write(gson.toJson(aCarsList))
                Log.d("CFGMGR", "cars saved")
            } catch (e: Exception)
            {
                Log.e("CFGMGR", String.format("cars not saved, %s", e.message))
                return false
            } finally {
                sw.close()
                f.close()
            }

            return true
        } catch (e: FileNotFoundException) {
            Log.e("CFGMGR", e.message)
            return false
        }
    }

}
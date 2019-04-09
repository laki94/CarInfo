package jo.carinfo

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val settings = SharedSettingsClass(this)

        if (settings.isLaunchedForFirstTime()) {
            Log.d("Main", "opened for first time")
            settings.setWasLaunchedForFirstTime()
        }
        else
            Log.d("Main", "not first time")
    }

    fun onSettingsClick(view : View){
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
}

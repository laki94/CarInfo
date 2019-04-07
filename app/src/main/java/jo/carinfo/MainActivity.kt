package jo.carinfo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var settings = SharedSettingsClass(this)

        if (settings.isLaunchedForFirstTime()) {
            Log.d("Main", "opened for first time")
            settings.setWasLaunchedForFirstTime()

        }
        else
            Log.d("Main", "not first time")
    }
}

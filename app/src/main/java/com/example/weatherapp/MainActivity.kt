package com.example.weatherapp

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.databinding.ActivityMainBinding
import org.json.JSONObject
import java.util.concurrent.Executors
const val key:String = "7f46ec81682646709f8194238232401"
const val url:String = "https://api.weatherapi.com/v1/current.json?key=7f46ec81682646709f8194238232401&q=Astana&aqi=no"

class MainActivity : AppCompatActivity() {

    private val CHANNEL_ID = "channel_id_example_01"
    private val notificationId = 101

    private lateinit var binding: ActivityMainBinding
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide();
        val queue = Volley.newRequestQueue(this)
        val stringRequest= StringRequest(Request.Method.GET, url,
            {
                response ->
                    val obj = JSONObject(response)
                    val locationInfo = obj.getJSONObject("location")
                    val forecastInfo = obj.getJSONObject("current")
                    binding.address.text = locationInfo.getString("name")
                    binding.updatedAt.text = "Updated at: ${forecastInfo.getString("last_updated")}"
                    binding.temp.text = "${forecastInfo.getString("temp_c")}°C"
                    binding.feelslike.text = "Feels like ${forecastInfo.getString("feelslike_c")}°C"
                    binding.wind.text = "Wind ${forecastInfo.getString("wind_kph")} kph"
                    val executor = Executors.newSingleThreadExecutor()

                    val handler = Handler(Looper.getMainLooper())

                    var image: Bitmap? = null
                    executor.execute {
                        val imageURL = forecastInfo.getJSONObject("condition").getString("icon")

                        try {
                            val `in` = java.net.URL(imageURL).openStream()
                            image = BitmapFactory.decodeStream(`in`)

                            handler.post {
                                binding.img1.setImageBitmap(image)
                            }
                        }
                        catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
            {Log.e("My Log", "Error: $it")})
        queue.add(stringRequest)


        createNotificationChannel()
        val btn_button = findViewById(R.id.btn_button) as Button

        btn_button.setOnClickListener {
            sendNotification()
        }

    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification Title"
            val descriptionText = "Notification Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID,name,importance).apply {
                 description= descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification() {
        val intent = Intent(this, MainActivity::class.java). apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("How is it feeling?")
            .setContentText("Check the weather now!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
    }

}
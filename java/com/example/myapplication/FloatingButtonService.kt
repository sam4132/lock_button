package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.NotificationCompat

class FloatingButtonService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private var rectangleView: View? = null
    private var isButtonClicked = false

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "foreground_service_channel"
        private const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        val stopSelfIntent = Intent(this, FloatingButtonService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val pendingIntent = PendingIntent.getService(this, 0, stopSelfIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.kirito)
            .setContentTitle("maimai button")
            .setContentText("maimai button is running")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(R.drawable.kirito, "Stop Service", pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_button, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 100

        windowManager.addView(floatingView, params)

        val button = floatingView.findViewById<ImageView>(R.id.floatingImageButton)
        button.setImageResource(R.drawable.baseline_lock_24)
        button.setOnClickListener {
            if (isButtonClicked) {
                button.setImageResource(R.drawable.baseline_lock_24)
                rectangleView?.let { view -> windowManager.removeView(view) }
                rectangleView = null
            } else {
                button.setImageResource(R.drawable.lock1)
                showRectangleView()
            }
            isButtonClicked = !isButtonClicked
            showToast()
        }

        // Listen for screen orientation changes
        registerOrientationListener()

        // Register broadcast receiver
        val filter = IntentFilter(ACTION_STOP_SERVICE)
        registerReceiver(stopServiceReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
    }

    private val stopServiceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_STOP_SERVICE) {
                stopSelf()
            }
        }
    }

    private fun showToast() {
        val message = if (isButtonClicked) {
            "Button is clicked!"
        } else {
            "Button is unclicked!"
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showRectangleView() {
        rectangleView = View(this)
        val rectangleParams = WindowManager.LayoutParams(
            calculateWidth(), // Width of the rectangle
            calculateHeight(), // Height of the rectangle
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        rectangleParams.gravity = Gravity.TOP or Gravity.START
        rectangleParams.x = 200
        rectangleParams.y = 0
        rectangleView?.setBackgroundColor(Color.argb(30, 255, 255, 255)) // White color with 70% transparency
        windowManager.addView(rectangleView, rectangleParams)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun calculateWidth(): Int {
        return if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            1800 // Width of the rectangle in landscape mode
        } else {
            300 // Width of the rectangle in portrait mode
        }
    }

    private fun calculateHeight(): Int {
        return if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            1080 // Height of the rectangle in landscape mode
        } else {
            200 // Height of the rectangle in portrait mode
        }
    }

    private fun registerOrientationListener() {
        val orientationListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                val currentOrientation = resources.configuration.orientation
                if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE ||
                    currentOrientation == Configuration.ORIENTATION_PORTRAIT
                ) {
                    // Remove and re-add the rectangle view to adjust its size and position
                    rectangleView?.let { view ->
                        windowManager.removeView(view)
                        showRectangleView()
                    }
                }
            }
        }
        if (orientationListener.canDetectOrientation()) {
            orientationListener.enable()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
        rectangleView?.let { view -> windowManager.removeView(view) }
        unregisterReceiver(stopServiceReceiver)
        // Reset button click state and icon
        floatingView.findViewById<ImageView>(R.id.floatingImageButton)?.setImageResource(R.drawable.baseline_lock_24)
        isButtonClicked = false
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(serviceChannel)
        }
    }
}

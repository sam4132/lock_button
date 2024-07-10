package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Switch
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private val overlayPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
            // Permission granted, start your service here
            toggleService(switch1.isChecked)
        }
    }

    private lateinit var switch1: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)

        switch1 = findViewById(R.id.switch1)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        }

        // 设置开关状态监听
        switch1.setOnCheckedChangeListener { _, isChecked ->
            toggleService(isChecked)
        }
    }

    private fun toggleService(start: Boolean) {
        val intent = Intent(this, FloatingButtonService::class.java)
        if (start) {
            ContextCompat.startForegroundService(this, intent)
        } else {
            stopService(intent)
        }
    }
}
